package org.emil.hnrpmc.hnessentials.network;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.emil.hnrpmc.hnessentials.HNPlayerData;

public class ClientPacketHandler {

    private static final Gson GSON = new Gson();

    public static void handleAdminGuiOpen(final OpenAdminScreenPayload payload, final IPayloadContext context) {
        // context.enqueueWork sorgt dafür, dass der Code im Haupt-Thread des Clients läuft
        context.enqueueWork(() -> {

            // Falls du die Daten als JSON-String schickst (empfohlen):
            HNPlayerData data = GSON.fromJson(payload.jsonData(), HNPlayerData.class);

            // Falls du die Daten direkt schickst, nimmst du einfach payload.data()
            // HNPlayerData data = payload.data();

            // Öffne den Screen
            Minecraft.getInstance().setScreen(new org.emil.hnrpmc.hnessentials.menu.AdminPlayerDataScreen(
                    payload.targetUUID(),
                    payload.targetName(),
                    data
            ));
        });
    }
}