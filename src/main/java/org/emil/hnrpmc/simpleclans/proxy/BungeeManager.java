package org.emil.hnrpmc.simpleclans.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessageBuilder;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;
import org.emil.hnrpmc.simpleclans.proxy.adapters.ClanPlayerListAdapter;
import org.emil.hnrpmc.simpleclans.proxy.adapters.ClanPlayerTypeAdapterFactory;
import org.emil.hnrpmc.simpleclans.proxy.adapters.ConfigurationSerializableAdapter;
import org.emil.hnrpmc.simpleclans.proxy.adapters.SCMessageAdapter;
import org.emil.hnrpmc.simpleclans.proxy.dto.BungeePayload;
import org.emil.hnrpmc.simpleclans.proxy.listeners.MessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BungeeManager implements ProxyManager {

    private static final String UPDATE_CLAN_CHANNEL = "UpdateClan";
    private static final String UPDATE_CLANPLAYER_CHANNEL = "UpdateClanPlayer";
    private static final String DELETE_CLAN_CHANNEL = "DeleteClan";
    private static final String DELETE_CLANPLAYER_CHANNEL = "DeleteClanPlayer";
    private static final String CHAT_CHANNEL = "Chat";
    private static final String BROADCAST = "Broadcast";
    private static final String MESSAGE = "Message";
    private static final String VERSION = "v1";
    private static final Pattern SUBCHANNEL_PATTERN =
            Pattern.compile("SimpleClans\\|(?<subchannel>\\w+)\\|(?<version>v\\d+)\\|(?<server>.+)");

    private final SimpleClans plugin;
    private final Gson gson;
    private final List<String> onlinePlayers = new ArrayList<>();
    private String serverName = "";
    private final Set<String> unsupportedChannels = new HashSet<>();

    public BungeeManager(SimpleClans plugin) {
        this.plugin = plugin;

        // GSON FIX: Wir fügen den Adapter für Optional hinzu
        this.gson = SimpleClans.getInstance().getGSON();

        if (!plugin.getSettingsManager().is(ConfigField.PERFORMANCE_USE_BUNGEECORD)) {
            return;
        }

        // Die Plugin-Channel Registrierung wurde in die Klasse 'NetworkRegistration' verschoben!

        // Timer für die Player-Liste (NeoForge nutzt eigene Scheduler oder Java Timer)
        // Wenn du eine einfache Lösung willst, bleib bei einem Java-Executor oder dem NeoForge TickEvent
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::requestPlayerList, 0, 60, java.util.concurrent.TimeUnit.SECONDS
        );

        requestServerName();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] data) {
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        String subChannel = dataInput.readUTF();
        if (unsupportedChannels.contains(subChannel)) {
            return;
        }

        SimpleClans.debug("Message received, sub-channel: " + subChannel);
        try {
            String serverName = null;
            String className = subChannel;
            String version = null;
            Matcher matcher = SUBCHANNEL_PATTERN.matcher(subChannel);
            if (matcher.find()) {
                serverName = matcher.group("server");
                className = matcher.group("subchannel");
                version = matcher.group("version");
            }
            Class<?> clazz = Class.forName("org.emil.hnrpmc.simpleclans.proxy.listeners." + className);
            MessageListener listener = (MessageListener) clazz.getConstructor(BungeeManager.class).newInstance(this);
            if (listener.isBungeeSubchannel()) {
                listener.accept(dataInput); //uses the original data
                return;
            }
            if (!VERSION.equals(version)) {
                plugin.getLogger().error(String.format("Unsupported channel (%s), expected version: %s", subChannel, VERSION));
                unsupportedChannels.add(subChannel);
                return;
            }
            if (serverName != null && !isServerAllowed(serverName)) {
                SimpleClans.debug(String.format("Server not allowed: %s", serverName));
                return;
            }
            byte[] messageBytes = new byte[dataInput.readShort()];
            dataInput.readFully(messageBytes);
            final ByteArrayDataInput message = ByteStreams.newDataInput(messageBytes);

            listener.accept(message); // uses the internal data
        } catch (ClassNotFoundException e) {
            SimpleClans.debug(String.format("Unknown channel: %s", subChannel));
            unsupportedChannels.add(subChannel);
        } catch (Exception ex) {
            plugin.getLogger().info(String.format("Error processing channel %s", subChannel), ex);
        }
    }

    private boolean isServerAllowed(@NotNull String serverName) {
        List<String> servers = plugin.getSettingsManager().getStringList(ConfigField.BUNGEE_SERVERS);
        if (servers.isEmpty()) {
            return true;
        }

        return servers.contains(serverName);
    }

    @Override
    public boolean isOnline(String playerName) {
        if (SimpleClans.getInstance().getServer().getPlayerList().getPlayers().stream().anyMatch(player -> player.getName().equals(playerName))) {
            return true;
        }
        return onlinePlayers.contains(playerName);
    }

    public void setOnlinePlayers(@NotNull List<String> onlinePlayers) {
        this.onlinePlayers.clear();
        this.onlinePlayers.addAll(onlinePlayers);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    public void setServerName(@NotNull String name) {
        this.serverName = name;
    }

    @Override
    public void sendMessage(SCMessage message) {
        forwardToOnlineServers(CHAT_CHANNEL, gson.toJson(message));
    }

    @Override
    public void sendMessage(@NotNull String target, @NotNull String message) {
        if (message.isEmpty()) {
            return;
        }

        if ("ALL".equals(target)) {
            SimpleClans.getInstance().getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(Component.literal(message)));
            sendBroadcast(message);
            return;
        }

        Player player = SimpleClans.getInstance().getServer().getPlayerList().getPlayerByName(target);
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
            return;
        }

        sendPrivateMessage(target, message);
    }

    private void sendBroadcast(@NotNull String message) {
        forwardToOnlineServers(BROADCAST, message);
    }

    @Override
    public void sendDelete(Clan clan) {
        forwardToAllServers(DELETE_CLAN_CHANNEL, clan.getTag());
    }

    @Override
    public void sendDelete(ClanPlayer cp) {
        forwardToAllServers(DELETE_CLANPLAYER_CHANNEL, cp.getUniqueId().toString());
    }

    public record ClanUpdatePacket(String clanName, String tag, List<UUID> members) {}

    @Override
    public void sendUpdate(Clan clan) {
        // Wandle den Clan in ein einfaches Paket um
        ClanUpdatePacket packet = new ClanUpdatePacket(
                clan.getName().toString(),
                clan.getTag(),
                clan.getMemberUUIDs()
        );

        String json = gson.toJson(packet);
        forwardToAllServers(UPDATE_CLANPLAYER_CHANNEL, json);
    }

    @Override
    public void sendUpdate(ClanPlayer cp) {
        if (cp == null) return;
        String json = gson.toJson(cp);
        forwardToAllServers(UPDATE_CLANPLAYER_CHANNEL, json);
    }

    public SimpleClans getPlugin() {
        return plugin;
    }

    public Gson getGson() {
        return gson;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void requestPlayerList() {
        if (!isChannelRegistered()) {
            return;
        }
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("PlayerList");
        output.writeUTF("ALL");

    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private void requestServerName() {
        scheduler.scheduleAtFixedRate(() -> {
            // 1. Check ob serverName schon da ist (entspricht task.cancel())
            if (!serverName.isEmpty()) {
                scheduler.shutdown(); // Stoppt den Timer
                return;
            }

            // 2. Ersten Spieler finden (entspricht Bukkit.getOnlinePlayers().stream().findFirst())
            // Wir nutzen ServerLifecycleHooks um an den aktuellen Server zu kommen
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            ServerPlayer player = server.getPlayerList().getPlayers().stream().findFirst().orElse(null);

            if (player != null) {
                // 3. Daten vorbereiten (entspricht ByteArrayDataOutput)
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeUtf("GetServer");

                // 4. Senden via NeoForge Payload
                // Wir nutzen die SDLinkPayload oder eine BungeePayload Klasse
                byte[] data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);

                // Sende das Paket an den Proxy
                //player.connection.send(new ClientboundCustomPayloadPacket(new SDLinkPayload(data)));
            }
        }, 0, 1, TimeUnit.SECONDS); // 0L, 20L (1 Sekunde bei 20 Ticks)
    }

    private String createSubchannelName(String subchannel) {
        return String.format("SimpleClans|%s|%s|%s", subchannel, VERSION, serverName);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendPrivateMessage(@NotNull String playerName, @NotNull String message) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(playerName);
        output.writeUTF(message);

        forwardPluginMessage(MESSAGE, output, false);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void forwardToAllServers(final String subChannel, final String message) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(message);
        forwardPluginMessage(subChannel, output, true);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void forwardToOnlineServers(final String subChannel, final String message) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(message);
        forwardPluginMessage(subChannel, output, false);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void forwardPluginMessage(final String subChannel, final ByteArrayDataOutput message, final boolean all) {
        SimpleClans.debug(String.format("Forwarding message, channel %s, message %s, all %s", subChannel, message, all));
        if (!isChannelRegistered()) {
            return;
        }
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Forward");
        output.writeUTF(all ? "ALL" : "ONLINE");
        output.writeUTF(createSubchannelName(subChannel));

        output.writeShort(message.toByteArray().length);
        output.write(message.toByteArray());

        sendOnBungeeChannel(output);
    }

    private void sendOnBungeeChannel(ByteArrayDataOutput output) {
        ServerPlayer player = plugin.getServer().getPlayerList().getPlayers().stream().findAny().orElse(null);

        if (player != null) {
            // Wir senden nun unsere eigene, registrierte Payload-Klasse
            // Dies umgeht die Sicherheitsprüfung, da die ID nun dem System bekannt ist
            player.connection.send(new ClientboundCustomPayloadPacket(
                    new BungeePayload(output.toByteArray())
            ));

            SimpleClans.debug("Bungee message forwarded via BungeeCordPayload.");
        }
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isChannelRegistered() {
        boolean registered = NetworkRegistration.IS_REGISTERED;
        SimpleClans.debug(String.format("BungeeCord/SDLink channel registered: %s", registered));
        return registered;
    }

}
