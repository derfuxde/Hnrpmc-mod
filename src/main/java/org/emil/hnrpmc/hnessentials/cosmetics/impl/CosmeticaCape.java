package org.emil.hnrpmc.hnessentials.cosmetics.impl;


import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCape;
import org.emil.hnrpmc.hnessentials.cosmetics.api.User;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.CosmeticModelLoader;

import static net.neoforged.fml.loading.FMLEnvironment.dist;

class CosmeticaCape extends BaseCape implements CustomCape {
    CosmeticaCape(String id, String name, String origin, String image, boolean cosmeticaAlternative, int frameDelay, long uploadTime, User owner) {
        super(id, name, origin, image, cosmeticaAlternative, frameDelay);

        this.owner = owner;
        this.uploadTime = uploadTime;
        this.showHemlet = true;

        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetics/" + id);

        BakableModel model = null;
        if (dist == Dist.CLIENT) {
            model = CosmeticModelLoader.createBakableModel(this.getId(), this.getName());
        }

        this.cosmetic = new Cosmetic(id, CosmeticSlot.CAPE, rl, Component.literal(name), model);
    }

    private final Cosmetic cosmetic;
    private final User owner;
    private final long uploadTime;
    private final boolean showHemlet;

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
    public CosmeticType<?> getType() {
        return CosmeticType.CAPE;
    }

    @Override
    public Cosmetic getasCosmetic() {
        BakableModel model = null;
        if (dist == Dist.CLIENT) {
            model = CosmeticModelLoader.createBakableModel(this.getId(), this.getName());
        }

        return new Cosmetic(
                this.getId(),
                CosmeticSlot.CAPE,
                ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetics/" + this.getId()),
                Component.literal(this.getName()),
                model
        );
    }
}
