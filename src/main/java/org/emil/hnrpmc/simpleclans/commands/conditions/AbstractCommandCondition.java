package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandConditions;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommandCondition extends AbstractCondition implements CommandConditions.Condition<CommandIssuer> {
    public AbstractCommandCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    public abstract void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument;
}
