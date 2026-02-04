package org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.fakeplayer.PlayerModelAccessor;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.LinearAlgebra;
import org.emil.hnrpmc.hnessentials.mixin.GohstMenuRenderLayer;
import org.emil.hnrpmc.hnessentials.mixin.PlayerRendereType;

public class MenuCapeLayer implements MenuRenderLayer, GohstMenuRenderLayer {
    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float f, float g, float delta, float bob, float yRotDiff, float xRot) {
        if (player.getData().cape().getImage() != null) {
            stack.pushPose();
            stack.translate(0.0D, 0.0D, 0.125D);
            double d = 0 - 0;
            double e = 0 - 0;
            double m = 0 - 0;
            float n = player.getYRotBody(0);
            double o = Mth.sin(n * 0.017453292F);
            double p = -Mth.cos(n * 0.017453292F);
            float q = (float)e * 10.0F;
            q = Mth.clamp(q, -6.0F, 32.0F);
            float r = (float)(d * o + m * p) * 100.0F;
            r = Mth.clamp(r, 0.0F, 150.0F);
            float s = (float)(d * p - m * o) * 100.0F;
            s = Mth.clamp(s, -20.0F, 20.0F);
            if (r < 0.0F) {
                r = 0.0F;
            }

            if (player.isSneaking()) {
                q += 25.0F;
            }

            stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.XP, 6.0F + r / 2.0F + q));
            stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.ZP, s / 2.0F));
            stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.YP, 180.0F - s / 2.0F));
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getRenderableCape()));
            ((PlayerModelAccessor) player.getModel()).getCloak().render(stack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, PlayerRendereType player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
        render(stack, bufferSource, packedLight, player, o, n, delta, bob, yRotDiff, xRot, 1.0f);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, PlayerRendereType player, float f, float g, float delta, float bob, float yRotDiff, float xRot, float alpha) {
        if (player.getData() == null || player.getData().cape() == null) return;
        if (player.getData().cape().getImage() != null) {
            stack.pushPose();
            stack.translate(0.0D, 0.0D, 0.125D);
            double d = 0 - 0;
            double e = 0 - 0;
            double m = 0 - 0;
            float n = player.getYRotBody(0);
            double o = Mth.sin(n * 0.017453292F);
            double p = -Mth.cos(n * 0.017453292F);
            float q = (float)e * 10.0F;
            q = Mth.clamp(q, -6.0F, 32.0F);
            float r = (float)(d * o + m * p) * 100.0F;
            r = Mth.clamp(r, 0.0F, 150.0F);
            float s = (float)(d * p - m * o) * 100.0F;
            s = Mth.clamp(s, -20.0F, 20.0F);
            if (r < 0.0F) {
                r = 0.0F;
            }

            if (player.isSneaking()) {
                q += 25.0F;
            }

            stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.XP, 6.0F + r / 2.0F + q));
            stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.ZP, s / 2.0F));
            stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.YP, 180.0F - s / 2.0F));
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getRenderableCape()));
            ((PlayerModelAccessor) player.getModel()).getCloak().render(stack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }
}
