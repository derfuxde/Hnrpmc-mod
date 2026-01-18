package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PreCreateClanEvent extends PlayerEvent implements ICancellableEvent {
    private boolean cancelled;
    private final String tag;
    private final String name;

    public PreCreateClanEvent(@NotNull Player who, @NotNull String tag, @NotNull String name) {
        super(who);
        this.tag = tag;
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

}
