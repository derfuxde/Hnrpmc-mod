package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.BB_LOGIN_SIZE;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.BB_SHOW_ON_LOGIN;

public class SCStaticPlayerListener {
    private static SimpleClans plugin;
    private static SettingsManager settingsManager;

    public SCStaticPlayerListener(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
        this.settingsManager = plugin.getSettingsManager();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getEntity().level().isClientSide) return;

        ClanPlayer cp = plugin.getClanManager().getCreateClanPlayer(player.getUUID());

        //updatePlayerName(player);
        plugin.getClanManager().updateLastSeen(player);
        //plugin.getClanManager().updateDisplayName(player);

        plugin.getPermissionsManager().addPlayerPermissions(cp);

        cp.getClan().displayBb(player, settingsManager.getInt(BB_LOGIN_SIZE));
        plugin.getLogger().info("showing bb");

        plugin.getPermissionsManager().addClanPermissions(cp);
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
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
}
