package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraModel.class)
public interface ElytraModelAccessor {
    @Accessor("leftWing")
    ModelPart getLeftWing();

    @Accessor("rightWing")
    ModelPart getRightWing();
}
