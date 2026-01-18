package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.gson.Gson;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;

public abstract class MessageListener {

    protected final BungeeManager bungee;

    public MessageListener(BungeeManager bungee) {
        this.bungee = bungee;
    }

    public abstract void accept(ByteArrayDataInput data);

    public abstract boolean isBungeeSubchannel();

    protected ClanManager getClanManager() {
        return SimpleClans.getInstance().getClanManager();
    }

}
