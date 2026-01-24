package org.emil.hnrpmc.hnessentials.cosmetics.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Box;

public record BakableModel(String id, String name, BlockModel model, ResourceLocation image, int extraInfo, Box bounds) {
}
