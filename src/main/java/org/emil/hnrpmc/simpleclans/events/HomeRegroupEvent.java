package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;

import java.util.Collections;
import java.util.List;

public final class HomeRegroupEvent extends Event implements ICancellableEvent {

    private final Clan clan;
    private final ClanPlayer issuer;
    private final List<ServerPlayer> targets;
    private final ResourceKey<Level> level;
    private final Vec3 position;

    public HomeRegroupEvent(
            Clan clan,
            ClanPlayer issuer,
            List<ServerPlayer> targets,
            ResourceKey<Level> level,
            Vec3 position
    ) {
        this.clan = clan;
        this.issuer = issuer;
        this.targets = targets == null ? List.of() : List.copyOf(targets);
        this.level = level;
        this.position = position;
    }

    public Clan getClan() {
        return clan;
    }

    public ClanPlayer getIssuer() {
        return issuer;
    }

    public List<ServerPlayer> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    public ResourceKey<Level> getLevel() {
        return level;
    }

    public Vec3 getPosition() {
        return position;
    }
}
