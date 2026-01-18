package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class Events {

    private Events() {
        // Can't instantiate this class
    }

    /**
     * Extrahiert den angreifenden Spieler aus einem Schadensevent.
     * In NeoForge arbeiten wir meist mit LivingDamageEvent.
     */
    @Nullable
    @Contract("null -> null")
    public static Player getAttacker(@Nullable LivingDamageEvent event) {
        if (event == null) return null;

        DamageSource source = event.getEntity().getLastDamageSource();

        // getEntity() liefert in Minecraft meist den "Urheber" (Schütze, Werfer, etc.)
        // Bei Nahkampf ist es direkt der Angreifer.
        Entity attacker = source.getEntity();

        if (attacker instanceof Player player) {
            return player;
        }

        return null;
    }

    /**
     * Falls du nur die DamageSource hast (universeller einsetzbar):
     */
    @Nullable
    public static Player getPlayerFromSource(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player) {
            return player;
        }
        return null;
    }
}