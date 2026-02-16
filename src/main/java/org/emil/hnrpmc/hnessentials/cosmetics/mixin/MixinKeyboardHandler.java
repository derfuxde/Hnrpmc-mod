package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.InputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.network.NotAfkanymoreRequest;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void hnrpmc$onAnyKeyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;


        if (action == 1) {
            if (mc.screen == null) {
                HNPlayerData playerData = HNessentials.getInstance().getHNPlayerData().get(mc.player.getUUID());

                List<Integer> thekeys = List.of(InputConstants.KEY_W, InputConstants.KEY_A, InputConstants.KEY_S, InputConstants.KEY_D, InputConstants.KEY_E);

                InputConstants.Key keyInstance = InputConstants.getKey(key, scanCode);
                if (playerData.isAfk()) {
                    if (thekeys.contains(key)) {
                        PacketDistributor.sendToServer(new NotAfkanymoreRequest(mc.player.getUUID()));
                    }
                }
            }
        }
    }
}