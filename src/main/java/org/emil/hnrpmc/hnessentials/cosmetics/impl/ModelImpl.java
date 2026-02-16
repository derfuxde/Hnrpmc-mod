package org.emil.hnrpmc.hnessentials.cosmetics.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Box;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.api.User;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.CosmeticModelLoader;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.Yootil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.neoforged.fml.loading.FMLEnvironment.dist;

class ModelImpl implements Model {
    ModelImpl(CosmeticType<?> type, String id, String name, int flags, Box bounds,
              String model, String base64Texture, User owner, long uploadTime, boolean usesUVRotations) {
        this.id = id;
        this.flags = flags;
        this.bounds = bounds;

        this.model = model;
        this.texture = base64Texture;
        this.owner = owner;
        this.uploadTime = uploadTime;
        this.usesUVRotations = usesUVRotations;
        this.type = type;
        this.name = name;
        this.showHemlet = true;
    }

    ModelImpl(CosmeticType<?> type, String id, String name, int flags, Box bounds,
              String model, String base64Texture, User owner, long uploadTime, boolean usesUVRotations, boolean showHemlet) {
        this.id = id;
        this.flags = flags;
        this.bounds = bounds;

        this.model = model;
        this.texture = base64Texture;
        this.owner = owner;
        this.uploadTime = uploadTime;
        this.usesUVRotations = usesUVRotations;
        this.type = type;
        this.name = name;
        this.showHemlet = showHemlet;
    }

    private final String id;
    private final int flags;
    private final Box bounds;

    private final String model;
    private final String texture;
    private final User owner;
    private final long uploadTime;
    private final boolean usesUVRotations;
    private final CosmeticType<?> type;
    private final String name;
    private final boolean showHemlet;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int flags() {
        return this.flags;
    }

    @Override
    public int getFrameDelay() {
        return 50 * ((this.flags >> 4) & 0x1F);
    }

    @Override
    public Box getBoundingBox() {
        return this.bounds;
    }

    @Override
    public String getModel() {
        return this.model;
    }

    @Override
    public String getTexture() {
        return texture;
    }

    @Override
    public User getOwner() {
        return this.owner;
    }

    public boolean showHelmet() {
        return this.showHemlet;
    }

    @Override
    public long getUploadTime() {
        return this.uploadTime;
    }

    @Override
    public boolean usesUVRotations() {
        return this.usesUVRotations;
    }

    @Override
    public boolean isBuiltin() {
        return this.id.charAt(0) == '-';
    }

    @Override
    public CosmeticType<?> getType() {
        return this.type;
    }

    @Override
    public Cosmetic getasCosmetic() {
        BakableModel model = null;
        if (dist == Dist.CLIENT) {
            model = CosmeticModelLoader.createBakableModel(this.getId(), this.getName());
        }

        return new Cosmetic(
                this.getId(),
                type.getAssociatedSlot(),
                ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetics/" + this.getId()),
                Component.literal(this.getName()),
                model
        );
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelImpl that = (ModelImpl) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    static Optional<Model> parse(@Nullable JsonObject json) {
        return json == null ? Optional.empty() : Optional.of(_parse(json));
    }

    static Model _parse(JsonObject json) {
        String id = json.has("id") ? json.get("id").getAsString() : "unknown";
        String name = json.has("name") ? json.get("name").getAsString() : "Unknown Cosmetic";
        int flags = json.get("extraInfo").getAsInt();
        JsonArray unparsedBounds = json.get("bounds").getAsJsonArray();
        JsonArray lowerBounds = unparsedBounds.get(0).getAsJsonArray();
        JsonArray upperBounds = unparsedBounds.get(1).getAsJsonArray();

        Box bounds = new Box(
                lowerBounds.get(0).getAsInt(),
                lowerBounds.get(1).getAsInt(),
                lowerBounds.get(2).getAsInt(),
                upperBounds.get(0).getAsInt(),
                upperBounds.get(1).getAsInt(),
                upperBounds.get(2).getAsInt());

        return new ModelImpl(
                CosmeticType.fromTypeString(json.get("type").getAsString()).get(),
                id,
                json.get("name").getAsString(),
                flags,
                bounds,
                json.get("model").getAsString(),
                json.get("texture").getAsString(),
                new User(Yootil.toUUID(json.get("owner").getAsString()), json.get("ownerName").getAsString()),
                json.get("uploaded").getAsLong(),
                json.get("usesUvRotations").getAsBoolean(),
                json.get("showHelmet").getAsBoolean()
        );
    }
}
