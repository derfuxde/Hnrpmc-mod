package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

public class AddKillEvent extends Event implements ICancellableEvent {
    private boolean cancelled = false;
    private final ClanPlayer victim;
    private final ClanPlayer attacker;

    public AddKillEvent(@NotNull ClanPlayer attacker, @NotNull ClanPlayer victim) {
        this.attacker = attacker;
        this.victim = victim;
    }

    public ClanPlayer getAttacker() {
        return attacker;
    }

    public ClanPlayer getVictim() {
        return victim;
    }
}
