package org.emil.hnrpmc.simpleclans.overlay;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.emil.hnrpmc.hnessentials.requester.GoldWolfLayer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.EntityType;

public class ClientHandler {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        var renderer = event.getRenderer(EntityType.WOLF);
        if (renderer instanceof WolfRenderer wolfRenderer) {
            // Hier fügst du dein Layer hinzu
            wolfRenderer.addLayer(new GoldWolfLayer(wolfRenderer));
        }
    }
}