package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.jetbrains.annotations.NotNull;

/**
 * @author RoinujNosde (Ported to NeoForge)
 */
public class ComponentClickEvent extends PlayerEvent implements ICancellableEvent {

    private final SCFrame frame;
    private final SCComponent component;

    public ComponentClickEvent(@NotNull Player who, @NotNull SCFrame frame, @NotNull SCComponent component) {
        super(who);
        this.frame = frame;
        this.component = component;
    }

    public @NotNull SCFrame getFrame() {
        return frame;
    }

    public @NotNull SCComponent getComponent() {
        return component;
    }

    // NeoForge nutzt intern setCanceled() und isCanceled() durch das Interface ICancellableEvent.
    // Du musst hier keine eigene 'cancelled' Variable mehr definieren!
}