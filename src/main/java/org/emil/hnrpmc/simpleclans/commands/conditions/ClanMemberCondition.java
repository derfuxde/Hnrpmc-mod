package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class ClanMemberCondition extends AbstractParameterCondition<ClanPlayer> {

    public ClanMemberCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanPlayer> getType() {
        return ClanPlayer.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext,
                                  ClanPlayer value) throws InvalidCommandArgument {

        // context.getIssuer() liefert jetzt korrekt den CommandIssuer zurück
        Conditions.assertClanMember(plugin.getClanManager(), context.getIssuer());
    }

    @Override
    public @NotNull String getId() {
        return "clan_member";
    }
}