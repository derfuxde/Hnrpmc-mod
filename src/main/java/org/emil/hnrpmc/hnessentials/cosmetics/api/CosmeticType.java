package org.emil.hnrpmc.hnessentials.cosmetics.api;


import org.emil.hnrpmc.hnessentials.CosmeticSlot;

import java.util.Optional;

/**
 * A set of types of cosmetics.
 */
public class CosmeticType<T extends CustomCosmetic> {
    private final String typeString;
    private final String urlString;
    private final CosmeticSlot associatedSlot; // Neue Verknüpfung

    private CosmeticType(String typeString, String urlstring, CosmeticSlot slot) {
        this.typeString = typeString;
        this.urlString = urlstring;
        this.associatedSlot = slot;
    }

    public CosmeticSlot getAssociatedSlot() {
        return associatedSlot;
    }

    public static final CosmeticType<CustomCape> CAPE = new CosmeticType<>("Cape", "cape", CosmeticSlot.CAPE); // Oder eigener CAPE Slot
    public static final CosmeticType<Model> HAT = new CosmeticType<>("Hat", "hat", CosmeticSlot.HAT);
    public static final CosmeticType<Model> SHOULDER_BUDDY = new CosmeticType<>("Shoulder Buddy", "shoulderbuddy", CosmeticSlot.SHOULDER_BUDDY);
    public static final CosmeticType<Model> BACK_BLING = new CosmeticType<>("Back Bling", "backbling", CosmeticSlot.BACK_BLING);

    /**
     * Gets the cosmetic type instance from the case-sensitive url string.
     * @param urlstring the case-sensitive string associated with this cosmetic in api urls.
     * @return the type associated with this string. Returns Optional.empty() if none.
     */
    public static Optional<CosmeticType<?>> fromUrlString(String urlstring) {
        switch (urlstring) {
            case "cape":
                return Optional.of(CAPE);
            case "hat":
                return Optional.of(HAT);
            case "shoulderbuddy":
                return Optional.of(SHOULDER_BUDDY);
            case "backbling":
                return Optional.of(BACK_BLING);
            default:
                return Optional.empty();
        }
    }

    public String getUrlString() {
        return this.urlString;
    }

    /**
     * Gets the cosmetic type instance from the case-sensitive type string.
     * @param typeString the case-sensitive string associated with this cosmetic in api responses.
     * @return the type associated with this string. Returns Optional.empty() if none.
     */
    public static Optional<CosmeticType<?>> fromTypeString(String typeString) {
        switch (typeString) {
            case "Cape":
                return Optional.of(CAPE);
            case "Hat":
                return Optional.of(HAT);
            case "Shoulder Buddy":
                return Optional.of(SHOULDER_BUDDY);
            case "Back Bling":
                return Optional.of(BACK_BLING);
            default:
                return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return this.typeString;
    }
}
