package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Rank;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Minat0_
 */
public class CreateRankEvent extends PlayerEvent {

    private final Rank rank;
    private final Clan clan;

    public CreateRankEvent(Player who, Clan clan, Rank rank) {
        super(who);
        this.clan = clan;
        this.rank = rank;
    }

    @NotNull
    public Rank getRank() {
        return rank;
    }

    @NotNull
    public Clan getClan() {
        return clan;
    }
}
