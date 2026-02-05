package org.emil.hnrpmc.hnessentials.cosmetics.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.GameType;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerTabOverlay {

    @Redirect(
            method = "getPlayerInfos",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;", remap = false)
    )
    private List<PlayerInfo> hnrpmc$modifyTabList(Stream<PlayerInfo> instance) {
        List<PlayerInfo> originalList = instance.collect(Collectors.toList());

        if (HNessentials.getInstance().getHNPlayerData() != null) {
            originalList.removeIf(pli -> HNessentials.getInstance().getHNPlayerData().get(pli.getProfile().getId()).isVanish());
        }

        return originalList;
    }

    @Inject(method = "decorateName", at = @At("RETURN"), cancellable = true)
    private void hnrpmc$onDecorateName(PlayerInfo playerInfo, MutableComponent name, CallbackInfoReturnable<Component> cir) {
        UUID uuid = playerInfo.getProfile().getId();
        var playerDataMap = HNessentials.getInstance().getHNPlayerData();

        if (playerDataMap != null && playerDataMap.containsKey(uuid)) {
            HNPlayerData data = playerDataMap.get(uuid);

            if (data.isAfk()) {
                boolean showAfkTag = (System.currentTimeMillis() / 3000) % 2 == 0;

                MutableComponent resultName;
                if (showAfkTag) {
                    resultName = Component.literal("AFK");
                } else {
                    resultName = name.copy();
                }

                resultName.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

                cir.setReturnValue(resultName);
            }
        }
    }
}
