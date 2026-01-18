package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Rank;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Minat0_
 */
public class PlayerRankUpdateEvent extends Event implements ICancellableEvent {

    private final ClanPlayer target;
    private final ClanPlayer issuer;

    private final Rank oldRank;
    private final Rank newRank;
    private final Clan clan;
    private boolean cancelled;

    public PlayerRankUpdateEvent(@NotNull ClanPlayer issuer, @NotNull ClanPlayer target, @NotNull Clan clan,
                                 @Nullable Rank oldRank, @Nullable Rank newRank) {
        this.target = target;
        this.issuer = issuer;
        this.oldRank = oldRank;
        this.clan = clan;
        this.newRank = newRank;
    }

    @Nullable
    public Rank getOldRank() {
        return oldRank;
    }

    @NotNull
    public Clan getClan() {
        return clan;
    }

    @Nullable
    public Rank getNewRank() {
        return newRank;
    }

    @NotNull
    public ClanPlayer getIssuer() {
        return issuer;
    }

    @NotNull
    public ClanPlayer getTarget() {
        return target;
    }
}
