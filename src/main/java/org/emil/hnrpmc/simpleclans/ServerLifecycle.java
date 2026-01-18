package org.emil.hnrpmc.simpleclans;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.simpleclans.storage.SQLiteCore;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public final class ServerLifecycle {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent e) {
        MinecraftServer server = e.getServer();
        SimpleClans.getInstance().serverStartup(server);
    }
}
