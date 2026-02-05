package org.emil.hnrpmc.hnessentials.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.PlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.*;

import static org.emil.hnrpmc.hnessentials.mixin.GhostRenderer.renderGhost;

public class Flattener {
    public static boolean renderingEnabled = true;

    public static final List<EntityType<?>> entityBlacklist = new ArrayList<>();
    public static final Map<ResourceKey<Level>, List<EntityType<?>>> entityDimensionWhitelist = new HashMap<>();
    public static final List<ResourceKey<Level>> dimensionBlacklist = new ArrayList<>();
    public static boolean dimensionListIsWhitelist = false;

    /**
     * Prepares the rendering of an entity in a flat style based on certain conditions. This method adjusts the pose stack
     * to achieve the desired flat rendering effect for the entity.
     *
     * @param rotation  The yaw rotation angle of the entity, in degrees. This angle is often the result of linearly interpolating
     *                  between the entity's previous and current yaw rotations using Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot()).
     * @param x         The X-coordinate of the entity's position.
     * @param z         The Z-coordinate of the entity's position.
     * @param poseStack The PoseStack used for rendering transformations.
     * @param entityIn  The entity to be rendered.
     */
    public static void prepareFlatRendering(float rotation, double x, double z, PoseStack poseStack, Entity entityIn) {
        if (renderingEnabled) {
            // Extract entity and dimension information
            final EntityType<?> entityType = entityIn.getType();
            final ResourceKey<Level> entityDimension = entityIn.getCommandSenderWorld().dimension();

            // Check if entity and dimension are blacklisted
            final boolean entityInList = entityBlacklist.contains(entityIn.getType());
            final boolean worldInList = dimensionBlacklist.contains(entityDimension);
            boolean entityBlacklisted = !entityBlacklist.isEmpty() && entityInList;
            boolean dimensionFlat = dimensionBlacklist.isEmpty() || (!dimensionBlacklist.isEmpty() && dimensionListIsWhitelist == worldInList);
            boolean renderAnyway = false;

            if (entityIn.getType() != EntityType.PLAYER) return;

            // Check if entity should be rendered based on whitelist
            if (!dimensionBlacklist.isEmpty() && !entityDimensionWhitelist.isEmpty()) {
                List<EntityType<?>> whitelist = entityDimensionWhitelist.getOrDefault(entityDimension, new ArrayList<>());
                renderAnyway = whitelist.contains(entityType) && !entityBlacklisted;
            }

            // Apply flat rendering adjustments if necessary
            if (!entityBlacklisted && (dimensionFlat || renderAnyway)) {
                // Get camera view type and player information
                final CameraType viewPoint = Minecraft.getInstance().options.getCameraType();
                boolean isPlayer = entityIn instanceof Player;
                float offset = 0;

                // Calculate rotation angles
                double angle1 = Mth.wrapDegrees(Math.atan2(z, x) / Math.PI * 180.0D);
                double angle2 = Mth.wrapDegrees(Math.floor((rotation - angle1) / 45.0) * 45.0D);

                // Adjust offset for player's head rotation
                if (isPlayer && entityIn instanceof Player player) {
                    offset = Mth.wrapDegrees(player.yHeadRot - player.yHeadRotO);
                }

                // Adjust angles based on camera view type
                if (isPlayer) {
                    if (viewPoint == CameraType.FIRST_PERSON || viewPoint == CameraType.THIRD_PERSON_BACK) {
                        angle1 = -90.0F - offset;
                    }
                    if (viewPoint == CameraType.THIRD_PERSON_FRONT) {
                        angle1 = 90 + offset;
                    }
                }

                // Apply Y-axis rotation transformation
                poseStack.mulPose(Axis.YP.rotationDegrees((float) angle1));

                // Scale entity for flat rendering effect
                poseStack.scale(0.02F, 1.0F, 1.0F);

                // Adjust angles based on camera view type again
                if (isPlayer) {
                    if (viewPoint == CameraType.FIRST_PERSON || viewPoint == CameraType.THIRD_PERSON_BACK) {
                        angle2 = 90 + offset;
                    }
                    if (viewPoint == CameraType.THIRD_PERSON_FRONT) {
                        angle2 = -90 - offset;
                    }
                }

                // Apply additional Y-axis rotation transformation
                poseStack.mulPose(Axis.YP.rotationDegrees((float) angle2));
            }
        }
    }

    public static void prepareFlatFakePlayerRendering(PoseStack poseStack, FakePlayer fakePlayer, Vec3 cameraPos) {

        double diffX = fakePlayer.yRotBody;
        double diffZ = fakePlayer.yRot;

        final CameraType viewPoint = Minecraft.getInstance().options.getCameraType();

        double angle1 = Mth.wrapDegrees(Math.atan2(diffZ, diffX) / Math.PI * 180.0D);

        float offset = Mth.wrapDegrees(fakePlayer.yRotHead - fakePlayer.yRotBody);

        if (viewPoint == CameraType.FIRST_PERSON || viewPoint == CameraType.THIRD_PERSON_BACK) {
            angle1 = -90.0F - offset;
        } else if (viewPoint == CameraType.THIRD_PERSON_FRONT) {
            angle1 = 90.0F + offset;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees((float) angle1));

        poseStack.scale(0.02F, 1.0F, 1.0F);

        double angle2 = 0;
        if (viewPoint == CameraType.FIRST_PERSON || viewPoint == CameraType.THIRD_PERSON_BACK) {
            angle2 = 90.0F + offset;
        } else if (viewPoint == CameraType.THIRD_PERSON_FRONT) {
            angle2 = -90.0F - offset;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees((float) angle2));
    }

    /**
     * Calculates the yaw rotation angle for an entity's rendering, considering its body and head rotations.
     *
     * @param entityIn     The living entity for which to calculate the rotation angle.
     * @param partialTicks The partial tick value used for smooth rotation interpolation.
     * @param shouldSit    A boolean indicating whether the entity is sitting.
     * @param <T>          A subtype of LivingEntity.
     * @return The calculated yaw rotation angle for the entity's rendering.
     */
    public static <T extends LivingEntity> float getYawRotation(T entityIn, float partialTicks, boolean shouldSit) {
        float f = Mth.rotLerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
        final float f1 = Mth.rotLerp(partialTicks, entityIn.yHeadRotO, entityIn.yHeadRot);
        if (shouldSit && entityIn.getVehicle() instanceof LivingEntity livingentity) {
            f = Mth.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            final float f2 = f1 - f;
            float f3 = Mth.wrapDegrees(f2);

            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;

            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }
        }
        return f;
    }

    public static void prepareToyRendering(float rotation, double x, double z, PoseStack poseStack, Entity entity, MultiBufferSource bufferSource) {
        if (entity instanceof Player player) {

            HNPlayerData hnPlayerData = HNessentials.getInstance().getHNPlayerData().get(entity.getUUID());

            if (hnPlayerData == null) {
                PacketDistributor.sendToServer(new requestPlayerData(Minecraft.getInstance().player.getUUID()));
                return;
            }

            //if (player.getTags().contains("vanish")) {// || hnPlayerData.isVanish()) {
            //return;
            //}

            if (player.getTags().contains("vanished") || hnPlayerData.isVanish()) {
                float alpha = 0.5f;


                if (player instanceof LocalPlayer Lplayer) {
                    boolean canSeeVanish = SimpleClans.getInstance().getPermissionsManager().has(Lplayer, "essentials.admin.vanish.see");
                    if (canSeeVanish) {
                        renderGhost(Lplayer, poseStack, bufferSource, 0, alpha);
                    }
                }else if (player instanceof RemotePlayer Rplayer) {
                    boolean canSeeVanish = SimpleClans.getInstance().getPermissionsManager().has(Minecraft.getInstance().player, "essentials.admin.vanish.see");

                    if(canSeeVanish) {
                        renderGhost(Rplayer, poseStack, bufferSource, 0, alpha);
                    }
                }
                return;
            }

            if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID()) != null && Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID()).getGameMode() == GameType.SPECTATOR) {
                return;
            }

            float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();

            if (hnPlayerData.getSelectedskineffect().equalsIgnoreCase("holo")) {
                applyHologram(poseStack, partialTicks);
            } else if (hnPlayerData.getSelectedskineffect().equalsIgnoreCase("echo")) {
                applyGhostEffect(poseStack, entity, partialTicks);
                //applyEchoEffect(poseStack, partialTicks);
            } else if (hnPlayerData.getSelectedskineffect().equalsIgnoreCase("flat")) {
                prepareFlatRendering(rotation, x, z, poseStack, entity);
            } else if (hnPlayerData.getSelectedskineffect().equalsIgnoreCase("gigant")) {
                applyGiant(poseStack, partialTicks);
            } else if (hnPlayerData.getSelectedskineffect().equalsIgnoreCase("ghost")) {
                applyGhostEffect(poseStack, entity, partialTicks);
            }
        }
    }

    public static void prepareToyRendering(float rotation, double x, double z, PoseStack poseStack, FakePlayer entity, String effect, MultiBufferSource.BufferSource bufferSource) {


        HNPlayerData hnPlayerData = HNessentials.getInstance().getHNPlayerData().get(entity.getUUID());

        float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();

        if (effect.equalsIgnoreCase("holo")) {
            applyHologram(poseStack, partialTicks);
        } else if (effect.equalsIgnoreCase("echo")) {
            applyGhostEffect(poseStack, entity, partialTicks);
            //applyEchoEffect(poseStack, partialTicks);
        } else if (effect.equalsIgnoreCase("flat")) {
            prepareFlatFakePlayerRendering(poseStack, entity, new Vec3(x, 0.0, z));
        } else if (effect.equalsIgnoreCase("gigant")) {
            applyGiant(poseStack, partialTicks);
        } else if (effect.equalsIgnoreCase("ghost")) {
            applyGhostEffect(poseStack, entity, partialTicks);
        }
    }

    static Map<UUID, Vec3> lastDeltaMoventent = new HashMap<>();

    public static final List<GhostSnapshot> ghosts = new ArrayList<>();

    public static void applyGhostEffect(PoseStack poseStack, Entity entity, float partialTicks) {
        Vec3 velocity = entity.getPosition(partialTicks);
        Vec3 velocity2 = lastDeltaMoventent.get(entity.getUUID()) != null ? lastDeltaMoventent.get(entity.getUUID()) : new Vec3(0, 0, 0) ;
        boolean isMoving = Math.abs((velocity.x - velocity2.x)) > 0.01 || Math.abs((velocity.z - velocity2.z)) > 0.01 || Math.abs((velocity.y - velocity2.y)) > 0.01;
        if (isMoving || Math.abs(velocity2.x) < 1 || Math.abs(velocity2.z) < 1 || Math.abs(velocity2.y) < 1) {
            lastDeltaMoventent.put(entity.getUUID(), velocity);
            //System.out.println("nicht moving " + entity.getName() + " velocae: " + Math.abs(velocity.x) + " vel2 " + Math.abs(velocity2.x) );
        }
        if (isMoving) {
            boolean isFrozen = false;
            if (entity.level() != null) {
                // Der TickRateManager verwaltet den Status der Spielgeschwindigkeit
                isFrozen = entity.level().tickRateManager().isFrozen();
            }
            if (entity.tickCount % 3 == 0 && !isFrozen) {
                PlayerData playerData = HNessentials.getInstance().getPlayerData().get(entity.getUUID());
                Minecraft mc = Minecraft.getInstance();
                ClientLevel level = mc.level;
                if (level == null) return;
                PlayerSkin otherSkin = null;
                Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
                if (entity instanceof Player playerEntity) {
                    GameProfile gameProfile = playerEntity.getGameProfile();
                    otherSkin = mc.getSkinManager().getInsecureSkin(gameProfile);
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        equipment.put(slot, playerEntity.getItemBySlot(slot).copy());
                    }
                }

                if (entity instanceof Player player) {
                    PlayerRendereType savedPlayerData = new PlayerRendereType(Minecraft.getInstance(), entity.getUUID(), entity.getName().getString(), playerData, otherSkin.model() == PlayerSkin.Model.SLIM);
                    long startTime = System.currentTimeMillis();
                    if (savedPlayerData.getModel() == null) return;


                    if (player instanceof LocalPlayer Lplayer) {
                        SavedPlayerModel savedPlayerModel = new SavedPlayerModel(Lplayer.clientLevel, Lplayer);
                        ghosts.add(new GhostSnapshot(
                                player.position(),
                                player.yBodyRot,
                                player.yHeadRot,
                                player.getXRot(),
                                otherSkin,
                                entity.tickCount,
                                startTime,
                                savedPlayerData,
                                player.walkAnimation.position(),
                                player.walkAnimation.speed(),
                                player.attackAnim,
                                player.isCrouching(),
                                player.isSwimming(),
                                player.isFallFlying(),
                                equipment,
                                savedPlayerModel
                        ));
                    }


                }

            }
        }
    }

    public static void applyGhostEffect(PoseStack poseStack, FakePlayer entity, float partialTicks) {
        Vec3 velocity = entity.getVelocity();
        Vec3 velocity2 = lastDeltaMoventent.get(entity.getUUID()) != null ? lastDeltaMoventent.get(entity.getUUID()) : new Vec3(0, 0, 0) ;
        boolean isMoving = Math.abs((velocity.x - velocity2.x)) > 0.01 || Math.abs((velocity.z - velocity2.z)) > 0.01 || Math.abs((velocity.y - velocity2.y)) > 0.01;
        if (isMoving || Math.abs(velocity2.x) < 1 || Math.abs(velocity2.z) < 1 || Math.abs(velocity2.y) < 1) {
            lastDeltaMoventent.put(entity.getUUID(), velocity);
            //System.out.println("nicht moving " + entity.getName() + " velocae: " + Math.abs(velocity.x) + " vel2 " + Math.abs(velocity2.x) );
        }
        if (isMoving) {
            boolean isFrozen = false;
            assert Minecraft.getInstance().player != null;
            isFrozen = Minecraft.getInstance().player.level().tickRateManager().isFrozen();
            if (entity.tickCount % 3 == 0 && !isFrozen) {
                PlayerData playerData = HNessentials.getInstance().getPlayerData().get(entity.getUUID());
                Minecraft mc = Minecraft.getInstance();
                ClientLevel level = mc.level;
                if (level == null) return;
                PlayerSkin defaultSkin = DefaultPlayerSkin.get(entity.getUUID());
                ResourceLocation tex = defaultSkin.texture();
                PlayerRendereType savedPlayerData = new PlayerRendereType(Minecraft.getInstance(), entity.getUUID(), entity.getName(), playerData, mc.player.getSkin().model() == PlayerSkin.Model.SLIM);
                if (savedPlayerData.getModel() == null) return;
                ghosts.add(new GhostSnapshot(
                        entity.getVelocity(),
                        entity.yRotBody,
                        entity.yRotHead,
                        entity.getXRot(0),
                        defaultSkin,
                        entity.tickCount,
                        System.currentTimeMillis(),
                        savedPlayerData,
                        0.0f,
                        0.0f,
                        0.0f,
                        entity.isSneaking(),
                        false,
                        false,
                        new HashMap<>(),
                        null
                ));
            }
        }
    }

    public static void applyEchoEffect(PoseStack poseStack, float partialTicks) {
        float time = (Minecraft.getInstance().player.level().getGameTime() + partialTicks);

        // Deutlich größerer Radius (1.5 Blöcke weit schwingen)
        float offsetX = Mth.sin(time * 0.5f) * 1.5f;
        float offsetZ = Mth.cos(time * 0.5f) * 1.5f;

        // Verschiebung
        poseStack.translate(offsetX, 0, offsetZ);

        // Stärkere Neigung (bis zu 45 Grad)
        poseStack.mulPose(Axis.ZP.rotationDegrees(offsetX * 30.0f));
    }

    public static void applyHologram(PoseStack poseStack, float partialTicks) {
        float time = (Minecraft.getInstance().player.level().getGameTime() + partialTicks);

        // Sichtbares Zittern (0.2 Blöcke hin und her)
        float shake = Mth.sin(time * 4.0f) * 0.2f;
        poseStack.translate(shake, 0, 0);

        // Scan-Welle: Wir machen den Spieler kurzzeitig sehr breit
        float scan = Mth.sin(time * 0.2f);
        if (scan > 0.7f) {
            // Spieler wird flach und breit wie eine gestörte Projektion
            poseStack.scale(1.5f, 0.8f, 1.5f);
        }


    }

    public static void applyGiant(PoseStack poseStack, float partialTicks) {
        float time = (Minecraft.getInstance().player.level().getGameTime() + partialTicks);
        float scale = 2.0f + Mth.sin(time * 0.1f) * 1.0f; // Pulsiert zwischen 1x und 3x Größe
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -(1.0 - scale) * 0.5, 0); // Hält die Füße am Boden
    }
}