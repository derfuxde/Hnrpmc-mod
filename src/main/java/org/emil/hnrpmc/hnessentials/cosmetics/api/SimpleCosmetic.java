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

    public SimpleCosmetic(String id, String name, CosmeticType<?> type, CosmeticSlot slot) {
        this.id = id;
        this.name = name;
        this.type = type;

        // ResourceLocation ist Server-sicher
        ResourceLocation modelRl = ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetics/" + id);

        // Wir übergeben 'null' für das BakableModel, da der Server es nicht braucht.
        // Der Client lädt das echte Modell später über die ResourceLocation.
        this.internalCosmetic = new Cosmetic(id, slot, modelRl, Component.literal(name), null);
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public CosmeticType<?> getType() { return type; }
    @Override public Cosmetic getasCosmetic() { return internalCosmetic; }
    @Override public User getOwner() { return new User(new UUID(0, 0), "System"); }
    @Override public long getUploadTime() { return 0; }
}