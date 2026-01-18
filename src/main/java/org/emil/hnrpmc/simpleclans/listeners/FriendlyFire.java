package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.GLOBAL_FRIENDLY_FIRE;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.SAFE_CIVILIANS;

public final class FriendlyFire {

    private final SimpleClans plugin;

    private final Map<UUID, Long> warned = new HashMap<>();
    private static final long WARN_DELAY_MS = 10_000L;

    public FriendlyFire(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }

        if (isBlacklistedWorld(victim)) {
            return;
        }

        ServerPlayer attacker = getAttacker(event);
        if (attacker == null) {
            return;
        }

        if (attacker.getUUID().equals(victim.getUUID())) {
            return;
        }

        ClanPlayer vcp = plugin.getClanManager().getClanPlayer(victim.getUUID());

        Clan victimClan = (vcp == null) ? null : vcp.getClan();
        Clan attackerClan = plugin.getClanManager().getClanByPlayerUniqueId(attacker.getUUID());

        process(event, attacker, vcp, victimClan, attackerClan);
    }

    private void process(LivingIncomingDamageEvent event,
                         ServerPlayer attacker,
                         @Nullable ClanPlayer vcp,
                         @Nullable Clan victimClan,
                         @Nullable Clan attackerClan) {

        if (vcp == null || victimClan == null || attackerClan == null) {
            if (plugin.getSettingsManager().is(SAFE_CIVILIANS)) {
                ChatBlock.sendMessageKey(attacker.createCommandSourceStack(), "cannot.attack.civilians");
                event.setCanceled(true);
            }
            return;
        }

        if (vcp.isFriendlyFire()
                || victimClan.isFriendlyFire()
                || plugin.getSettingsManager().is(GLOBAL_FRIENDLY_FIRE)) {
            return;
        }

        if (victimClan.equals(attackerClan)) {
            warn(attacker, "cannot.attack.clan.member");
            event.setCanceled(true);
            return;
        }

        if (victimClan.isAlly(attackerClan.getTag())) {
            warn(attacker, "cannot.attack.ally");
            event.setCanceled(true);
        }
    }

    private void warn(ServerPlayer attacker, String messageKey) {
        long last = warned.getOrDefault(attacker.getUUID(), 0L);
        long now = System.currentTimeMillis();

        if (last + WARN_DELAY_MS <= now) {
            ChatBlock.sendMessageKey(attacker.createCommandSourceStack(), messageKey);
            warned.put(attacker.getUUID(), now);
        }
    }

    @Nullable
    private static ServerPlayer getAttacker(LivingIncomingDamageEvent event) {
        var src = event.getSource();
        if (src == null) return null;

        var direct = src.getDirectEntity();
        if (direct instanceof ServerPlayer sp) return sp;

        var owner = src.getEntity();
        if (owner instanceof ServerPlayer sp) return sp;

        return null;
    }

    private boolean isBlacklistedWorld(ServerPlayer player) {
        // Wenn du vorher Worlds blacklistest: in NeoForge z.B. über Dimension Key prüfen.
        // Beispiel: player.level().dimension().location().toString()
        // Hier stubben wir auf "false", bis du deine alte Logik portierst.
        return false;
    }
}
