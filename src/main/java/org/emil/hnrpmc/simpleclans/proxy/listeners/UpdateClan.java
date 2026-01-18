package org.emil.hnrpmc.simpleclans.proxy.listeners;

import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;
import org.jetbrains.annotations.Nullable;

public class UpdateClan extends Update<Clan> {

    public UpdateClan(BungeeManager bungee) {
        super(bungee);
    }

    @Override
    public boolean isBungeeSubchannel() {
        return false;
    }

    @Override
    protected Class<Clan> getType() {
        return Clan.class;
    }

    @Override
    protected @Nullable Clan getCurrent(Clan clan) {
        return getClanManager().getClan(clan.getTag());
    }

    @Override
    protected void insert(Clan clan) {
        getClanManager().importClan(clan);
    }

}
