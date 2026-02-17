package org.emil.hnrpmc.doc.listeners;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.doc.HNDoc;
import org.emil.hnrpmc.doc.menu.MainMenu;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.network.AskOpenVIPMenuRequest;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "hnrpmc", value = Dist.CLIENT)
public class clientlisten {
    public static final KeyMapping OPEN_DOC_MENU = new KeyMapping(
            "Open Doc Menü",
            GLFW.GLFW_KEY_F10,
            "key.categories.hnrpmc"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(clientlisten.OPEN_DOC_MENU);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        while (OPEN_DOC_MENU.consumeClick()) {
            HNDoc.getInstance().getLoader().loadFromGithub();
            mc.setScreen(new MainMenu(Component.literal("HNRPMC mod Wiki")));
        }
    }
}
