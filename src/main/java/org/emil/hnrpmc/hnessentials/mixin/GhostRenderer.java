package org.emil.hnrpmc.hnessentials.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.ElytraModelAccessor;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.fakeplayer.HumanoidModelAccessor;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.fakeplayer.PlayerModelAccessor;

import java.util.*;

@EventBusSubscriber(modid = Hnrpmc.MODID, value = Dist.CLIENT)
public class GhostRenderer {

    @SubscribeEvent
    public static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        Flattener.ghosts.clear();
        lastages.clear();
    }

    //public static final List<GhostSnapshot> ghosts = new ArrayList<>();
    public static final Map<Integer, Float> lastages = new HashMap<>();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;



        boolean isFrozen;
        if (mc.level != null) {
            isFrozen = mc.level.tickRateManager().isFrozen();
        } else {
            isFrozen = false;
        }

        long now = System.currentTimeMillis();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        List<GhostSnapshot> ghosts = Flattener.ghosts;

        if (ghosts.isEmpty()) return;

        for (GhostSnapshot ghost : ghosts) {
            //float age = !isFrozen ? (now - ghost.timestamp()) / 500f : ghost.age();
            float lastage = lastages.containsKey(ghosts.indexOf(ghost)) ? lastages.get(ghosts.indexOf(ghost)) : !isFrozen ? (event.getRenderTick() - ghost.startTick()) / 20.0f : ghost.startTick();
            float age = !isFrozen ? (now - ghost.startTime()) / 1000.0f : lastage;

            // Cleanup: Wenn der Geist älter als 2 Sekunden ist
            if (age > 2.0f && !isFrozen) {
                //ghosts.remove(ghost);
                continue;
            }
            PlayerRenderer srenderer = (PlayerRenderer) dispatcher.getSkinMap().get(ghost.PlayerSkin().model());

            ghost.PlayerSaved().setGhostEquipment(ghost.equipment());

            setModelProperties(ghost.PlayerSaved());
            //RenderPlayerRenderer.render(poseStack, ghost.PlayerSaved(), bufferSource, 0, 0, 0, ghost.yBodyRot(), 0, 100);

            PlayerModel<AbstractClientPlayer> model = ghost.PlayerSaved().getModel();

            if (!isFrozen) {
                lastages.put(ghosts.indexOf(ghost), age);
            }

            float fade = Math.max(0, 1.0f - (age / 2.0f));
            float alpha = 0.4f * fade;

            poseStack.pushPose();
            poseStack.translate(
                    ghost.position().x - cameraPos.x,
                    ghost.position().y - cameraPos.y,
                    ghost.position().z - cameraPos.z
            );

            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - ghost.yBodyRot()));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0D, -1.501D, 0.0D);

            if (model == null) return;

            model.body.xRot = ghost.xRot();
            model.body.yRot = ghost.yBodyRot();

            model.attackTime = 0;
            model.riding = false;
            model.young = false;
            model.crouching = ghost.isCrouching();;
            if (ghost.isFallFlying()) {
                model.leftArm.xRot = (float)Math.toRadians(180.0);
                model.rightArm.xRot = (float)Math.toRadians(180.0);
                model.leftLeg.xRot = (float)Math.toRadians(0.0);
                model.rightLeg.xRot = (float)Math.toRadians(0.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(ghost.xRot() + 90));
                poseStack.translate(0.0D, -1.0D, 0.0D);
            }
            model.swimAmount = ghost.isSwimming() ? 1 : 0;

            model.attackTime = ghost.swingTime();

            float limbNodes = ghost.walkAnimPos();
            float limbSpeed = ghost.walkAnimSpeed();

            //VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ghost.PlayerSkin().texture()));

            int color = java.awt.Color.HSBtoRGB(0.6f, 0.8f, 1.0f); // Optional: Bläulicher Flash-Look

            /*model.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    ghostColor
            );*/
            float yRotDiff = ghost.yHeadRot() - ghost.yBodyRot();
            modelSetupAnim(
                    model,
                    ghost.PlayerSaved(),
                    limbNodes,
                    limbSpeed,
                    0.0f,
                    ghost.yHeadRot() - ghost.yBodyRot(),
                    ghost.xRot()
            );

            int alphaInt = (int)(alpha * 255);
            int ghostColor = FastColor.ARGB32.color(alphaInt, 150, 150, 255);

            ResourceLocation skinTex = ghost.PlayerSkin().texture();
            RenderType ghostLayer = RenderType.entityTranslucent(skinTex);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(ghostLayer);
            int packedOverlayCoords = getOverlayCoords(0.0f);
            model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, ghostColor);

            int combinedLight = LevelRenderer.getLightColor(mc.level, BlockPos.containing(ghost.position()));

            int ghostLight = 0xF000F0;//Math.max(combinedLight, 15728880);

            // In GhostRenderer.java -> onRenderLevel
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    if (slot == EquipmentSlot.CHEST && ghost.PlayerSaved().getItemBySlot(slot).getItem() instanceof ElytraItem) {
                        renderGhostElytra(ghost, model, poseStack, bufferSource, ghostLight, alpha);
                    } else {
                        renderGhostArmor(ghost, model, poseStack, bufferSource, ghostLight, slot, alpha);
                    }
                }
            }

            renderGhostHandItem(ghost, model, poseStack, bufferSource, combinedLight, true, ghostColor);
            renderGhostHandItem(ghost, model, poseStack, bufferSource, combinedLight, false, ghostColor);


            for (GohstMenuRenderLayer layer : ghost.PlayerSaved().getLayers()) {
                layer.render(
                        poseStack,
                        bufferSource,
                        0xF000F0,
                        ghost.PlayerSaved(),
                        0.0f, // animationPosition
                        0.0f, // animationSpeed
                        0.0f, // delta
                        0.0f, // bob
                        yRotDiff,
                        ghost.xRot(),
                        alpha
                );
            }

            //ghosts.remove(ghost);
            //ghosts.add(new GhostSnapshot(ghost.position(), ghost.yBodyRot(), ghost.yHeadRot(), ghost.xRot(), ghost.PlayerSkin(), ghost.timestamp(), ghost.look(), ghost.PlayerSaved(), age));

            poseStack.popPose();
        }

        ghosts.removeIf(ghost -> (System.currentTimeMillis() - ghost.startTime()) > 2000);
    }

    public static void renderGhost(AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();

        EntityRenderer<? super AbstractClientPlayer> renderer = renderDispatcher.getRenderer(player);
        if (!(renderer instanceof PlayerRenderer playerRenderer)) return;

        // 2. Modell vorbereiten
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();

        model.attackTime = player.getAttackAnim(mc.getFrameTimeNs());
        model.riding = player.isPassenger();
        model.young = player.isBaby();

        float partialTicks = mc.getFrameTimeNs();
        float yRot = Mth.lerp(partialTicks, player.yRotO, player.getYRot());
        float xRot = Mth.lerp(partialTicks, player.xRotO, player.getXRot());

        // PoseStack vorbereiten
        poseStack.pushPose();

        ResourceLocation texture = player.getSkin().texture();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        int color = FastColor.ARGB32.color((int)(alpha * 255), 255, 255, 255);

        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ElytraItem) {
            // Hier deine fertige renderGhostElytra Methode aufrufen,
            // aber 'player' statt 'ghost' übergeben
        }

        // Das Hauptmodell zeichnen
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }

    private static void renderGhostHandItem(GhostSnapshot ghost, PlayerModel<?> model, PoseStack poseStack, MultiBufferSource buffer, int light, boolean rightHand, int color) {
        ItemStack itemStack = ghost.PlayerSaved().getItemBySlot(rightHand ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        if (itemStack.isEmpty()) return;

        poseStack.pushPose();
        if (rightHand) {
            model.rightArm.translateAndRotate(poseStack);
        } else {
            model.leftArm.translateAndRotate(poseStack);
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(rightHand ? -0.0625F : 0.0625F, 0.125F, -0.625F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                itemStack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                null,
                color
        );
        poseStack.popPose();
    }

    private static void renderGhostArmor(GhostSnapshot ghost, PlayerModel<AbstractClientPlayer> playerModel, PoseStack poseStack, MultiBufferSource buffer, int light, EquipmentSlot slot, float alpha) {
        ItemStack armorStack = ghost.PlayerSaved().getItemBySlot(slot);
        if (armorStack.isEmpty()) return;

        var baseModel = new HumanoidModel<LivingEntity>(Minecraft.getInstance().getEntityModels().bakeLayer(
                slot == EquipmentSlot.LEGS ? ModelLayers.PLAYER_INNER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR));

        var armorModel = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(armorStack)
                .getHumanoidArmorModel(null, armorStack, slot, baseModel);

        playerModel.copyPropertiesTo((HumanoidModel<AbstractClientPlayer>) (Object) armorModel);

        ResourceLocation texture = net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();

        if (armorStack.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
            var layers = armorItem.getMaterial().value().layers();
            if (!layers.isEmpty()) {
                net.minecraft.world.item.ArmorMaterial.Layer layer = layers.get(0);

                texture = net.neoforged.neoforge.client.ClientHooks.getArmorTexture(
                        null,
                        armorStack,
                        layer,
                        slot == EquipmentSlot.LEGS,
                        slot
                );
            }
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(texture));
        int color = net.minecraft.util.FastColor.ARGB32.color((int)(alpha * 255), 255, 255, 255);

        armorModel.renderToBuffer(poseStack, vertexConsumer, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, color);
    }

    private static void renderGhostElytra(GhostSnapshot ghost, PlayerModel<AbstractClientPlayer> playerModel, PoseStack poseStack, MultiBufferSource buffer, int light, float alpha) {
        ItemStack chestStack = ghost.PlayerSaved().getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestStack.getItem() instanceof net.minecraft.world.item.ElytraItem)) return;

        poseStack.pushPose();

        var elytraModel = new net.minecraft.client.model.ElytraModel<net.minecraft.world.entity.LivingEntity>(
                Minecraft.getInstance().getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.ELYTRA)
        );

        ElytraModelAccessor accessor = (ElytraModelAccessor) elytraModel;
        ModelPart left = accessor.getLeftWing();
        ModelPart right = accessor.getRightWing();

        AbstractClientPlayer ACP = new AbstractClientPlayer(Minecraft.getInstance().player.clientLevel, Minecraft.getInstance().player.getGameProfile()) {
            @Override
            public boolean isSpectator() {
                return super.isSpectator();
            }
        };

        right.xRot = ACP.elytraRotX;
        right.yRot = ACP.elytraRotY;
        right.zRot = ACP.elytraRotZ;

        // Rechts ist gespiegelt
        right.xRot = left.xRot;
        right.yRot = -left.yRot;
        right.zRot = -left.zRot;

        // Positionierung am Rücken (wie besprochen)
        playerModel.body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, -1.5, 0.125D);

        poseStack.scale(2, 2, 2);

        // Falls der Geist schleicht, muss die Elytra auch physisch tiefer sitzen
        if (ghost.isCrouching()) {
            left.y = 3.0F;
            right.y = 3.0F;
        }

        ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int color = FastColor.ARGB32.color((int)(alpha * 255), 255, 255, 255);

        elytraModel.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }

    private static void setModelProperties(PlayerRendereType fakePlayer) {
        PlayerModel<AbstractClientPlayer> playerModel = fakePlayer.getModel();

        playerModel.setAllVisible(true);
        playerModel.hat.visible = fakePlayer.isModelPartShown(PlayerModelPart.HAT);
        playerModel.jacket.visible = fakePlayer.isModelPartShown(PlayerModelPart.JACKET);
        playerModel.leftPants.visible = fakePlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        playerModel.rightPants.visible = fakePlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        playerModel.leftSleeve.visible = fakePlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        playerModel.rightSleeve.visible = fakePlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        playerModel.crouching = fakePlayer.isSneaking();

        if (fakePlayer.getMainArm() == HumanoidArm.RIGHT) {
            playerModel.rightArmPose = fakePlayer.isMainArmRaised() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
            playerModel.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        } else {
            playerModel.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            playerModel.leftArmPose = fakePlayer.isMainArmRaised() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        }
    }

    private static int getOverlayCoords(float u) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(false));
    }

    private static RenderType getRenderType(PlayerRendereType player, boolean isVisible, boolean isInvisibleToPlayer, boolean isGlowing) {
        ResourceLocation resourceLocation = player.getSkinn();

        if (isInvisibleToPlayer) {
            return RenderType.itemEntityTranslucentCull(resourceLocation);
        } else if (isVisible) {
            return player.getModel().renderType(resourceLocation);
        } else {
            return isGlowing ? RenderType.outline(resourceLocation) : null;
        }
    }

    private static void modelSetupAnim(PlayerModel<AbstractClientPlayer> model, PlayerRendereType player, float f, float g, float bob, float yRotDiff, float xRot) {
        model.head.yRot = yRotDiff * 0.017453292F;

        if (model.swimAmount > 0.0F) {
            model.head.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(model.swimAmount, model.head.xRot, xRot * 0.017453292F);
        } else {
            model.head.xRot = xRot * 0.017453292F;
        }

        model.body.yRot = 0.0F;
        model.rightArm.z = 0.0F;
        model.rightArm.x = -5.0F;
        model.leftArm.z = 0.0F;
        model.leftArm.x = 5.0F;
        float k = 1.0F;

        if (k < 1.0F) {
            k = 1.0F;
        }

        model.rightArm.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / k;
        model.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;
        model.rightArm.zRot = 0.0F;
        model.leftArm.zRot = 0.0F;
        model.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g / k;
        model.leftLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g / k;
        model.rightLeg.yRot = 0.0F;
        model.leftLeg.yRot = 0.0F;
        model.rightLeg.zRot = 0.0F;
        model.leftLeg.zRot = 0.0F;
        ModelPart currentModel;

        if (model.riding) {
            currentModel = model.rightArm;
            currentModel.xRot += -0.62831855F;
            currentModel = model.leftArm;
            currentModel.xRot += -0.62831855F;
            model.rightLeg.xRot = -1.4137167F;
            model.rightLeg.yRot = 0.31415927F;
            model.rightLeg.zRot = 0.07853982F;
            model.leftLeg.xRot = -1.4137167F;
            model.leftLeg.yRot = -0.31415927F;
            model.leftLeg.zRot = -0.07853982F;
        }

        model.rightArm.yRot = 0.0F;
        model.leftArm.yRot = 0.0F;
        boolean bl3 = player.getMainArm() == HumanoidArm.RIGHT;
        boolean bl4;

        bl4 = bl3 ? model.leftArmPose.isTwoHanded() : model.rightArmPose.isTwoHanded();

        if (bl3 != bl4) {
            poseLeftArm(model);
            poseRightArm(model);
        } else {
            poseRightArm(model);
            poseLeftArm(model);
        }

        if (model.crouching) {
            model.body.xRot = 0.5F;
            currentModel = model.rightArm;
            currentModel.xRot += 0.4F;
            currentModel = model.leftArm;
            currentModel.xRot += 0.4F;
            model.rightLeg.z = 4.0F;
            model.leftLeg.z = 4.0F;
            model.rightLeg.y = 12.2F;
            model.leftLeg.y = 12.2F;
            model.head.y = 4.2F;
            model.body.y = 3.2F;
            model.leftArm.y = 5.2F;
            model.rightArm.y = 5.2F;
        } else {
            model.body.xRot = 0.0F;
            model.rightLeg.z = 0.1F;
            model.leftLeg.z = 0.1F;
            model.rightLeg.y = 12.0F;
            model.leftLeg.y = 12.0F;
            model.head.y = 0.0F;
            model.body.y = 0.0F;
            model.leftArm.y = 2.0F;
            model.rightArm.y = 2.0F;
        }

        if (model.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(model.rightArm, bob, 1.0F);
        }

        if (model.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(model.leftArm, bob, -1.0F);
        }

        if (model.swimAmount > 0.0F) {
            float l = f % 26.0F;
            HumanoidArm humanoidArm = player.getMainArm();
            float m = humanoidArm == HumanoidArm.RIGHT && model.attackTime > 0.0F ? 0.0F : model.swimAmount;
            float n = humanoidArm == HumanoidArm.LEFT && model.attackTime > 0.0F ? 0.0F : model.swimAmount;
            float o;

            if (l < 14.0F) {
                model.leftArm.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.xRot, 0.0F);
                model.rightArm.xRot = Mth.lerp(m, model.rightArm.xRot, 0.0F);
                model.leftArm.yRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.yRot, 3.1415927F);
                model.rightArm.yRot = Mth.lerp(m, model.rightArm.yRot, 3.1415927F);
                model.leftArm.zRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.zRot, 3.1415927F + 1.8707964F * ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(l) / ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(14.0F));
                model.rightArm.zRot = Mth.lerp(m, model.rightArm.zRot, 3.1415927F - 1.8707964F * ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(l) / ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(14.0F));
            } else if (l >= 14.0F && l < 22.0F) {
                o = (l - 14.0F) / 8.0F;
                model.leftArm.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.xRot, 1.5707964F * o);
                model.rightArm.xRot = Mth.lerp(m, model.rightArm.xRot, 1.5707964F * o);
                model.leftArm.yRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.yRot, 3.1415927F);
                model.rightArm.yRot = Mth.lerp(m, model.rightArm.yRot, 3.1415927F);
                model.leftArm.zRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.zRot, 5.012389F - 1.8707964F * o);
                model.rightArm.zRot = Mth.lerp(m, model.rightArm.zRot, 1.2707963F + 1.8707964F * o);
            } else if (l >= 22.0F && l < 26.0F) {
                o = (l - 22.0F) / 4.0F;
                model.leftArm.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.xRot, 1.5707964F - 1.5707964F * o);
                model.rightArm.xRot = Mth.lerp(m, model.rightArm.xRot, 1.5707964F - 1.5707964F * o);
                model.leftArm.yRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.yRot, 3.1415927F);
                model.rightArm.yRot = Mth.lerp(m, model.rightArm.yRot, 3.1415927F);
                model.leftArm.zRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.zRot, 3.1415927F);
                model.rightArm.zRot = Mth.lerp(m, model.rightArm.zRot, 3.1415927F);
            }

            model.leftLeg.xRot = Mth.lerp(model.swimAmount, model.leftLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F + 3.1415927F));
            model.rightLeg.xRot = Mth.lerp(model.swimAmount, model.rightLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F));
        }

        model.hat.copyFrom(model.head);

        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.jacket.copyFrom(model.body);

        ModelPart cloak = ((PlayerModelAccessor) model).getCloak();

        if (player.isSneaking()) {
            cloak.z = 1.4F;
            cloak.y = 1.85F;
        } else {
            cloak.z = 0.0F;
            cloak.y = 0.0F;
        }
    }

    private static void poseLeftArm(PlayerModel model) {
        switch(model.leftArmPose) {
            case EMPTY:
                model.leftArm.yRot = 0.0F;
                break;
            case BLOCK:
                model.leftArm.xRot = model.leftArm.xRot * 0.5F - 0.9424779F;
                model.leftArm.yRot = 0.5235988F;
                break;
            case ITEM:
                model.leftArm.xRot = model.leftArm.xRot * 0.5F - 0.31415927F;
                model.leftArm.yRot = 0.0F;
                break;
        }
    }

    private static void poseRightArm(PlayerModel model) {
        switch (model.rightArmPose) {
            case EMPTY:
                model.rightArm.yRot = 0.0F;
                break;
            case BLOCK:
                model.rightArm.xRot = model.rightArm.xRot * 0.5F - 0.9424779F;
                model.rightArm.yRot = -0.5235988F;
                break;
            case ITEM:
                model.rightArm.xRot = model.rightArm.xRot * 0.5F - 0.31415927F;
                model.rightArm.yRot = 0.0F;
                break;
        }
    }
}