package org.emil.hnrpmc.hnessentials.requester;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class PlayerSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerSkinLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {


        ResourceLocation texture = player.getSkin().texture();

        // Rainbow Effekt
        float speed = 50.0f;
        float hue = (player.tickCount + partialTicks) / speed;
        int rgb = java.awt.Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f);
        int color = 0xFF000000 | (rgb & 0xFFFFFF);

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexconsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                color
        );
    }
}