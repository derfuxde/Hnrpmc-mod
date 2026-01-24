package org.emil.hnrpmc.hnessentials.cosmetics.impl;

import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.api.ShoulderBuddies;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ShoulderBuddiesImpl implements ShoulderBuddies {
    public ShoulderBuddiesImpl(Optional<Model> left, Optional<Model> right) {
        this.left = left;
        this.right = right;
    }

    private final Optional<Model> left;
    private final Optional<Model> right;

    /**
     * @return the player's left shoulder buddy.
     */
    @Nullable
    public Optional<Model> getLeft() {
        return this.left;
    }

    /**
     * @return the player's right shoulder buddy.
     */
    @Nullable
    public Optional<Model> getRight() {
        return this.right;
    }
}
