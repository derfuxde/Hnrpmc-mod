package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class SenderClanMemberCondition extends AbstractCommandCondition {

    public SenderClanMemberCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        Conditions.assertClanMember(clanManager, context.getIssuer());
    }

    @Override
    public @NotNull String getId() {
        return "clan_member";
    }
}
