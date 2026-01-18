package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Rank;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * @author Minat0_
 */
public class DeleteRankEvent extends PlayerEvent implements ICancellableEvent {

    private final Rank rank;
    private final Clan clan;
    private boolean cancelled;

    public DeleteRankEvent(Player who, Clan clan, Rank rank) {
        super(who);
        this.rank = rank;
        this.clan = clan;
    }

    public Rank getRank() {
        return rank;
    }

    public Clan getClan() {
        return clan;
    }

}
