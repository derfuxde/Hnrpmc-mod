package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraLayer.class)
public interface ElytraLayerAccessor {
    @Accessor("elytraModel")
    ElytraModel<LivingEntity> getElytraModel();
}
