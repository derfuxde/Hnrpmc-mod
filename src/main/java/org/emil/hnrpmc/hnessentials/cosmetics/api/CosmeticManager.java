package org.emil.hnrpmc.hnessentials.cosmetics.api;

import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.network.ClientDataCache;
import org.emil.hnrpmc.hnessentials.HNessentials;

import java.util.*;
import java.util.UUID;

public class CosmeticManager<T> {

    /**
     * Diese Methode ersetzt den Web-API Aufruf.
     * Sie entscheidet automatisch, ob sie im Cache (Client) oder in der Datei (Server) nachschaut.
     */
    public static Map<CosmeticSlot, String> getAllEquippedCosmetics(UUID playerUUID) {
        Map<CosmeticSlot, String> equipped = new HashMap<>();

        // CLIENT SEITE: Aus dem Cache lesen
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            for (CosmeticSlot slot : CosmeticSlot.values()) {
                String id = ClientDataCache.getCosmetic(playerUUID, slot);
                if (!id.equals("none")) {
                    equipped.put(slot, id);
                }
            }
            return equipped;
        }

        // SERVER SEITE: Aus dem StorageManager lesen
        var storage = HNessentials.getInstance().getStorageManager();
        if (storage != null) {
            var data = storage.getOrCreatePlayerData(playerUUID);
            for (CosmeticSlot slot : CosmeticSlot.values()) {
                String id = data.getCosmetic(slot); // Nutze deine existierende Methode
                if (id != null && !id.equals("none")) {
                    equipped.put(slot, id);
                }
            }
        }

        return equipped;
    }

    public static String getEquippedCosmetic(UUID playerUUID, CosmeticSlot slot) {
        // Wenn wir auf dem Client sind (beim Rendern)
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            return ClientDataCache.getCosmetic(playerUUID, slot);
        }

        // Wenn wir auf dem Server sind (z.B. für Admin-Logik)
        var storage = HNessentials.getInstance().getStorageManager();
        if (storage != null) {
            return storage.getOrCreatePlayerData(playerUUID).getCosmetic(slot);
        }

        return "none";
    }
}