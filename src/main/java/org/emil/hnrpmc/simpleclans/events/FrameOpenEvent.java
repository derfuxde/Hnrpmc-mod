package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.jetbrains.annotations.NotNull;

/**
 * @author RoinujNosde (Ported to NeoForge)
 */
public class FrameOpenEvent extends PlayerEvent implements ICancellableEvent {

    private final @NotNull SCFrame frame;

    public FrameOpenEvent(@NotNull Player viewer, @NotNull SCFrame frame) {
        super(viewer);
        this.frame = frame;
    }

    public @NotNull SCFrame getFrame() {
        return frame;
    }

}