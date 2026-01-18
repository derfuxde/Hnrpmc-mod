package org.emil.hnrpmc.simpleclans.events;

import org.emil.hnrpmc.simpleclans.Clan;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author NeT32
 */
public class RivalClanRemoveEvent extends Event {
    private final Clan clanFirst;
    private final Clan clanSecond;

    public RivalClanRemoveEvent(Clan clanFirst, Clan clanSecond) {
        this.clanFirst = clanFirst;
        this.clanSecond = clanSecond;
    }

    public Clan getClanFirst() {
        return this.clanFirst;
    }

    public Clan getClanSecond() {
        return this.clanSecond;
    }
}
