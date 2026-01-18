package org.emil.hnrpmc.simpleclans.events;

import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import net.neoforged.bus.api.Event;


/**
 *
 * @author NeT32
 */
public class PlayerDemoteEvent extends Event {

    private final Clan clan;
    private final ClanPlayer target;

    public PlayerDemoteEvent(Clan clan, ClanPlayer target) {
        this.clan = clan;
        this.target = target;
    }

    public Clan getClan() {
        return this.clan;
    }

    public ClanPlayer getClanPlayer() {
        return this.target;
    }

}
