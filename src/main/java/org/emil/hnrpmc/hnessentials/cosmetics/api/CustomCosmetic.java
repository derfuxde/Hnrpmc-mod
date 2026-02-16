package org.emil.hnrpmc.hnessentials.cosmetics.api;

import org.emil.hnrpmc.hnessentials.Cosmetic;

/**
 * Represents a cosmetic stored on cosmetica servers. This could be either a cape or model.
 */
public interface CustomCosmetic {
    /**
     * Gets the type of cosmetic this is.
     * @return the cosmetic type.
     */
    CosmeticType<?> getType();

    Cosmetic getasCosmetic();

    /**
     * Gets the owner of this cosmetic.
     * @return the owner of this cosmetic. Can be a hyphenless UUID of a player, or "system" (e.g. for the starter capes).
     */
    User getOwner();

    boolean showHelmet();

    /**
     * Get the upload time of this cosmetic.
     * @return the UTC unix timestamp, in seconds, at which this model was uploaded.
     */
    long getUploadTime();

    /**
     * Gets the name of this cosmetic.
     * @return the name of this cosmetic.
     */
    String getName();

    /**
     * Gets the id of this cosmetic.
     * @return the id of this cosmetic.
     * @apiNote It is useful to use this field along with caching to ensure each model is only built once on the application end.
     */
    String getId();
}
