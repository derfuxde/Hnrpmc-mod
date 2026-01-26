package org.emil.hnrpmc.hnessentials.requester;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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

public class PetSkinLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation GOLD_TEXTURE = ResourceLocation.fromNamespaceAndPath("hnrpmc", "textures/entity/wolf/gold_wolf.png");
    // Wir nutzen das Standard-Texture-System von Minecraft, um die "normale" Textur des Tiers zu finden

    public PetSkinLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    private Integer oldSkinId = null;

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // Prüfen, ob das Tier gezähmt ist (funktioniert für Wolf, Katze, Papagei)
        if (entity instanceof Player) {

            renderPlayer(poseStack, buffer, packedLight, entity, this.getTextureLocation(entity), 0xFF_B9F2FF);
        }
        if (entity instanceof TamableAnimal tamable) {
            if (tamable.getOwnerUUID() == null) return;
        } else if (entity instanceof AbstractHorse horse) {
            if (!horse.isTamed()) return;
        } else {
            return;
        }

        // Skin aus deinem Cache holen
        int skinIndex = entity.getPersistentData().getInt("skinIndex");//HNessentials.clientPetSkins.get(entity.getUUID());
        //if (skinIndex == null) return;

        if (oldSkinId == null) {
            oldSkinId = skinIndex;
        }

        List<String> skins = HNessentials.getInstance().getSkins();
        if (skinIndex >= 0 && skinIndex < skins.size()) {
            String texname = skins.get(skinIndex);

            ResourceLocation currentTexture = this.getTextureLocation(entity);

            if (texname.equals("Gold")) {
                renderEffect(poseStack, buffer, packedLight, entity, GOLD_TEXTURE, 0xFF_FFD700);
            } else if (texname.equals("Diamond")) {
                renderEffect(poseStack, buffer, packedLight, entity, currentTexture, 0xFF_B9F2FF);
            } else if (texname.equals("Rainbow")) {
                renderRainbow(poseStack, buffer, packedLight, entity, currentTexture, partialTicks);
            }
        }
    }

    private void renderEffect(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, ResourceLocation texture, int color) {
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), color);
    }

    private void renderPlayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, ResourceLocation texture, float partialTicks) {


    }

    private void renderRainbow(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, ResourceLocation texture, float partialTicks) {
        float speed = 50.0f;
        float hue = (entity.tickCount + partialTicks) / speed;
        hue = hue % 1.0f;
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        int rainbowColor = 0xFF000000 | (rgb & 0xFFFFFF);

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), rainbowColor);
    }
}