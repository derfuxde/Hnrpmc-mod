package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Minat0_
 */
public class PreCreateRankEvent extends PlayerEvent implements ICancellableEvent {

    private String rankName;
    private final Clan clan;
    private boolean cancelled;

    public PreCreateRankEvent(Player who, Clan clan, String rankName) {
        super(who);
        this.clan = clan;
        this.rankName = rankName;
    }

    @NotNull
    public Clan getClan() {
        return clan;
    }

    @NotNull
    public String getRankName() {
        return rankName;
    }

    public void setRankName(@NotNull String rankName) {
        this.rankName = rankName;
    }
}
