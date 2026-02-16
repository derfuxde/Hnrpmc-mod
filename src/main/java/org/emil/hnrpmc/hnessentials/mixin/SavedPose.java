package org.emil.hnrpmc.hnessentials.mixin;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.ElytraLayerAccessor;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.ElytraModelAccessor;

public class SavedPose {
    public final PartPose headPose;
    public final PartPose bodyPose;
    public final PartPose rightArmPose;
    public final PartPose leftArmPose;
    public final PartPose rightLegPose;
    public final PartPose leftLegPose;
    public PartPose leftWing;
    public PartPose rightWing;

    public final boolean isFlying, isSwimming;

    public SavedPose(PlayerModel<?> model, ElytraLayer<LivingEntity, ElytraModel<LivingEntity>> EL, boolean isFlying, boolean isSwimming) {
        this.headPose = model.head.storePose();
        this.bodyPose = model.body.storePose();
        this.rightArmPose = model.rightArm.storePose();
        this.leftArmPose = model.leftArm.storePose();
        this.rightLegPose = model.rightLeg.storePose();
        this.leftLegPose = model.leftLeg.storePose();
        this.leftWing = null;
        this.rightWing = null;
        if (EL != null) {
            ElytraModel<LivingEntity> sourceElytra = ((ElytraLayerAccessor) EL).getElytraModel();
            ModelPart snapshotLeftWing = ((ElytraModelAccessor) sourceElytra).getLeftWing();
            ModelPart snapshotRightWing = ((ElytraModelAccessor) sourceElytra).getRightWing();

            leftWing = snapshotLeftWing.storePose();
            this.rightWing = snapshotRightWing.storePose();
        }

        this.isFlying = isFlying;
        this.isSwimming = isSwimming;
    }

    public void applyTo(PlayerModel<?> model) {
        model.head.loadPose(headPose);
        model.body.loadPose(bodyPose);
        model.rightArm.loadPose(rightArmPose);
        model.leftArm.loadPose(leftArmPose);
        model.rightLeg.loadPose(rightLegPose);
        model.leftLeg.loadPose(leftLegPose);

        model.hat.copyFrom(model.head);
    }
}