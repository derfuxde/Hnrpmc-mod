package org.emil.hnrpmc.simpleclans.events;

import org.emil.hnrpmc.simpleclans.War;
import org.jetbrains.annotations.NotNull;
import net.neoforged.bus.api.Event;

public class WarEndEvent extends Event {

    private final War war;
    private final Reason reason;

    public WarEndEvent(@NotNull War war, @NotNull Reason reason) {
        this.war = war;
        this.reason = reason;
    }

    @NotNull
    public War getWar() {
        return war;
    }

    @NotNull
    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        REQUEST, EXPIRATION
    }
}
