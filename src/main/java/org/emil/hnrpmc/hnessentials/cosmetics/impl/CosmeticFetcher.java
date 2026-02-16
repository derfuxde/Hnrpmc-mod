package org.emil.hnrpmc.hnessentials.cosmetics.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.*;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.Yootil;
import org.emil.hnrpmc.hnessentials.network.CosmeticRegistry;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class to hide that my solution to stopping code duplication is to literally just store a copy of {@link CosmeticaWebAPI} internally.
 */
public final class CosmeticFetcher {
    private CosmeticFetcher() {
        // NO-OP, prevent construction of instances
        // kinda useless but it's "goOd prACTise".
    }

    private static final CosmeticaAPI API = CosmeticaWebAPI.newUnauthenticatedInstance();

    @Nullable
    public static CustomCape getCape(String id) {
        CustomCosmetic CC = CosmeticRegistry.get(id);
        if (CC.getType() != CosmeticType.CAPE) return null;
        var result = API.getCosmetic(CC.getType(), id).getOrNull();

        // Prüfe vor dem Casten, ob es wirklich ein Cape ist
        if (result instanceof CustomCape cape) {
            return cape;
        }

        // Falls es nur ein SimpleCosmetic ist, müssen wir es anders behandeln
        return null;
    }

    @Nullable

    public static Model getModel(CosmeticType<?> type, String id) {

        CustomCosmetic CC = API.getCosmetic(type, id).getOrNull();

        if (CC == null) return null;



        JsonObject JO = loadModelAsJson(CC.getasCosmetic().getModel());



        double maxY = 16.0; // Standardhöhe

        if (JO != null && JO.has("elements")) {

            JsonArray elements = JO.getAsJsonArray("elements");

            for (int i = 0; i < elements.size(); i++) {

                JsonObject element = elements.get(i).getAsJsonObject();

                JsonArray to = element.getAsJsonArray("to");

                double elementY = to.get(1).getAsDouble();

                if (elementY > maxY) maxY = elementY;

            }

        }



        Box headBounds = new Box(-8, 0, -8, 8, 60, 8);



        String modelJsonContent = JO != null ? JO.toString() : "";



        String texturePath = getLocalTextureAsBase64(CC.getasCosmetic().getModel());



        Model model = new ModelImpl( type, id, CC.getName(), 0x1, headBounds, modelJsonContent, texturePath, CC.getOwner(), CC.getUploadTime(), false, CC.showHelmet());

        return model;

    }

    public static String getLocalTextureAsBase64(ResourceLocation location) {
        try {
            ResourceLocation fullPath = location.withPrefix("textures/").withSuffix(".png");

            var resourceManager = Minecraft.getInstance().getResourceManager();
            var resource = resourceManager.getResource(fullPath);

            if (resource.isPresent()) {
                try (InputStream is = resource.get().open()) {
                    byte[] bytes = is.readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    return "data:image/png;base64," + base64;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // Fallback
    }

    public static JsonObject loadModelAsJson(ResourceLocation location) {
        try {
            // Baue den Pfad korrekt zusammen (Prefix "models/" und Suffix ".json")
            ResourceLocation fullPath = location.withPrefix("models/").withSuffix(".json");

            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(fullPath);

            if (resource.isPresent()) {
                try (Reader reader = resource.get().openAsReader()) {
                    // Hier wird die Datei direkt in ein JsonObject geparst
                    return JsonParser.parseReader(reader).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Oder ein leeres JsonObject: new JsonObject()
    }
}
