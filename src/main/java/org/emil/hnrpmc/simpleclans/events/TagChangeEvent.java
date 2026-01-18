package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class TagChangeEvent extends PlayerEvent implements ICancellableEvent {

    private final @NotNull Clan clan;
    private @NotNull String newTag;
    private boolean cancelled;

    public TagChangeEvent(@NotNull Player player, @NotNull Clan clan, @NotNull String newTag) {
        super(player);
        this.clan = clan;
        this.newTag = newTag;
    }

    public @NotNull Clan getClan() {
        return clan;
    }

    public @NotNull String getNewTag() {
        return newTag;
    }

    public void setNewTag(@NotNull String newTag) {
        this.newTag = newTag;
    }
}
