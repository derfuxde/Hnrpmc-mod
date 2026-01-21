package org.emil.hnrpmc.hnessentials.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.emil.hnrpmc.Hnrpmc; // Nutze hier deine MODID
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.menu.PetMorphScreen;
import org.emil.hnrpmc.hnessentials.network.OpenAdminScreenPayload;
import org.emil.hnrpmc.hnessentials.network.ScoreSyncPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
// Importiere deine eigenen Klassen (Pfade anpassen!)
import org.emil.hnrpmc.hnessentials.network.OpenAdminScreenPayload;
import org.emil.hnrpmc.hnessentials.network.AdminUpdateDataPayload;
import org.emil.hnrpmc.hnessentials.network.ClientPacketHandler;
import org.emil.hnrpmc.hnessentials.network.ServerPacketHandler;

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
        Player player = event.getEntity();
        Level level = event.getLevel();

        // WICHTIG: Nur auf dem Client ausführen, wenn es um GUIs geht
        if (!level.isClientSide) return;

        if (player.isShiftKeyDown() && event.getHand() == InteractionHand.MAIN_HAND) {
            if (event.getTarget() instanceof TamableAnimal pet && pet.isTame()) {
                if (player.getUUID().equals(pet.getOwnerUUID())) {

                    // Paket an Server senden (Daten anfragen)
                    PacketDistributor.sendToServer(new PlayerDataRequestPayload(pet.getUUID()));

                    // GUI öffnen über eine sichere Client-Methode
                    if (HNessentials.clientVipScore == 2) {
                        openPetScreen(pet);
                    }
                }
            }
        }
    }

    // Diese Methode isoliert den Zugriff auf die Minecraft-Klasse
    private static void openPetScreen(TamableAnimal pet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new PetMorphScreen(pet, HNessentials.getInstance()));
        }
    }
}