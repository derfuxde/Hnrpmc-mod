package org.emil.hnrpmc.simpleclans.overlay;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.EntityType;
import org.emil.hnrpmc.hnessentials.requester.PetSkinLayer;
import org.emil.hnrpmc.hnessentials.requester.PlayerSkinLayer;

public class ClientHandler {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // Für Wölfe
        addPetLayer(event, EntityType.WOLF);
        // Für Katzen
        addPetLayer(event, EntityType.CAT);
        // Für Papageien
        addPetLayer(event, EntityType.PARROT);
        //addPetLayer(event, EntityType.PLAYER);

        //addLayerToPlayer(event, PlayerSkin.Model.WIDE);

        //addLayerToPlayer(event, PlayerSkin.Model.SLIM);
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> void addPetLayer(EntityRenderersEvent.AddLayers event, EntityType<? extends T> type) {
        LivingEntityRenderer<T, M> renderer = event.getRenderer(type);
        if (renderer != null) {
            renderer.addLayer(new PetSkinLayer<>(renderer));
        }
    }

    private static void addLayerToPlayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model modelName) {
        EntityRenderer<? extends Player> renderer = event.getSkin(modelName);
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            // Wir müssen casten, da PlayerRenderer AbstractClientPlayer nutzt
            livingRenderer.addLayer(new PlayerSkinLayer(livingRenderer));
        }
    }
}