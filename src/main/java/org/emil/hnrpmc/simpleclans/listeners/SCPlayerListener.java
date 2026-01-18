package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.chat.SCMessage.Source;
import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanConvo;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.CLAN;
import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.NONE;
import static org.emil.hnrpmc.simpleclans.SimpleClans.getInstance;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class SCPlayerListener {

    private final SimpleClans plugin;
    private final SettingsManager settingsManager;

    public SCPlayerListener(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
        this.settingsManager = plugin.getSettingsManager();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (isBlacklistedWorld(player)) return;

        ClanPlayer cp = plugin.getClanManager().getCreateClanPlayer(player.getUUID());

        //updatePlayerName(player);
        plugin.getClanManager().updateLastSeen(player);
        //plugin.getClanManager().updateDisplayName(player);

        plugin.getPermissionsManager().addPlayerPermissions(cp);

        if (settingsManager.is(BB_SHOW_ON_LOGIN) && cp.isBbEnabled() && cp.getClan() != null) {
            cp.getClan().displayBb(player, settingsManager.getInt(BB_LOGIN_SIZE));
            plugin.getLogger().info("showing bb");
        }

        plugin.getPermissionsManager().addClanPermissions(cp);
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUUID());
        for (ClanPlayer clanPlayer : plugin.getClanManager().getAllClanPlayers()) {
            plugin.getStorageManager().updateClanPlayer(clanPlayer);
        }
        if (cp != null && cp.getClan() != null) {
            Clan clan = cp.getClan();
            // Sicherstellen, dass getProtectionManager() in SimpleClans existiert
            if (clan.getOnlineMembers().size() <= 1) {
            }
        }

        if (cp != null) {
            plugin.getPermissionsManager().removeClanPlayerPermissions(cp);
        }
        plugin.getClanManager().updateLastSeen(player);
        plugin.getRequestManager().endPendingRequest(player.getName().getString());
    }

    @SubscribeEvent
    public void onPlayerCommand(CommandEvent event) {
        CommandSourceStack source = event.getParseResults().getContext().getSource();

        // Prüfen, ob der Sender ein Spieler ist
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Der vollständige Befehlstext (ohne führenden /)
        String commandLine = event.getParseResults().getReader().getString();
        plugin.getLogger().info("got an command {}", commandLine);
        String[] split = commandLine.split(" ");
        if (split.length == 0) return;

        String command = split[0];

        // Logik aus deinem Bukkit-Code
        if (settingsManager.is(CLANCHAT_TAG_BASED)) {
            Clan clan = plugin.getClanManager().getClan(command);

            // Wenn es ein Clan-Tag ist und der Spieler Mitglied ist
            if (clan != null && clan.isMember(player.getUUID())) {
                // Event abbrechen, damit der "falsche" Befehl nicht ausgeführt wird
                event.setCanceled(true);

                // Den Befehl umschreiben zu /clan chat <nachricht>
                String clanChatCommand = settingsManager.getString(COMMANDS_CLAN_CHAT);
                String message = commandLine.substring(command.length()).trim();
                String finalCommand = clanChatCommand + " " + message;

                // Den neuen Befehl im Namen des Spielers ausführen
                plugin.getServer().getCommands().performPrefixedCommand(source, finalCommand);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        SCConversation convo = SCConversation.getConversation(player.getUUID());
        if (convo != null && convo.getState() == ClanConvo.ConversationState.STARTED) {
            event.setCanceled(true);

            String input = event.getMessage().getString().trim();
            if (!input.isEmpty()) {
                convo.acceptInput(input);
            }
            return;
        }
        SimpleClans plugin = SimpleClans.getInstance();

        // 1. Blacklist Check (World check)
        if (plugin.isBlacklistedWorld(player)) {
            plugin.getLogger().info("word ist on black list");
            return;
        }

        plugin.getLogger().info("word ist nicht on black list");

        ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(player.getUUID());
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!settingsManager.is(TELEPORT_HOME_ON_SPAWN) || isBlacklistedWorld(player)) {
            return;
        }

        for (ClanPlayer clanPlayer : plugin.getClanManager().getAllClanPlayers()) {
            plugin.getStorageManager().updateClanPlayer(clanPlayer);
        }

        Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (clan != null && clan.getHomeLocation() != null) {
            // Hier sollte deine Teleport-Logik folgen
        }
    }

    private void updatePlayerName(@NotNull ServerPlayer player) {
        String name = player.getName().getString();
        final ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(player.getUUID());

        for (ClanPlayer other : plugin.getClanManager().getAllClanPlayers()) {
            if (other.getName().equals(name) && !other.getUniqueId().equals(player.getUUID())) {
                other.setName(other.getUniqueId().toString());
                plugin.getStorageManager().updatePlayerName(other);
            }
        }
        if (cp != null) {
            cp.setName(name);
            plugin.getStorageManager().updatePlayerName(cp);
        }
    }

    private boolean isBlacklistedWorld(ServerPlayer player) {
        String worldName = player.level().dimension().location().toString();
        // Falls WORLD_BLACKLIST Fehler wirft, stelle sicher, dass es in ConfigField existiert
        return settingsManager.getList(SETTINGS_WORLD_BLACKLIST).contains(worldName);
    }
}