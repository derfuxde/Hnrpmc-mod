package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;

import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.SimpleClans.debug;

public class DeleteClanPlayer extends MessageListener {

    public DeleteClanPlayer(BungeeManager bungee) {
        super(bungee);
    }

    @Override
    public void accept(ByteArrayDataInput data) {
        UUID uuid = UUID.fromString(data.readUTF());
        getClanManager().deleteClanPlayerFromMemory(uuid);
        debug(String.format("Deleted cp %s", uuid));
    }

    @Override
    public boolean isBungeeSubchannel() {
        return false;
    }
}
