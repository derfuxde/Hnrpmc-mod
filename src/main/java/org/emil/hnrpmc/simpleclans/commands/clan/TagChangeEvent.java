package org.emil.hnrpmc.simpleclans.commands.clan;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.Clan;

public final class TagChangeEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final Clan clan;
    private String newTag;

    public TagChangeEvent(ServerPlayer player, Clan clan, String newTag) {
        this.player = player;
        this.clan = clan;
        this.newTag = newTag;
    }

    public ServerPlayer getPlayer() { return player; }
    public Clan getClan() { return clan; }
    public String getNewTag() { return newTag; }
    public void setNewTag(String newTag) { this.newTag = newTag; }
}
