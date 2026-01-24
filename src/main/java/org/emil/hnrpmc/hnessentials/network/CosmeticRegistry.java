package org.emil.hnrpmc.hnessentials.network;

import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;

import java.util.*;

public final class CosmeticRegistry {

    private static final Map<String, CustomCosmetic> COSMETICS = new HashMap<>();

    public static void register(CustomCosmetic cosmetic) {
        COSMETICS.put(cosmetic.getId(), cosmetic);
    }

    public static CustomCosmetic get(String id) {
        Map<String, CustomCosmetic> inner = COSMETICS;
        return inner.get(id);
    }

    public static Collection<CustomCosmetic> all() {
        return COSMETICS.values();
    }
}
