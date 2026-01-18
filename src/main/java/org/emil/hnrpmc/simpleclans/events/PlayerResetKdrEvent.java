package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * @author ThiagoROX
 */
public class PlayerResetKdrEvent extends Event implements ICancellableEvent {

    private final ClanPlayer clanPlayer;
    private boolean cancelled;

    /**
     * Event called before a player's kill death rate is reset
     *
     * @param clanPlayer The ClanPlayer whose kill death rate going to be reset
     */
    public PlayerResetKdrEvent(@NotNull ClanPlayer clanPlayer) {
        this.clanPlayer = clanPlayer;
    }

    /**
     * Gets the player whose kill death rate going to be reset
     *
     * @return The player whose kill death rate going to be reset
     */
    @NotNull
    public ClanPlayer getClanPlayer() {
        return clanPlayer;
    }

}
