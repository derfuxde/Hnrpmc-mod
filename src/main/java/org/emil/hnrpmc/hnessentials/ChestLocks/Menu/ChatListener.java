package org.emil.hnrpmc.hnessentials.ChestLocks.Menu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.hnessentials.ChestLocks.config.LockData;
import org.emil.hnrpmc.hnessentials.HNessentials;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public class ChatListener {
    public static Map<UUID, BlockPos> awaitingPlayer = new HashMap<>();

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (awaitingPlayer.containsKey(player.getUUID())) {
            event.setCanceled(true); // Nachricht nicht öffentlich senden

            BlockPos pos = awaitingPlayer.remove(player.getUUID());
            String targetName = event.getMessage().getString();

            // Spieler suchen (Offline oder Online)
            player.getServer().getProfileCache().get(targetName).ifPresentOrElse(profile -> {
                BlockEntity be = player.level().getBlockEntity(pos);
                if (be != null && be.hasData(HNessentials.LOCK_DATA)) {
                    LockData current = be.getData(HNessentials.LOCK_DATA);
                    be.setData(HNessentials.LOCK_DATA, current.addTrusted(profile.getId()));
                    player.sendSystemMessage(Component.literal("§a" + targetName + " wurde vertraut!"));
                }
            }, () -> {
                player.sendSystemMessage(Component.literal("§cSpieler nicht gefunden."));
            });
        }
    }
}