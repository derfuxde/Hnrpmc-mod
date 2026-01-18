package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.minecraft.network.chat.Component;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;
import net.minecraft.world.entity.player.Player;

public class Message extends MessageListener {

    public Message(BungeeManager bungee) {
        super(bungee);
    }

    @Override
    public void accept(ByteArrayDataInput data) {
        String target = data.readUTF();
        String message = data.readUTF();

        Player player = bungee.getPlugin().getServer().getPlayerList().getPlayerByName(target);
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public boolean isBungeeSubchannel() {
        return false;
    }
}
