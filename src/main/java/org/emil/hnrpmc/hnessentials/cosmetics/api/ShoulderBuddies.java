package org.emil.hnrpmc.hnessentials.cosmetics.api;

import java.util.Optional;

/**
 * Data class which stores the player's shoulder buddies.
 */
public interface ShoulderBuddies {
    /**
     * @return the player's left shoulder buddy.
     */
    Optional<Model> getLeft();

    /**
     * @return the player's right shoulder buddy.
     */
    Optional<Model> getRight();
}
