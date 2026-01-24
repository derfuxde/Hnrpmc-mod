package org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer;

import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.PlayerData;

/**
 * Something that's... playerish. Could be a player, could be a fake player.
 * Methods are amazingly named to avoid conflicts.
 */
public interface Playerish {
    /**
     * Get the player's tick count, which is kinda like a lifetime I guess.
     */
    int getLifetime();

    /**
     * Get something that could be an entity id. Then again, maybe it isn't.
     */
    int getPseudoId();

    /**
     * Gets the velocity of the player.
     * @return the velocity of this player.
     */
    Vec3 getVelocity();

    /**
     * Whether the player is sneaking.
     */
    boolean isSneaking();

    /**
     * Whether the player's nametag should be rendered 'discreetly.' When this happens, it cannot be seen through walls and is more transparent.
     * The conditions are slightly different from isSneaking for a player.
     */
    boolean renderDiscreteNametag();

    /**
     * Get the player data for this player-ish.
     * @return the player data associated with this player. Might return dummy data if still loading!
     */
    PlayerData getCosmeticaPlayerData();
}
