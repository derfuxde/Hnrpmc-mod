package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;

import static org.emil.hnrpmc.simpleclans.SimpleClans.debug;

public class DeleteClan extends MessageListener {

    public DeleteClan(BungeeManager bungee) {
        super(bungee);
    }

    @Override
    public void accept(ByteArrayDataInput data) {
        String tag = data.readUTF();
        getClanManager().removeClan(tag);
        for (ClanPlayer cp : getClanManager().getAllClanPlayers()) {
            if (tag.equals(cp.getTag())) {
                cp.setClan(null);
                cp.setJoinDate(0);
                cp.setRank(null);
                cp.setLeader(false);
            }
        }
        debug(String.format("Deleted clan %s", tag));
    }

    @Override
    public boolean isBungeeSubchannel() {
        return false;
    }
}
