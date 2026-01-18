package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.events.AddKillEvent;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.events.EconomyTransactionEvent.Cause.PLAYER_KILLED;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class PlayerDeath {

    private final SimpleClans plugin;

    public PlayerDeath(SimpleClans plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerDeath(LivingDeathEvent event) {
        // Prüfen, ob das Opfer ein Spieler ist
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }

        if (isNPC(victim) || isBlacklistedWorld(victim)) {
            return;
        }

        // Nutze deine Utility-Klasse Events, um den Angreifer zu finden
        Player attacker = Events.getPlayerFromSource(event.getSource());
        if (isInvalidKill(victim, attacker)) return;

        ClanPlayer victimCp = plugin.getClanManager().getCreateClanPlayer(victim.getUUID());
        ClanPlayer attackerCp = plugin.getClanManager().getCreateClanPlayer(attacker.getUUID());

        classifyKill(victimCp, attackerCp);
        giveMoneyReward(victimCp, attackerCp);

        // Record death for victim
        victimCp.addDeath();
        plugin.getStorageManager().updateClanPlayer(victimCp);
        plugin.getStorageManager().updateClanPlayer(attackerCp);

        // War Logik
        handleWarDeath(victim, attacker);
    }

    private void handleWarDeath(ServerPlayer victim, Player attacker) {
        Clan victimClan = plugin.getClanManager().getClanByPlayerUniqueId(victim.getUUID());
        Clan killerClan = plugin.getClanManager().getClanByPlayerUniqueId(attacker.getUUID());

        if (victimClan == null || killerClan == null) return;
    }

    private void classifyKill(@NotNull ClanPlayer victim, @NotNull ClanPlayer attacker) {
        Clan victimClan = victim.getClan();
        Clan attackerClan = attacker.getClan();

        if (victimClan == null || attackerClan == null || !victimClan.isVerified() || !attackerClan.isVerified()) {
            addKill(Kill.Type.CIVILIAN, attacker, victim);
        } else if (attackerClan.isRival(victim.getTag())) {
            addKill(Kill.Type.RIVAL, attacker, victim);
        } else if (attackerClan.isAlly(victimClan.getTag()) || attackerClan.equals(victimClan)) {
            addKill(Kill.Type.ALLY, attacker, victim);
        } else {
            addKill(Kill.Type.NEUTRAL, attacker, victim);
        }
    }

    private void giveMoneyReward(@NotNull ClanPlayer victim, @NotNull ClanPlayer attacker) {
        if (!plugin.getSettingsManager().is(ECONOMY_MONEY_PER_KILL)) return;

        Clan attackerClan = attacker.getClan();
        if (attackerClan == null) return;

        double reward = calculateReward(attacker, victim);
        if (reward != 0) {
            List<ClanPlayer> onlineMembers = attackerClan.getOnlineMembers();
            double money = Math.round((reward / onlineMembers.size()) * 100D) / 100D;

            for (ClanPlayer cp : onlineMembers) {
                ServerPlayer player = (ServerPlayer) cp.toPlayer();
                if (player == null) continue;

                // Nachricht senden mit Minecraft Components
                String msg = lang("player.got.money", player, money, victim.getName(), attacker.getKDR());
                player.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    @Contract("_, null -> true")
    private boolean isInvalidKill(@NotNull ServerPlayer victim, @Nullable Player attacker) {
        if (attacker == null || attacker.getUUID().equals(victim.getUUID())) {
            SimpleClans.debug("Attacker is not a player or self-kill");
            return true;
        }

        if (plugin.getSettingsManager().is(KILL_WEIGHTS_DENY_SAME_IP_KILLS)) {
            // In NeoForge Zugriff über connection
            String victimIp = victim.connection.getRemoteAddress().toString().split(":")[0];
            if (attacker instanceof ServerPlayer sAttacker) {
                String attackerIp = sAttacker.connection.getRemoteAddress().toString().split(":")[0];
                if (victimIp.equals(attackerIp)) {
                    plugin.getLogger().info("Blocked same IP kill: {} killed {}. IP: {}"+
                            attacker.getName().getString()+ victim.getName().getString()+ attackerIp);
                    return true;
                }
            }
        }

        // Eigenes Event über NeoForge Bus
        AddKillEvent addKillEvent = new AddKillEvent(
                plugin.getClanManager().getCreateClanPlayer(attacker.getUUID()),
                plugin.getClanManager().getCreateClanPlayer(victim.getUUID())
        );
        NeoForge.EVENT_BUS.post(addKillEvent);
        if (addKillEvent.isCanceled()) return true;

        PermissionsManager pm = plugin.getPermissionsManager();
        String kdrExempt = "simpleclans.other.kdr-exempt";
        return pm.has((ServerPlayer) attacker, kdrExempt) || pm.has(victim, kdrExempt);
    }

    // addKill, saveKill und calculateReward bleiben logisch gleich,
    // müssen aber ggf. auf plugin.getServer().execute() achten bei Datenbankzugriffen.

    private void addKill(Kill.Type type, ClanPlayer attacker, ClanPlayer victim) {
        // ... (Logik wie im Original)
    }

    private void saveKill(Kill kill, Kill.Type type) {
        plugin.getClanManager().addKill(kill);
        ClanPlayer killer = kill.getKiller();
        ClanPlayer victim = kill.getVictim();
        killer.addKill(type);
        plugin.getStorageManager().insertKill(killer, victim, type.getShortname(), kill.getTime());
    }

    private double calculateReward(@NotNull ClanPlayer attacker, @NotNull ClanPlayer victim) {
        // ... (Logik wie im Original)
        return 0; // Platzhalter
    }

    private boolean isNPC(Player player) {
        // In NeoForge/Minecraft prüfen wir oft auf FakePlayer oder spezifische Tags
        return player instanceof net.neoforged.neoforge.common.util.FakePlayer;
    }

    private boolean isBlacklistedWorld(Player player) {
        // Implementiere deine Welt-Blacklist Logik hier
        return false;
    }
}