package org.emil.hnrpmc.hnessentials.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;

import java.util.Map;

public record GhostSnapshot(
        Vec3 position,
        float yBodyRot,
        float yHeadRot,
        float xRot,
        PlayerSkin PlayerSkin,
        int startTick,
        long startTime,
        PlayerRendereType PlayerSaved,
        float walkAnimPos,
        float walkAnimSpeed,
        float swingTime,
        boolean isCrouching,
        boolean isSwimming,
        boolean isFallFlying,
        Map<EquipmentSlot, ItemStack> equipment,
        SavedPlayerModel AbstractClientPlayer
) {}