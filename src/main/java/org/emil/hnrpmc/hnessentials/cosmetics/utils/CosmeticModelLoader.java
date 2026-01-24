package org.emil.hnrpmc.hnessentials.cosmetics.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Box;
import java.io.Reader;
import java.util.*;

public class CosmeticModelLoader {

    private static final Map<String, Map<String, ResourceLocation>> MATERIAL_MAP = new HashMap<>();

    public static void registerMaterial(String modelId, String textureId, ResourceLocation location) {
        MATERIAL_MAP.computeIfAbsent(modelId, k -> new HashMap<>()).put(textureId, location);
    }

    public static ResourceLocation getRegisteredMaterial(String modelId, String textureId) {
        if (MATERIAL_MAP.containsKey(modelId)) {
            return MATERIAL_MAP.get(modelId).get(textureId);
        }
        return null;
    }

    public static BakableModel createBakableModel(String id, String name) {
        // Der Pfad zu deinem Model-JSON in src/main/resources/assets/hnrpmc/models/cosmetics/
        ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath("hnrpmc", "models/cosmetics/" + id + ".json");
        // Der Pfad zur Textur
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath("hnrpmc", "textures/cosmetics/" + id + ".png");

        try {
            var manager = Minecraft.getInstance().getResourceManager();
            var resource = manager.getResource(modelLocation);

            if (resource.isPresent()) {
                try (Reader reader = resource.get().openAsReader()) {
                    // Wandelt das JSON in ein BlockModel um
                    BlockModel blockModel = BlockModel.fromStream(reader);

                    return new BakableModel(
                            id,
                            name,
                            blockModel,
                            textureLocation,
                            0, // extraInfo
                            new Box(0, 0, 0, 16, 16, 16) // Standard-Größe
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Cosmetic-Models: " + id);
            e.printStackTrace();
        }

        return null; // Oder ein Default-Model
    }
}