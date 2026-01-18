package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.War;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

public class WarStartEvent extends Event implements ICancellableEvent {

    private final War war;
    private boolean cancel;

    public WarStartEvent(@NotNull War war) {
        this.war = war;
    }

    @NotNull
    public War getWar() {
        return war;
    }

}
