package org.emil.hnrpmc.simpleclans.managers;

import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.sdlink.api.events.SDLinkReadyEvent;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.mojang.authlib.GameProfile;
import net.neoforged.fml.ModList;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.chat.ChatHandler;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook;
import org.emil.hnrpmc.simpleclans.hooks.placeholder.PlaceholderContext;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.DISCORD;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public final class ChatManager {

    private SimpleClans plugin;
    private final Set<ChatHandler> handlers = new HashSet<>();

    private DiscordHook discordHook;

    public ChatManager(SimpleClans plugin) {
        this.plugin = plugin;
        registerHandlers();
    }

    @CraterEventListener(priority = 100)
    public void registerDiscord(SDLinkReadyEvent event) {
        if (plugin == null) {
            plugin = SimpleClans.getInstance();
        }
        plugin.getLogger().info(event.wasCancelled() ? "event wurde gecaneled" : "läuft");
        this.discordHook = new DiscordHook(plugin);
        plugin.getLogger().info("discordhooker ist regestirert");
    }

    @Nullable
    public DiscordHook getDiscordHook(SimpleClans tplugin) {
        if (isDiscordHookEnabled(tplugin)) {
            // Falls der Hook noch null ist, versuchen wir ihn zu initialisieren
            if (discordHook == null) {

                discordHook = new DiscordHook(tplugin);

            }
        }
        return discordHook;
    }

    public static boolean isDiscordHookEnabled(SimpleClans tplugin) {
        // Prüft, ob der Mod "sdlink" im mods-Ordner installiert und geladen ist
        boolean isModLoaded = ModList.get().isLoaded("sdlink") && ChannelManager.getConsoleChannel() != null;

        // Prüft zusätzlich deine interne Config-Einstellung
        boolean isEnabledInConfig = tplugin.getSettingsManager().is(SettingsManager.ConfigField.DISCORDCHAT_ENABLE);

        tplugin.getLogger().info("discord ist {} und in der ocnfig {}", isModLoaded ? "geladen" : "nicht geladen", isEnabledInConfig ? "aktiv" : "nicht aktiv");

        return isModLoaded && isEnabledInConfig;
    }

    public void processChat(@NotNull SCMessage message) {
        Clan clan = Objects.requireNonNull(message.getSender().getClan(), "Clan cannot be null");
        plugin.getLogger().info("processChat {}", message.getContent());

        List<ClanPlayer> receivers = new ArrayList<>();
        switch (message.getChannel()) {
            case ALLY:
                if (!plugin.getSettingsManager().is(ALLYCHAT_ENABLE)) {
                    return;
                }

                receivers.addAll(getOnlineAllyMembers(clan).stream().filter(allyMember ->
                        !allyMember.isMutedAlly()).toList());
                receivers.addAll(clan.getOnlineMembers().stream().filter(onlineMember ->
                        !onlineMember.isMutedAlly()).toList());
                break;
            case CLAN:
                if (!plugin.getSettingsManager().is(CLANCHAT_ENABLE)) {
                    return;
                }

                receivers.addAll(clan.getOnlineMembers().stream().filter(member -> !member.isMuted()).
                        toList());
        }
        message.setReceivers(receivers);

        for (ChatHandler ch : handlers) {
            if (ch.canHandle(message.getSource())) {
                ch.sendMessage(message.clone());
            }
        }
    }

    public void processChat(@NotNull Source source, @NotNull Channel channel,
                            @NotNull ClanPlayer clanPlayer, String message) {
        Objects.requireNonNull(clanPlayer.getClan(), "Clan cannot be null");
        plugin.getLogger().info("sending {} chat {}", channel.name(), message);
        processChat(new SCMessage(source, channel, clanPlayer, message));
    }

    public String parseChatFormat(String format, SCMessage message) {
        return parseChatFormat(format, message, new HashMap<>());
    }

    public String parseChatFormat(String format, SCMessage message, Map<String, String> placeholders) {
        SettingsManager sm = plugin.getSettingsManager();
        ClanPlayer sender = message.getSender();

        String leaderColor = sm.getColored(ConfigField.valueOf(message.getChannel() + "CHAT_LEADER_COLOR"));
        String memberColor = sm.getColored(ConfigField.valueOf(message.getChannel() + "CHAT_MEMBER_COLOR"));
        String trustedColor = sm.getColored(ConfigField.valueOf(message.getChannel() + "CHAT_TRUSTED_COLOR"));

        String rank = sender.getRankId().isEmpty() ? null : ChatUtils.parseColors(sender.getRankDisplayName());
        ConfigField configField = ConfigField.valueOf(String.format("%sCHAT_RANK",
                message.getSource() == DISCORD ? "DISCORD" : message.getChannel()));
        String rankFormat = (rank != null) ? sm.getColored(configField).replace("%rank%", rank) : "";

        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                format = format.replace("%" + e.getKey() + "%", e.getValue());
            }
        }

        String parsedFormat = ChatUtils.parseColors(format)
                .replace("%clan%", Objects.requireNonNull(sender.getClan()).getColorTag())
                .replace("%clean-tag%", sender.getClan().getTag())
                .replace("%nick-color%",
                        (sender.isLeader() ? leaderColor : sender.isTrusted() ? trustedColor : memberColor))
                .replace("%player%", sender.getName())
                .replace("%rank%", rankFormat);
        parsedFormat = parseWithPapi(message.getSender(), parsedFormat)
                .replace("%message%", message.getContent());

        return parsedFormat;
    }

    private String parseWithPapi(ClanPlayer cp, String message) {
        GameProfile sender = plugin.getServer().getProfileCache().get(cp.getUniqueId()).get();
        PlaceholderContext plc = new PlaceholderContext(plugin, cp.toPlayer());
        message = plugin.getPlaceholderService().apply(plc, message);

        // If there are still placeholders left, try to parse them
        // E.g. if the user has a placeholder as LuckPerms prefix/suffix
        if (message.contains("%")) {
            message = plugin.getPlaceholderService().apply(plc, message);
        }
        return message;
    }

    private void registerHandlers() {
        Set<Class<? extends ChatHandler>> chatHandlers =
                Helper.getSubTypesOf("org.emil.hnrpmc.simpleclans.chat.handlers", ChatHandler.class);
        plugin.getLogger().info("Registering {} chat handlers...", chatHandlers.size());

        for (Class<? extends ChatHandler> handler : chatHandlers) {
            try {
                handlers.add(handler.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                plugin.getLogger().info("Error while trying to register {}: ",
                        ex.getMessage(), handler.getSimpleName());
            }
        }
    }

    private List<ClanPlayer> getOnlineAllyMembers(Clan clan) {
        return clan.getAllAllyMembers().stream().
                filter(allyPlayer -> allyPlayer.toPlayer() != null).
                collect(Collectors.toList());
    }
}
