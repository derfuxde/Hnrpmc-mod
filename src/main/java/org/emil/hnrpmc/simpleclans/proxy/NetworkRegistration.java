package org.emil.hnrpmc.simpleclans.proxy;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.emil.hnrpmc.Hnrpmc; // Dein Haupt-Mod-Klassen-Pfad

@EventBusSubscriber(modid = Hnrpmc.MODID)
public class NetworkRegistration {
    public static boolean IS_REGISTERED = false;

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Hnrpmc.MODID);

        registrar.playToServer(SDLinkPayload.TYPE, SDLinkPayload.CODEC, (p, c) -> {});

        // Sobald das Event durchgelaufen ist, setzen wir den Status auf true
        IS_REGISTERED = true;
    }
}