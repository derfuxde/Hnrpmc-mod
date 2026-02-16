package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class onRenderArmorPiece {

    @Inject(
            method = "renderArmorPiece",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void MixinonRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, LivingEntity entity,
                                    EquipmentSlot equipmentSlot, int packedLight, HumanoidModel<?> humanoidModel, CallbackInfo info) {

        if (entity instanceof Player) {
            HNPlayerData hnPlayerData = HNessentials.getInstance().getHNPlayerData().get(entity.getUUID());
            if (Minecraft.getInstance().player == null || hnPlayerData == null) return;
            HNPlayerData localhnPlayerData = HNessentials.getInstance().getHNPlayerData().get(Minecraft.getInstance().player.getUUID());

            if (hnPlayerData.isVanish() && !localhnPlayerData.getTags().contains("vanish_see")) {
                info.cancel();
            }
        }

    }
}
