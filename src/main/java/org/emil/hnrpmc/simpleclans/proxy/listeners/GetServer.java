package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;

public class GetServer extends MessageListener {

    public GetServer(BungeeManager bungee) {
        super(bungee);
    }

    @Override
    public void accept(ByteArrayDataInput data) {
        String name = data.readUTF();
        bungee.setServerName(name);
        SimpleClans.debug(String.format("Server name: %s", name));
    }

    @Override
    public boolean isBungeeSubchannel() {
        return true;
    }
}
