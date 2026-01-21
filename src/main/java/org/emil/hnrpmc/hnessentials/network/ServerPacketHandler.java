package org.emil.hnrpmc.hnessentials.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;

public class ServerPacketHandler {

    public static void handleAdminUpdate(final AdminUpdateDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Der Spieler, der das Paket gesendet hat (der Admin)
            ServerPlayer admin = (ServerPlayer) context.player();

            // Sicherheitscheck: Hat der Absender überhaupt Admin-Rechte?
            if (!admin.hasPermissions(2)) {
                admin.sendSystemMessage(Component.literal("§cDu hast keine Berechtigung, Daten zu ändern!"));
                return;
            }

            // Die Daten des Ziel-Spielers laden
            HNessentials plugin = HNessentials.getInstance();
            HNPlayerData targetData = plugin.getStorageManager().getOrCreatePlayerData(payload.target());

            if (targetData == null) {
                admin.sendSystemMessage(Component.literal("§cFehler: Spielerdaten konnten nicht gefunden werden."));
                return;
            }

            // Feld basierend auf dem Paket-Inhalt aktualisieren
            String field = payload.field().toLowerCase();
            String value = payload.value();

            try {
                switch (field) {
                    case "money" -> targetData.setMoney(Double.parseDouble(value));
                    case "godmode" -> targetData.setGodMode(Boolean.parseBoolean(value));
                    case "muted" -> targetData.setMuted(Boolean.parseBoolean(value));
                    //case "cosmetic" -> targetData.setActiveCosmetic(value);
                    default -> {
                        admin.sendSystemMessage(Component.literal("§cUnbekanntes Feld: " + field));
                        return;
                    }
                }

                // WICHTIG: Die geänderten Daten im StorageManager speichern (schreibt in die JSON)
                plugin.getStorageManager().setPlayerData(payload.target(), targetData);

                // Bestätigung an Admin
                admin.sendSystemMessage(Component.literal("§aErfolgreich aktualisiert: §e" + field + " §f= §7" + value));

            } catch (Exception e) {
                admin.sendSystemMessage(Component.literal("§cFehler beim Verarbeiten des Wertes: " + e.getMessage()));
            }
        });
    }
}