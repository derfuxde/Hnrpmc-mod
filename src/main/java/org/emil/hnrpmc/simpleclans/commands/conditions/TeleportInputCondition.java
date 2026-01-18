package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

public class TeleportInputCondition extends AbstractParameterCondition<ClanInput> {

    public TeleportInputCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanInput> getType() {
        return ClanInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context, CommandExecutionContext execContext, ClanInput value) throws InvalidCommandArgument {
        new TeleportCondition(plugin).validateCondition(context, execContext, value.getClan());
    }

    @Override
    public @NotNull String getId() {
        return "can_teleport";
    }
}
