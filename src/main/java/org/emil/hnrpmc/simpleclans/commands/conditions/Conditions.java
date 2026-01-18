package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.MessageKeys;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class Conditions {

    public static boolean verified(ServerPlayer player) {
        var clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (clan == null) return false;
        if (!clan.isVerified()) {
            ChatBlock.sendMessage(player.createCommandSourceStack(), "simpleclans.not_verified");
            return false;
        }
        return true;
    }

    @NotNull
    public static Clan assertClanMember(@NotNull ClanManager clanManager,
                                        @NotNull CommandIssuer issuer) {
        Conditions.assertPlayer(issuer);
        Clan clan = clanManager.getClanByPlayerUniqueId(issuer.getUniqueId());
        if (clan == null) {
            SimpleClans.getInstance().getLogger().error(lang("not.a.member.of.any.clan", (ServerPlayer) issuer));
        }
        return clan;
    }

    @NotNull
    public static Player assertPlayer(@NotNull CommandIssuer issuer) {
        Player player = issuer.getIssuer();
        if (player == null) {
            SimpleClans.getInstance().getLogger().error("Diser Command ist nicht in der console erlaubt");
        }
        return player;
    }

    public static boolean rank(ClanPlayer cp, String permName) {
        return cp != null && cp.hasRankPermission(permName);
    }

    public static boolean clanMember(SimpleClans plugin, ServerPlayer player, CommandSourceStack src) {
        if (plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID()) == null) {
            //src.sendFailure(Component.literal(lang("simpleclans.not_in_clan", player)));
            return false;
        }
        return true;
    }

    public static ClanPlayer clanPlayer(ServerPlayer player) {
        if (SimpleClans.getInstance().getClanManager().getClanPlayer(player) == null) {
            return null;
        }
        return SimpleClans.getInstance().getClanManager().getClanPlayer(player);
    }

    public static boolean sameClan(SimpleClans plugin, ServerPlayer a, ServerPlayer b, CommandSourceStack src) {
        Clan ca = plugin.getClanManager().getClanByPlayerUniqueId(a.getUUID());
        Clan cb = plugin.getClanManager().getClanByPlayerUniqueId(b.getUUID());
        if (ca == null || cb == null || !ca.getTag().equals(cb.getTag())) {
            //src.sendFailure(Component.literal(lang("simpleclans.not_same_clan")));
            return false;
        }
        return true;
    }

    public static boolean different(Clan issuer, Clan other, CommandSourceStack src) {
        if (issuer.getTag().equalsIgnoreCase(other.getTag())) {
            //src.sendFailure(Component.literal(lang("simpleclans.must_be_different")));
            return false;
        }
        return true;
    }

    public static boolean alliedClan(Clan issuer, Clan other, CommandSourceStack src) {
        if (!issuer.isAlly(other.getTag())) {
            //src.sendFailure(Component.literal(lang("simpleclans.not_allied")));
            return false;
        }
        return true;
    }

    public static boolean memberFeeEnabled(SimpleClans plugin, Clan clan, CommandSourceStack src) {
        if (!clan.isMemberFeeEnabled()) {
            //src.sendFailure(Component.literal(lang("simpleclans.fee_disabled")));
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob der Spieler überhaupt in einem Clan ist.
     */
    public static Clan clan(ServerPlayer player) {
        Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (clan == null) {
            // Option A: Direkt Nachricht senden (für manuelle Checks)
            //player.sendSystemMessage(Component.literal(lang("simpleclans.not_in_clan")));
            return null;
        }
        return clan;
    }

    /**
     * Prüft, ob der Spieler der Leader des angegebenen Clans ist.
     */
    public static boolean leader(ServerPlayer player, Clan clan) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(player);
        if (cp == null || !cp.isLeader() || !cp.getClan().getTag().equals(clan.getTag())) {
            //player.sendSystemMessage(Component.literal(lang("simpleclans.not_leader")));
            return false;
        }
        return true;
    }

    /**
     * Basis-Check: Ist der Spieler verifiziert und hat er die Grundrechte?
     */
    public static boolean basic(ServerPlayer player) {
        // Prüft verifizierung (nutzt deine bestehende Methode oben)
        if (!verified(player)) return false;

        // Prüft ob er überhaupt ein ClanPlayer Objekt hat
        if (clanPlayer(player) == null) return false;

        return true;
    }

    /**
     * Prüft eine spezifische Rang-Berechtigung innerhalb des Clans.
     */
    public static boolean rankPermission(ServerPlayer player, String permName) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(player);
        if (cp == null || !cp.hasRankPermission(permName)) {
            //player.sendSystemMessage(Component.translatable("simpleclans.no_rank_permission"));
            return false;
        }
        return true;
    }

    public static boolean changeFee(SimpleClans plugin, ServerPlayer player, Clan clan, CommandSourceStack src) {
        return true;
    }

    public static boolean rivable(SimpleClans plugin, Clan issuer, CommandSourceStack src) {
        return true;
    }

    public static boolean minimumToRival(SimpleClans plugin, Clan issuer, CommandSourceStack src) {
        return true;
    }

    public static boolean minimumToAlly(SimpleClans plugin, Clan issuer, CommandSourceStack src) {
        return true;
    }

    public static boolean canWarTarget(SimpleClans plugin, ClanPlayer requester, Clan requestClan, Clan targetClan, CommandSourceStack src) {
        return true;
    }

    public static boolean permission(ServerPlayer player, String s) {
        return SimpleClans.getInstance().getPermissionsManager().has(player, s);
    }
}
