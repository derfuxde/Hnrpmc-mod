package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.TAMABLE_MOBS_SHARING;

public class TamableMobsSharing {

    private final SimpleClans plugin;
    private final SettingsManager settings;
    private final ClanManager clanManager;

    public TamableMobsSharing(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettingsManager();
        this.clanManager = plugin.getClanManager();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) return;

        if (settings.is(TAMABLE_MOBS_SHARING)) {
            // Prüfen ob das Ziel ein Wolf ist und der Angreifer ein Spieler
            if (event.getEntity() instanceof Wolf wolf && event.getSource().getEntity() instanceof Player attacker) {
                ClanPlayer cp = clanManager.getAnyClanPlayer(attacker.getUUID());
                if (cp == null || cp.getClan() == null) {
                    return;
                }

                UUID ownerUUID = wolf.getOwnerUUID();
                if (ownerUUID != null && cp.getClan().isMember(ownerUUID)) {
                    // Wolf wird friedlich gegenüber dem Clan-Mitglied
                    wolf.setPersistentAngerTarget(null);
                    wolf.setRemainingPersistentAngerTime(0);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityTarget(LivingChangeTargetEvent event) {
        if (event.isCanceled()) return;

        if (settings.is(TAMABLE_MOBS_SHARING)) {
            // Prüfen ob die Entität zähmbar ist und das Ziel ein Spieler
            if (event.getEntity() instanceof TamableAnimal tamable && event.getTargetType() instanceof Player target) {
                ClanPlayer cp = clanManager.getAnyClanPlayer(target.getUUID());
                if (cp == null || cp.getClan() == null) {
                    return;
                }

                UUID ownerUUID = tamable.getOwnerUUID();
                if (ownerUUID != null && cp.getClan().isMember(ownerUUID)) {
                    // Verhindert, dass das Tier ein Clan-Mitglied angreift
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.isCanceled()) return;

        if (settings.is(TAMABLE_MOBS_SHARING) && event.getTarget() instanceof TamableAnimal tamable) {
            Player player = event.getEntity();
            ClanPlayer cp = clanManager.getAnyClanPlayer(player.getUUID());
            if (cp == null || cp.getClan() == null) {
                return;
            }

            UUID ownerUUID = tamable.getOwnerUUID();
            if (ownerUUID != null) {
                // Bei Wölfen prüfen wir, ob sie sitzen (wie im Original)
                if (tamable instanceof Wolf wolf && !wolf.isOrderedToSit()) {
                    return;
                }

                if (cp.getClan().isMember(ownerUUID)) {
                    // Temporärer Besitzwechsel oder Interaktion erlauben
                    // Minecraft erlaubt das Setzen des Owners via UUID
                    tamable.setOwnerUUID(player.getUUID());
                }
            }
        }
    }
}