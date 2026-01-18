package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class SameClanCondition extends AbstractParameterCondition<ClanPlayerInput> {
    public SameClanCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanPlayerInput> getType() {
        return ClanPlayerInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext,
                                  ClanPlayerInput value) throws InvalidCommandArgument {
        CommandIssuer issuer = context.getIssuer();
        Clan clan = Conditions.assertClanMember(clanManager, issuer);
        if (value == null || value.getClanPlayer().getClan() == null ||
                !value.getClanPlayer().getClan().equals(clan)) {
            throw new ConditionFailedException(lang("the.player.is.not.a.member.of.your.clan", (ServerPlayer) issuer));
        }
    }

    @Override
    public @NotNull String getId() {
        return "same_clan";
    }
}
