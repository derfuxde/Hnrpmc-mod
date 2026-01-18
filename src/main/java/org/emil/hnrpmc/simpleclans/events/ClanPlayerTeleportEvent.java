package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

public class ClanPlayerTeleportEvent extends Event implements ICancellableEvent {

    private boolean cancelled;
    private final ClanPlayer player;
    private final Vec3 origin;
    private final Vec3 destination;

    public ClanPlayerTeleportEvent(ClanPlayer player, Vec3 origin, Vec3 destination) {
        this.player = player;
        this.origin = origin;
        this.destination = destination;
    }

    public ClanPlayer getClanPlayer() {
        return player;
    }

    public Vec3 getOrigin() {
        return origin;
    }

    public Vec3 getDestination() {
        return destination;
    }
}
