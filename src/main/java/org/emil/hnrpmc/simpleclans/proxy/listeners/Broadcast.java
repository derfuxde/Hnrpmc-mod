package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.minecraft.network.chat.Component;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;

public class Broadcast extends MessageListener {

    public Broadcast(BungeeManager bungee) {
        super(bungee);
    }

    @Override
    public void accept(ByteArrayDataInput data) {
        String message = data.readUTF();
        bungee.getPlugin().getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(Component.literal(message)));
    }

    @Override
    public boolean isBungeeSubchannel() {
        return false;
    }
}
