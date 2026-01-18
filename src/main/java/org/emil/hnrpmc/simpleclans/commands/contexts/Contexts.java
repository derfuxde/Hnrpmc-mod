package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static co.aikar.commands.MessageKeys.*;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class Contexts {

    private Contexts() {
    }

    @NotNull
    public static Clan assertClanMember(@NotNull ClanManager clanManager,
                                        @NotNull CommandIssuer issuer) {
        assertPlayer(issuer);
        Clan clan = clanManager.getClanByPlayerUniqueId(issuer.getUniqueId());
        if (clan == null) {
            throw new InvalidCommandArgument(lang("not.a.member.of.any.clan", (ServerPlayer) issuer), false);
        }
        return clan;
    }

    @NotNull
    public static Player assertPlayer(@NotNull CommandIssuer issuer) {
        Player player = issuer.getIssuer();
        if (player == null) {
            throw new InvalidCommandArgument(NOT_ALLOWED_ON_CONSOLE, false);
        }
        return player;
    }

    public static void validateMinMax(Number val, Number minValue, Number maxValue) throws InvalidCommandArgument {
        if (maxValue != null && val.doubleValue() > maxValue.doubleValue()) {
            throw new InvalidCommandArgument(PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(maxValue));
        }
        if (minValue != null && val.doubleValue() < minValue.doubleValue()) {
            throw new InvalidCommandArgument(PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(minValue));
        }
    }
}
