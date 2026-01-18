package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandConditions;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractParameterCondition<T> extends AbstractCondition implements
        CommandConditions.ParameterCondition<T, CommandExecutionContext, CommandIssuer> {

    public AbstractParameterCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    public abstract Class<T> getType();

    @Override
    public abstract void validateCondition(
            ConditionContext<CommandIssuer> context,
            CommandExecutionContext execContext,
            T value
    ) throws InvalidCommandArgument;
}
