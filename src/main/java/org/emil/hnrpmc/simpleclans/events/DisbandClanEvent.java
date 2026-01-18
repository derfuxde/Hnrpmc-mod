package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.Clan;
import net.neoforged.bus.api.Event;
import org.emil.hnrpmc.simpleclans.SimpleClans;


/**
 *
 * @author NeT32
 */
public class DisbandClanEvent extends Event {

    private final CommandSourceStack sender;
    private final Clan clan;

    public DisbandClanEvent(CommandSourceStack sender, Clan clan) {
        if (sender == null) {
            sender = SimpleClans.getInstance().getServer().createCommandSourceStack();
        }
        this.sender = sender;
        this.clan = clan;
    }

    public Clan getClan() {
        return this.clan;
    }

    public CommandSourceStack getSender() {
        return sender;
    }

}
