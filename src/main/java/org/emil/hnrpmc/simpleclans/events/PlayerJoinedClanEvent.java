package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.Event; // NeoForge Base Event
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;

/**
 * @author NeT32 (Ported to NeoForge)
 */
public class PlayerJoinedClanEvent extends Event {

    private final Clan clan;
    private final ClanPlayer target;

    public PlayerJoinedClanEvent(Clan clan, ClanPlayer target) {
        this.clan = clan;
        this.target = target;
    }

    public Clan getClan() {
        return this.clan;
    }

    public ClanPlayer getClanPlayer() {
        return this.target;
    }
}