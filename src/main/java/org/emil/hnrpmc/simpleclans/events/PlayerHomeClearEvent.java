package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.jetbrains.annotations.NotNull;
import net.neoforged.bus.api.Event;

public class PlayerHomeClearEvent extends Event implements ICancellableEvent {

    private final Clan clan;
    private final ClanPlayer cp;
    private boolean cancelled;

    public PlayerHomeClearEvent(Clan clan, ClanPlayer cp) {
        this.clan = clan;
        this.cp = cp;
    }

    public Clan getClan() {
        return clan;
    }

    public ClanPlayer getCp() {
        return cp;
    }


}
