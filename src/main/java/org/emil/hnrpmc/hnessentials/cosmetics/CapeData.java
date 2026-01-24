package org.emil.hnrpmc.hnessentials.cosmetics;


import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class CapeData {
    public CapeData(@Nullable ResourceLocation image, String capeName, String capeId, boolean thirdPartyCape, String origin) {
        this.image = image;
        this.capeName = capeName;
        this.capeId = capeId;
        this.thirdPartyCape = thirdPartyCape;
        this.origin = origin;
    }

    private final @Nullable ResourceLocation image;
    private final String capeName;
    private final String capeId;
    private final boolean thirdPartyCape;
    private final String origin;
    private PlayerSkin skinCache;

    public static final CapeData NO_CAPE = new CapeData(null, "", "none", false, "");

    public ResourceLocation getImage() {
        return CustomLayer.CAPE_OVERRIDER.get(() -> this.image);
    }

    public ResourceLocation getActualImage() {
        return this.image;
    }

    public void clearSkinCache() {
        this.skinCache = null;
    }

    public PlayerSkin getSkin(PlayerSkin existing) {
        if (this.skinCache != null) return this.skinCache;
        return this.skinCache = new PlayerSkin(
                existing.texture(),
                existing.textureUrl(),
                this.image,
                this.image,
                existing.model(),
                existing.secure()
        );
    }

    public String getName() {
        return this.capeName;
    }

    public String getId() {
        return this.capeId;
    }

    public boolean isThirdPartyCape() {
        return this.thirdPartyCape;
    }

    public String getOrigin() {
        return this.origin;
    }

    // Generated & Reformatted equals, hashCode, and toString functions.

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        CapeData capeData = (CapeData) other;
        return this.thirdPartyCape == capeData.thirdPartyCape && Objects.equals(this.image, capeData.image) && Objects.equals(this.capeName, capeData.capeName) && Objects.equals(this.capeId, capeData.capeId) && Objects.equals(this.origin, capeData.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.image, this.capeName, this.capeId, this.thirdPartyCape, this.origin);
    }

    @Override
    public String toString() {
        return "CapeData{" +
                "image=" + this.image +
                ", capeName='" + this.capeName + '\'' +
                ", capeId='" + this.capeId + '\'' +
                ", thirdPartyCape=" + this.thirdPartyCape +
                ", origin='" + this.origin + '\'' +
                '}';
    }
}
