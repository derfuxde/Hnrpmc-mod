package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class MixinHideItemsWhenInvisible {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hnrpmc$hideHeldItems(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                      LivingEntity entity, float limbSwing, float limbSwingAmount,
                                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                                      CallbackInfo ci) {
        if (entity instanceof Player) {
            HNPlayerData hnPlayerData = HNessentials.getInstance().getHNPlayerData().get(entity.getUUID());
            if (Minecraft.getInstance().player == null || hnPlayerData == null) return;
            HNPlayerData localhnPlayerData = HNessentials.getInstance().getHNPlayerData().get(Minecraft.getInstance().player.getUUID());

            if (hnPlayerData.isVanish() && !localhnPlayerData.getTags().contains("vanish_see")) {
                ci.cancel();
            }
        }


    }
}