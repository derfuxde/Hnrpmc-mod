package org.emil.hnrpmc.hnessentials;

import net.minecraft.client.model.Model;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.User;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;

public class Cosmetic {

    private final String id;
    private final CosmeticSlot slot;
    private final ResourceLocation model;
    private final Component displayName;
    private final BakableModel bakableModel;

    public Cosmetic(String id, CosmeticSlot slot, ResourceLocation model, Component displayName, BakableModel bakableModel) {
        this.id = id;
        this.slot = slot;
        this.model = model;
        this.displayName = displayName;
        this.bakableModel = bakableModel;
    }

    public String getId() {
        return id;
    }

    public CosmeticSlot getSlot() {
        return slot;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public Component getDisplayName() {
        return displayName;
    }
}
