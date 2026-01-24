package org.emil.hnrpmc.hnessentials.network;

import org.emil.hnrpmc.hnessentials.CosmeticSlot;

import java.util.HashMap;
import java.util.*;

public class ClientDataCache {
    // Eine Map, die einer UUID eine weitere Map (Slot -> ID) zuordnet
    private static final Map<UUID, Map<CosmeticSlot, String>> PLAYER_COSMETICS = new HashMap<>();

    public static void updateCosmetic(UUID uuid, CosmeticSlot slot, String cosmeticId) {
        PLAYER_COSMETICS.computeIfAbsent(uuid, k -> new HashMap<>()).put(slot, cosmeticId);
    }

    public static String getCosmetic(UUID uuid, CosmeticSlot slot) {
        if (!PLAYER_COSMETICS.containsKey(uuid)) return "none";
        return PLAYER_COSMETICS.get(uuid).getOrDefault(slot, "none");
    }
}