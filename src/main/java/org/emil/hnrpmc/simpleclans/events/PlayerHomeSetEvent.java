package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import net.neoforged.bus.api.Event;

/**
 *
 * @author NeT32
 */
public class PlayerHomeSetEvent extends Event implements ICancellableEvent {

    private boolean cancelled;
    private final Clan clan;
    private final ClanPlayer cp;
    private final Vec3 loc;

    public PlayerHomeSetEvent(Clan clan, ClanPlayer cp, Vec3 loc) {
        this.clan = clan;
        this.cp = cp;
        this.loc = loc;
    }

    public Clan getClan() {
        return this.clan;
    }

    public ClanPlayer getClanPlayer() {
        return this.cp;
    }

    public Vec3 getLocation() {
        return this.loc;
    }
}
