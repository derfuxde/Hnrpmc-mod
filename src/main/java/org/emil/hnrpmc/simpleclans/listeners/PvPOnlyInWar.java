package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PVP_ONLY_WHILE_IN_WAR;

public class PvPOnlyInWar {

    private final SimpleClans plugin;
    // Beispiel:


    public PvPOnlyInWar(SimpleClans plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDamage(LivingIncomingDamageEvent event) {
        // Falls das Event bereits abgebrochen wurde (ignoreCancelled = true Äquivalent)
        if (event.isCanceled()) return;

        // Prüfen, ob das Opfer ein Spieler ist
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }

        // Blacklist Check (Weltprüfung)
        // In NeoForge: victim.level().dimension().location().toString() nutzen
        if (isBlacklistedWorld(victim)) {
            return;
        }

        // Nutze die zuvor erstellte Utility-Klasse, um den Angreifer zu finden
        Player attacker = Events.getPlayerFromSource(event.getSource());

        if (attacker == null || victim.getUUID().equals(attacker.getUUID())) {
            return;
        }

        Clan attackerClan = plugin.getClanManager().getClanByPlayerUniqueId(attacker.getUUID());
        Clan victimClan = plugin.getClanManager().getClanByPlayerUniqueId(victim.getUUID());

        if (plugin.getSettingsManager().is(PVP_ONLY_WHILE_IN_WAR)) {
            process(event, attacker, victim, attackerClan, victimClan);
        }
    }

    private void process(LivingIncomingDamageEvent event, Player attacker, Player victim, @Nullable Clan attackerClan,
                         @Nullable Clan victimClan) {
        if (attackerClan == null || victimClan == null) {
            ChatBlock.sendMessageKey(attacker.createCommandSourceStack(), "must.be.in.clan.to.pvp", victim.getName().getString());
            event.setCanceled(true);
            return;
        }

        if (plugin.getPermissionsManager().has((ServerPlayer) victim, "simpleclans.mod.nopvpinwar")) {
            event.setCanceled(true);
            return;
        }

        if (!attackerClan.isWarring(victimClan)) {
            ChatBlock.sendMessageKey(attacker.createCommandSourceStack(), "clans.not.at.war.pvp.denied", victimClan.getName());
            event.setCanceled(true);
        }
    }

    private boolean isBlacklistedWorld(Player player) {
        // Beispiel für NeoForge Welt-Check:
        List<String> blacklist = plugin.getSettingsManager().getStringList(SettingsManager.ConfigField.SETTINGS_WORLD_BLACKLIST);
        String worldName = player.level().dimension().location().toString();
        return blacklist.contains(worldName);
    }
}