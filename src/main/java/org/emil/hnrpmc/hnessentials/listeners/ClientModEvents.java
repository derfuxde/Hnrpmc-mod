package org.emil.hnrpmc.hnessentials.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.Hnrpmc; // Nutze hier deine MODID
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.menu.PetMorphScreen;
import org.emil.hnrpmc.hnessentials.network.ScoreSyncPayload;

import static org.emil.hnrpmc.hnessentials.listeners.PlayerEventLister.getPlayerScore;

@EventBusSubscriber(modid = "hnrpmc", value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Wolf wolf) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new PlayerDataRequestPayload(wolf.getUUID())
            );
        }
    }

    @SubscribeEvent
    public static void onPetInteract(PlayerInteractEvent.EntityInteract event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();

        if (player.isShiftKeyDown() && event.getHand() == InteractionHand.MAIN_HAND) {
            if (event.getTarget() instanceof TamableAnimal pet && pet.isTame()) {

                if (player.getUUID().equals(pet.getOwnerUUID())) {

                    PacketDistributor.sendToServer(
                            new PlayerDataRequestPayload(pet.getUUID())
                    );

                    if (HNessentials.clientVipScore == 2){
                        net.minecraft.client.Minecraft.getInstance().setScreen(new PetMorphScreen(pet, HNessentials.getInstance()));
                    }
                }
            }
        }
    }
}