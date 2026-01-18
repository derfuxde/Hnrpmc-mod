package org.emil.hnrpmc.simpleclans.commands.contexts;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractContextResolver<T> {

    protected final @NotNull SimpleClans plugin;
    protected final @NotNull ClanManager clanManager;

    public AbstractContextResolver(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
        clanManager = plugin.getClanManager();
    }

    public abstract Class<T> getType();
}
