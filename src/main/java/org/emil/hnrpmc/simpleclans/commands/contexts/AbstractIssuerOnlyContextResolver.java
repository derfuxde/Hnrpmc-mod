package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractIssuerOnlyContextResolver<T> extends AbstractContextResolver<T>
        implements IssuerOnlyContextResolver<T, CommandExecutionContext<?, ?>> {

    public AbstractIssuerOnlyContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }
}