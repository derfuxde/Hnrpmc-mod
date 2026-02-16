package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.Hats;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.CosmeticFetcher;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.LinearAlgebra;
import org.emil.hnrpmc.hnessentials.mixin.Flattener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.emil.hnrpmc.hnessentials.mixin.GhostRenderer.renderGhost;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixinFlat<T extends LivingEntity> {

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
            shift = Shift.AFTER,
            ordinal = 1))
    public void flatterRender(T entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, CallbackInfo cir) {
        final boolean shouldSit = entityIn.isPassenger();
        float f = Flattener.getYawRotation(entityIn, partialTicks, shouldSit);
        double x = entityIn.getX();
        double z = entityIn.getZ();

        final Player player = Minecraft.getInstance().player;
        if (player != null) {
            x -= player.getX();
            z -= player.getZ();
        }

        if (entityIn instanceof Player target) {
            Flattener.prepareToyRendering(f, x, z, poseStack, entityIn, bufferSource);
        }

        //Flattener.prepareFlatRendering(f, x, z, poseStack, entityIn);
    }

}

