package org.emil.hnrpmc.hnessentials.cosmetics.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;

import java.util.UUID;

public class SimpleCosmetic implements CustomCosmetic {
    private final String id;
    private final String name;
    private final CosmeticType<?> type;
    private final Cosmetic internalCosmetic;
    private final boolean showHelmet;

    public SimpleCosmetic(String id, String name, CosmeticType<?> type, CosmeticSlot slot, boolean showHelmet) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.showHelmet = showHelmet;

        ResourceLocation modelRl = ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetics/" + id);

        this.internalCosmetic = new Cosmetic(id, slot, modelRl, Component.literal(name), null);
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public CosmeticType<?> getType() { return type; }
    @Override public Cosmetic getasCosmetic() { return internalCosmetic; }
    @Override public User getOwner() { return new User(new UUID(0, 0), "System"); }
    @Override public boolean showHelmet() { return showHelmet; }
    @Override public long getUploadTime() { return 0; }
}