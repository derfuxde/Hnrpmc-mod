package org.emil.hnrpmc.simpleclans.utils;

import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class VanishUtils {

    private VanishUtils() {}

    public static @NotNull List<ClanPlayer> getNonVanished(@Nullable ServerPlayer viewer, @NotNull Clan clan) {
        return getNonVanished(viewer, clan.getMembers());
    }

    public static @NotNull List<ClanPlayer> getNonVanished(@Nullable ServerPlayer viewer,
                                                           @NotNull List<ClanPlayer> clanPlayers) {
        ArrayList<ClanPlayer> nonVanished = new ArrayList<>();
        for (ClanPlayer cp : clanPlayers) {
            if (!isVanished(viewer, cp)) {
                nonVanished.add(cp);
            }
        }
        return nonVanished;
    }

    public static boolean isVanished(@Nullable ServerPlayer viewer, @NotNull ClanPlayer cp) {
        if (!isOnline(cp)) {
            return true;
        }

        ServerPlayer target = (ServerPlayer) cp.toPlayer(); // Stelle sicher: ClanPlayer#toPlayer() liefert ServerPlayer in NeoForge
        if (target == null) {
            return true;
        }

        return isVanished(viewer, target);
    }

    public static boolean isVanished(@Nullable ServerPlayer viewer, @NotNull ServerPlayer target) {
        // Wenn kein Viewer vorhanden ist (Konsole/Serverlogik), entscheide anhand "globaler" Vanish-Regel:
        // Wir übergeben viewer = target als Fallback, falls dein Service das so erwartet.
        ServerPlayer effectiveViewer = (viewer != null) ? viewer : target;

        return SimpleClans.getInstance()
                .getVanishService()
                .isVanished(effectiveViewer, target);
    }

    public static boolean isVanished(@NotNull ClanPlayer cp) {
        return isVanished((ServerPlayer) null, cp);
    }

    public static boolean isOnline(@NotNull ClanPlayer player) {
        return SimpleClans.getInstance().getProxyManager().isOnline(player.getName());
    }
}
