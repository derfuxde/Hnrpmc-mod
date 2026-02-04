package org.emil.hnrpmc.hnessentials.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.PlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.Playerish;
import org.emil.hnrpmc.hnessentials.listeners.PlayerDataRequestPayload;
import org.emil.hnrpmc.hnessentials.menu.VIPPlayerDataScreen;
import org.emil.hnrpmc.hnessentials.network.AskOpenVIPMenuRequest;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@EventBusSubscriber(modid = Hnrpmc.MODID, value = Dist.CLIENT)
public class ClientGhostManager {

    public static final KeyMapping OPEN_VIP_MENU = new KeyMapping(
            KeyMapping.CATEGORY_MISC,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.hnrpmc"
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused()) return;

        if (mc.level == null) return;


        //PacketDistributor.sendToServer(new PlayerDataRequestPayload(mc.player.getUUID()));

        while (OPEN_VIP_MENU.consumeClick() && HNessentials.clientVipScore >= 1) {
            PacketDistributor.sendToServer(new AskOpenVIPMenuRequest(mc.player.getUUID()));
            //mc.setScreen(new VIPPlayerDataScreen(mc.player.getUUID(), mc.player.getName().getString(), HNessentials.getInstance().getHNPlayerData().get(mc.player.getUUID())));
        }


        if (mc.player.tickCount % 1200 == 0) {
            PacketDistributor.sendToServer(new requestPlayerData(mc.player.getUUID()));
        }

    }
}
