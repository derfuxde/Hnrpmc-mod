package org.emil.hnrpmc.hnessentials.network;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.Cosmetica;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticPosition;
import org.emil.hnrpmc.hnessentials.menu.AdminPlayerDataScreen;
import org.emil.hnrpmc.hnessentials.menu.VIPPlayerDataScreen;

import java.util.*;

public class ClientPacketHandler {

    private static final Gson GSON = new Gson();

    public static void handleCosmeticUpdate(CosmeticUpdatePayload payload, IPayloadContext context) {
        // enqueueWork sorgt dafür, dass der Code im Main-Thread von Minecraft läuft (wichtig für Rendering!)
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            // 1. Den Spieler in der Welt anhand der UUID finden
            Player player = mc.level.getPlayerByUUID(payload.playerUUID());

            if (player != null) {
                // 2. Die lokalen PlayerData für diesen Spieler holen oder erstellen
                // Hier nutzen wir dein Storage-System auf dem Client-Cache
                HNPlayerData data = HNessentials.getInstance().getStorageManager()
                        .getOrCreatePlayerData(payload.playerUUID());

                // 3. Das Cosmetic aktualisieren (Annahme: Wir wissen den Slot oder das Paket schickt ihn mit)
                // Wenn dein Paket nur ID schickt, musst du evtl. den Slot raten oder das Paket erweitern:
                data.setCosmetic(CosmeticSlot.HAT, payload.cosmeticId());

                // Falls dein Paket auch den Slot enthält (empfohlen):
                //data.setCosmetic(payload.slot(), payload.cosmeticId());

                // 4. (Optional) Dem Renderer sagen, dass er das Model neu laden soll
                // In vielen Fällen passiert das automatisch beim nächsten Frame
            }
        });
    }

    public class ClientDataCache {
        // Eine Map, die einer UUID eine weitere Map (Slot -> ID) zuordnet
        private static final Map<UUID, Map<CosmeticSlot, String>> PLAYER_COSMETICS = new HashMap<>();

        public static void updateCosmetic(UUID uuid, CosmeticSlot slot, String cosmeticId) {
            PLAYER_COSMETICS.computeIfAbsent(uuid, k -> new HashMap<>()).put(slot, cosmeticId);
            CosmeticPosition pos = CosmeticPosition.valueOf(slot.name());
            Cosmetica.api.setCosmetic(pos, cosmeticId);
        }

        public static String getCosmetic(UUID uuid, org.emil.hnrpmc.hnessentials.CosmeticSlot slot) {
            if (!PLAYER_COSMETICS.containsKey(uuid)) return "none";
            return PLAYER_COSMETICS.get(uuid).getOrDefault(slot, "none");
        }
    }

    public static void handleAdminGuiOpen(final OpenAdminScreenPayload payload, final IPayloadContext context) {
        // context.enqueueWork sorgt dafür, dass der Code im Haupt-Thread des Clients läuft
        context.enqueueWork(() -> {

            HNPlayerData data = GSON.fromJson(payload.jsonData(), HNPlayerData.class);

            if (payload.vip()) {
                Minecraft.getInstance().setScreen(new VIPPlayerDataScreen(
                        payload.targetUUID(),
                        payload.targetName(),
                        data
                ));
            } else {
                Minecraft.getInstance().setScreen(new AdminPlayerDataScreen(
                        payload.targetUUID(),
                        payload.targetName(),
                        data
                ));
            }

        });
    }
}