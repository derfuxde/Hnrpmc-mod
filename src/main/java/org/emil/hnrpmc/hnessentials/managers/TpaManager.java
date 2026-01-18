package org.emil.hnrpmc.hnessentials.managers;

import org.emil.hnrpmc.hnessentials.Tpa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TpaManager {
    // Speichert: Empfänger-UUID -> Liste aller an ihn gerichteten TPA-Anfragen
    private final Map<UUID, List<Tpa>> activeRequests = new HashMap<>();

    public void addRequest(Tpa tpa) {
        UUID receiverId = tpa.getReceiver().getUUID();
        // Erstellt eine neue Liste, falls noch keine existiert, und fügt die TPA hinzu
        activeRequests.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(tpa);
    }

    // Holt alle Anfragen für einen Spieler
    public List<Tpa> getRequests(UUID receiverUUID) {
        return activeRequests.getOrDefault(receiverUUID, new ArrayList<>());
    }

    // Entfernt eine spezifische Anfrage (nachdem sie angenommen/abgelehnt wurde)
    public void removeRequest(Tpa tpa) {
        UUID receiverId = tpa.getReceiver().getUUID();
        List<Tpa> requests = activeRequests.get(receiverId);
        if (requests != null) {
            requests.remove(tpa);
            if (requests.isEmpty()) {
                activeRequests.remove(receiverId);
            }
        }
    }
}