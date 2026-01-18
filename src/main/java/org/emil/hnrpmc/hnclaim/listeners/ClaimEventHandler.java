package org.emil.hnrpmc.hnclaim.listeners;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.claimperms;
import org.emil.hnrpmc.hnclaim.managers.ClaimManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimEventHandler {

    private final HNClaims plugin;
    //private final SettingsManager settingsManager;
    private final ClaimManager claimManager;

    public ClaimEventHandler(@NotNull HNClaims plugin) {
        this.plugin = plugin;
        //this.settingsManager = plugin.getSettingsManager();
        this.claimManager = plugin.getClaimManager();
    }

    @SubscribeEvent
    public void blockmined(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        plugin.getLogger().debug("block break");
        //Claim claim = claimManager.getClaimbyPlayerPos((ServerPlayer) event.getPlayer());
        Claim claim = claimManager.getClaimbyPos(event.getPos().getCenter(), level.dimension().location().toString(),0.0);
        if (claim != null) {
            plugin.getLogger().debug("block break2 {}", claim.getPerms());
            if (!claim.getPerms().contains(claimperms.BREAK_BLOCKS)){
                plugin.getLogger().debug("block break3 " + event.getPlayer().getName());
                if (!plugin.getPermissionsManager().has(event.getPlayer(), claimperms.BREAK_BLOCKS)) {
                    event.setCanceled(true);
                    event.getPlayer().displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim abzubauen"), true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void blockplaced(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        if (event.getEntity() instanceof Player player) {
            //Claim claim = claimManager.getClaimbyPlayerPos((ServerPlayer) event.getPlayer());
            Claim claim = claimManager.getClaimbyPos(event.getPos().getCenter(), level.dimension().location().toString(),0.0);
            if (claim != null) {
                if (!claim.getPerms().contains(claimperms.PLACE_BLOCKS)){
                    if (!plugin.getPermissionsManager().has((ServerPlayer) player, claimperms.PLACE_BLOCKS)) {
                        event.setCanceled(true);
                        player.displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim Block zu plazieren"), true);
                        return;
                    }
                } else {
                    
                }
            }
        }
    }

    @SubscribeEvent
    public void blockinteractions(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

        if (ClaimPlayerListener.isHoldingTool(player.getMainHandItem()) || ClaimPlayerListener.isHoldingTool(player.getOffhandItem())) return;

        Claim claim = claimManager.getClaimbyPos(pos.getCenter(), level.dimension().location().toString(), 0.0);

        if (claim != null) {
            boolean hasInventory = state.getMenuProvider(level, pos) != null;
            boolean isMachine = level.getBlockEntity(pos) != null;

            if (hasInventory || isMachine) {
                if (!claim.getPerms().contains(claimperms.BLOCK_INTERACTIONS)) {

                    if (!plugin.getPermissionsManager().has(player, claimperms.BLOCK_INTERACTIONS)) {
                        event.setCanceled(true);
                        player.displayClientMessage(Component.literal("§cDieser Block ist geschützt!"), true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void itemdrop(ItemTossEvent event) {
        Level level = event.getEntity().level();
        //Claim claim = claimManager.getClaimbyPlayerPos((ServerPlayer) event.getPlayer());
        Claim claim = claimManager.getClaimbyPos(event.getEntity().getPosition(0), level.dimension().location().toString(),0.0);
        if (claim != null) {
            if (!claim.getPerms().contains(claimperms.DROP_ITEM)){
                if (!plugin.getPermissionsManager().has(event.getPlayer(), claimperms.DROP_ITEM)) {
                    event.setCanceled(true);
                    event.getPlayer().displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim Items wegzuwerfen"), true);
                    return;
                }
            } else {
                
            }
        }
    }

    @SubscribeEvent
    public void itempickup(ItemEntityPickupEvent.Pre event) {
        Level level = event.getPlayer().level();
        //Claim claim = claimManager.getClaimbyPlayerPos((ServerPlayer) event.getPlayer());
        Claim claim = claimManager.getClaimbyPos(event.getPlayer().getPosition(0), level.dimension().location().toString(),0.0);
        if (claim != null) {
            if (!claim.getPerms().contains(claimperms.PICKUP_ITEM)){
                if (!plugin.getPermissionsManager().has(event.getPlayer(), claimperms.PICKUP_ITEM)) {
                    event.setCanPickup(TriState.FALSE);
                    event.getPlayer().displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim Items aufzuheben"), true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void entityinteraction(PlayerInteractEvent.EntityInteract event) {
        Level level = event.getEntity().level();
        if (event.getEntity() instanceof Player player) {
            //Claim claim = claimManager.getClaimbyPlayerPos((ServerPlayer) event.getPlayer());
            Claim claim = claimManager.getClaimbyPos(event.getEntity().getPosition(0), level.dimension().location().toString(), 0.0);
            if (claim != null) {
                if (!claim.getPerms().contains(claimperms.ENTITY_INTERACTIONS)) {
                    if (!plugin.getPermissionsManager().has(player, claimperms.ENTITY_INTERACTIONS)) {
                        event.setCanceled(true);
                        player.displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim mit Entitys interagieren"), true);
                        return;
                    }
                } else {
                    
                }
            }
        }else {
            plugin.getServer();
        }
    }

    @SubscribeEvent
    public void onDamage(LivingIncomingDamageEvent event) {
        var server = event.getEntity().getServer();
        if (server == null) return;

        Entity attacker = event.getSource().getEntity();
        Entity directSource = event.getSource().getDirectEntity();

        // PvP Bypass
        if (attacker instanceof ServerPlayer attackerpl) {
            //if (plugin.getClaimManager().getClaimbyPlayerPos(attackerpl) == null) return;
            if (event.getEntity() instanceof ServerPlayer victem) {
                if (plugin.getClaimManager().getClaimbyPlayerPos(victem) != null || plugin.getClaimManager().getClaimbyPlayerPos(attackerpl) != null) {
                    if (!plugin.getPermissionsManager().has(attackerpl, claimperms.PVP, Optional.of(true))) {
                        event.setCanceled(true);
                        attackerpl.displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim andere spieler zu schlagen"), true);
                        return;
                    }
                }
            } else {
                if (plugin.getClaimManager().getClaimbyPlayerPos(attackerpl) == null) return;
                if (!plugin.getPermissionsManager().has(attackerpl, claimperms.HITENTITYS, Optional.of(true))) {
                    event.setCanceled(true);
                    attackerpl.displayClientMessage(Component.literal("§cDu hast keine berechtigungen in diesem Claim Mobs zu schlagen"), true);
                    return;
                }

            }

            // Explosion Damage Bypass
            if (directSource instanceof Creeper) {
                if (!plugin.getPermissionsManager().has(attackerpl, claimperms.PVP, Optional.of(true))) event.setCanceled(true);
            }
            else if ((directSource instanceof PrimedTnt || directSource instanceof MinecartTNT)) {
                if (!plugin.getPermissionsManager().has(attackerpl, claimperms.PVP, Optional.of(true))) event.setCanceled(true);
            }
            else if (directSource instanceof EndCrystal) {
                if (!plugin.getPermissionsManager().has(attackerpl, claimperms.PVP, Optional.of(true))) event.setCanceled(true);
            }
        }

    }

    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Detonate event) {
        var server = event.getLevel().getServer();
        if (server == null) return;

        Entity source = event.getExplosion().getDirectSourceEntity();
        Entity indirect = event.getExplosion().getIndirectSourceEntity();

        List<Entity> affectedEntitys = event.getAffectedEntities();

        List<BlockPos> affectedBlocks = event.getAffectedBlocks();

        if (affectedEntitys.isEmpty()) {
            Iterator<Entity> Entityiterator = affectedEntitys.iterator();
            while (Entityiterator.hasNext()) {
                Entity pos = Entityiterator.next();
                Claim claim = claimManager.getClaimbyPos(pos.getPosition(0), event.getLevel().dimension().location().toString());
                //plugin.getLogger().debug("got block {}", pos);
                if (claim != null) {
                    ServerPlayer sp = null;
                    if (indirect != null) {
                        sp = claimManager.getServerPlayer(indirect.getUUID());
                    }
                    if (!plugin.getPermissionsManager().has(sp, claimperms.PVP, Optional.of(true), Entityiterator.next())) {
                        Entityiterator.remove();
                    }
                }
            }
        }


        Iterator<BlockPos> iterator = affectedBlocks.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            Claim claim = claimManager.getClaimbyPos(pos.getCenter(), event.getLevel().dimension().location().toString());
            //plugin.getLogger().debug("got block {}", pos);
            if (claim != null) {
                if (!claim.getPerms().contains(claimperms.EXPLODE_BLOCKS)) {
                    iterator.remove();
                }
            }
        }


    }


    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event) {
        var server = event.getLevel().getServer();
        if (server == null) return;

        Entity source = event.getExplosion().getDirectSourceEntity();
        Entity indirect = event.getExplosion().getIndirectSourceEntity();

        if (source instanceof Creeper) {
            if (!plugin.getPermissionsManager().has(null, claimperms.EXPLODE_BLOCKS, Optional.of(true), source)) event.setCanceled(true);
        }
        else if ((source instanceof PrimedTnt || source instanceof MinecartTNT)) {
            if (source instanceof PrimedTnt tnt) {
                ServerPlayer sp = claimManager.getServerPlayer(tnt.getOwner().getUUID());
                if (!plugin.getPermissionsManager().has(null, claimperms.EXPLODE_BLOCKS, Optional.of(true), source)) {
                    if (sp != null) {
                        sp.displayClientMessage(Component.literal("§cTNT-Explosionen sind hier deaktiviert!"), true);
                    }

                    event.setCanceled(true);
                }
            } else if (source instanceof MinecartTNT tnt) {
                ServerPlayer sp = null;
                if (indirect != null) {
                    sp = plugin.getServer().getPlayerList().getPlayer(indirect.getUUID());
                }
                if (!plugin.getPermissionsManager().has(sp, claimperms.EXPLODE_BLOCKS, Optional.of(true), source)) {
                    if (sp != null) {
                        sp.displayClientMessage(Component.literal("§cTNT-Explosionen sind hier deaktiviert!"), true);
                    }

                    event.setCanceled(true);
                }
            }

        }
        else if (source instanceof EndCrystal) {
            ServerPlayer sp = null;
            if (indirect != null) {
                sp = plugin.getServer().getPlayerList().getPlayer(indirect.getUUID());
            }

            if (!plugin.getPermissionsManager().has((ServerPlayer) indirect, claimperms.EXPLODE_BLOCKS, Optional.of(true), source)) {
                if (sp != null) {
                    sp.displayClientMessage(Component.literal("§cKristall-Explosionen sind hier deaktiviert!"), true);
                }
                event.setCanceled(true);
            }
        }
    }
}
