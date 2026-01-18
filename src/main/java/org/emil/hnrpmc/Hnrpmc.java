package org.emil.hnrpmc;

import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook;
import org.emil.hnrpmc.simpleclans.overlay.ServerTickNamesHandler;
import org.emil.hnrpmc.simpleclans.proxy.dto.BungeePayload;
import org.slf4j.Logger;

// GeckoLib Imports
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import net.neoforged.fml.config.ModConfig;


import net.minecraft.resources.ResourceKey;

import java.util.UUID;


@Mod(Hnrpmc.MODID)
public class Hnrpmc {
    public static final String MODID = "hnrpmc";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static Hnrpmc instance;

    public static Hnrpmc getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        // Erstellt einen Registrar für den Namespace "bungeecord"
        final PayloadRegistrar registrar = event.registrar("bungeecord");

        // Wir registrieren das Paket für die "Play"-Phase in Richtung Client/Proxy
        registrar.playToClient(
                BungeePayload.TYPE,
                BungeePayload.CODEC,
                (payload, context) -> {}
        );
    }

    public static LuckPerms getLuckPerms() {
        if (!ModList.get().isLoaded("luckperms")) {
            return null;
        }

        try {
            // Dieser Aufruf funktioniert erst, wenn LP die "setup" Phase beendet hat
            return LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public Hnrpmc(IEventBus modEventBus) {

        instance = this;

        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            modEventBus.addListener(org.emil.hnrpmc.simpleclans.overlay.ClientHandler::onAddLayers);
        }

        modEventBus.addListener(this::onRegisterPayloads);
        new org.emil.hnrpmc.simpleclans.SimpleClans(modEventBus);
        new org.emil.hnrpmc.hnclaim.HNClaims(modEventBus);
        new org.emil.hnrpmc.hnessentials.HNessentials(modEventBus);


    }

    @SubscribeEvent
    private void commonSetup(FMLCommonSetupEvent event) {
        instance = this;
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {


    }

}