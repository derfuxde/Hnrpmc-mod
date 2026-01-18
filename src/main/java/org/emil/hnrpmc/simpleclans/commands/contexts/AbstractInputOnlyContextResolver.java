package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractInputOnlyContextResolver<T> extends AbstractContextResolver<T>
        implements ContextResolver<T, CommandExecutionContext<?, ?>> {

    public AbstractInputOnlyContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }
}