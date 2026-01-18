package org.emil.hnrpmc.simpleclans.commands.conditions;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCondition implements IdentifiableCondition {

    protected final SimpleClans plugin;
    protected final PermissionsManager permissionsManager;
    protected final ClanManager clanManager;
    protected final RequestManager requestManager;
    protected final SettingsManager settingsManager;

    public AbstractCondition(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
        permissionsManager = plugin.getPermissionsManager();
        clanManager = plugin.getClanManager();
        requestManager = plugin.getRequestManager();
        settingsManager = plugin.getSettingsManager();
    }
}
