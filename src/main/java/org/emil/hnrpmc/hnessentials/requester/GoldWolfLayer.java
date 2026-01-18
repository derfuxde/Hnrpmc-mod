package org.emil.hnrpmc.hnessentials.requester;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Scoreboard;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.listeners.PlayerEventLister;
import org.emil.hnrpmc.hnessentials.menu.PetMorphScreen;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GoldWolfLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
    // Deine ResourceLocation
    private static final ResourceLocation GOLD_TEXTURE = ResourceLocation.fromNamespaceAndPath("hnrpmc", "textures/entity/wolf/gold_wolf.png");
    private static final ResourceLocation Normal = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/wolf/wolf.png");

    public GoldWolfLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (wolf.getOwnerUUID() == null) return;

        Integer skinIndex = HNessentials.clientPetSkins.get(wolf.getUUID());

        if (skinIndex == null) {
            return;
        }

        List<String> skins = HNessentials.getInstance().getSkins();
        if (skinIndex >= 0 && skinIndex < skins.size()) {
            String texname = skins.get(skinIndex);
            if (texname.equals("Gold")) {
                golden(poseStack, buffer, packedLight, wolf, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            } else if (texname.equals("Diamond")) {
                diamond(poseStack, buffer, packedLight, wolf, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            } else if (texname.equals("Rainbow")) {
                Rainbow(poseStack, buffer, packedLight, wolf, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }
    }


    /*
    (PoseStack poseStack, MultiBufferSource buffer, int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    golden(poseStack, buffer, packedLight, wolf, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    Rainbow(poseStack, buffer, packedLight, wolf, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    */
    private void golden(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(GOLD_TEXTURE));

        int goldColor = 0xFF_FFD700;

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexconsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(wolf, 0.0F),
                goldColor
        );
    }

    private void diamond(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(Normal));

        int goldColor = 0xFF_B9F2FF;

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexconsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(wolf, 0.0F),
                goldColor
        );
    }

    private void Rainbow(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = 50.0f;

        // 2. Den "Hue" (Farbton) basierend auf der Zeit berechnen
        // ageInTicks + partialTicks sorgt dafür, dass die Animation flüssig ist (nicht ruckelt)
        float hue = (wolf.tickCount + partialTicks) / speed;
        hue = hue % 1.0f; // Wert bleibt immer zwischen 0.0 und 1.0

        // 3. Den HSB-Wert in einen RGB-Integer umrechnen
        // (Farbton, Sättigung 100%, Helligkeit 100%)
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);

        // 4. Den Alpha-Kanal hinzufügen (0xFF für volle Deckkraft)
        // Wir nehmen die RGB-Werte und setzen FF davor
        int rainbowColor = 0xFF000000 | (rgb & 0xFFFFFF);

        // 5. Den Buffer holen und rendern
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(Normal));

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexconsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(wolf, 0.0F),
                rainbowColor
        );
    }
}