package org.emil.hnrpmc.hnessentials.commands.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.hnessentials.network.ServerPacketHandler;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class commonCommands extends ClanSBaseCommand {

    private final HNessentials plugin;

    private final CommandHelper commandHelper;

    public commonCommands(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("fly", "watch", "restart", "vanish", "afk");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        if (rootLiteral.equals("fly")) {
            dispatcher.register(fly(rootLiteral));
        } else if (rootLiteral.equals("watch")) {
            dispatcher.register(watch(rootLiteral));
        } else if (rootLiteral.equals("restart")) {
            dispatcher.register(restart(rootLiteral));
        } else if (rootLiteral.equals("vanish")) {
            dispatcher.register(vanish(rootLiteral));
        } else if (rootLiteral.equals("afk")) {
            dispatcher.register(afk(rootLiteral));
        }
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return fly(root);
    }

    public LiteralArgumentBuilder<CommandSourceStack> afk(String root) {
        return Commands.literal(root)
                .requires(s -> s.getPlayer() != null)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());

                    if (playerData == null) return 0;

                    playerData.setAfk(!playerData.isAfk());

                    plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
                    plugin.getStorageManager().save(player.getUUID());

                    ServerPacketHandler.sendDataToAll();

                    for (ServerPlayer p : plugin.getServer().getPlayerList().getPlayers()) {
                        if (p.getUUID().equals(player.getUUID())) continue;
                        p.sendSystemMessage(Component.literal(playerData.isAfk() ? commandHelper.formatMessage("§7* §v{}§7 ist nun abwesend.", player.getName().getString()) : commandHelper.formatMessage("§7* §v{}§7 ist wieder da.", player.getName().getString())));
                    }

                    context.getSource().sendSystemMessage(Component.literal(
                            playerData.isAfk() ? "§7Du bist nun abwesend." : "§7Du bist nicht länger abwesend."));
                    return 1;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> vanish(String root) {
        return Commands.literal(root)
                .requires(s -> Conditions.permission(s.getPlayer(), "essentials.admin.vanish"))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());

                    if (playerData == null) return 0;
                    boolean isVanished = toggleVanish(player);

                    for (ServerPlayer sp : plugin.getServer().getPlayerList().getPlayers()) {
                        HNPlayerData splayerData = plugin.getStorageManager().getOrCreatePlayerData(sp.getUUID());
                        if (PermissionsManager.has(sp, "essentials.admin.vanish.see")) {
                            if (!splayerData.containsTag("vanish_see")) {
                                sp.getTags().add("vanish_see");
                                splayerData.addTag("vanish_see");
                            }
                        } else {
                            sp.getTags().remove("vanish_see");
                            splayerData.removeTag("vanish_see");
                        }

                        plugin.getStorageManager().setPlayerData(sp.getUUID(), splayerData);
                        plugin.getStorageManager().save(sp.getUUID());
                    }

                    playerData.setVanish(isVanished);

                    plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
                    plugin.getStorageManager().save(player.getUUID());

                    ServerPacketHandler.sendDataToAll();

                    context.getSource().sendSuccess(() -> Component.literal(
                            isVanished ? "Du bist nun im Vanish!" : "Vanish deaktiviert!"), true);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> fly(String root) {
        return Commands.literal(root)
                .requires(ctx -> Conditions.permission(ctx.getPlayer(), "essentials.admin.fly"))
                .executes(this::executefly)
                .then(Commands.argument("Spieler", EntityArgument.player())
                        .executes(this::executefly)
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> watch(String root) {
        return Commands.literal(root)
                .requires(ctx -> Conditions.permission(ctx.getPlayer(), "essentials.admin.watch"))
                .executes(this::executewatch)
                .then(Commands.argument("entity", EntityArgument.entity())
                        .executes(this::executewatch)
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> restart(String root) {
        return Commands.literal(root)
                .requires(ctx -> Conditions.permission(ctx.getPlayer(), "essentials.admin.restart"))
                .executes(this::executerestart);
    }

    private int executerestart(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        ctx.getSource().sendSuccess(() -> Component.literal("Server wird neu gestartet...").withStyle(ChatFormatting.RED), true);

        ctx.getSource().getServer().halt(false);

        return 1;
    }

    private int executefly(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = null;
        try {
            player = EntityArgument.getPlayer(ctx, "Spieler");
        } catch (Exception e) {
            try {
                player = ctx.getSource().getPlayerOrException();
            } catch (CommandSyntaxException ex) {
                throw new RuntimeException(ex);
            }
            //throw new RuntimeException(e);
        }
        if (player == null) return 0;

        player.onUpdateAbilities();

        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(commandHelper.formatMessage("Fly wurde für {} {}", player.getName().getString(), player.mayFly() ? "aktiviert" : "deaktivert")));

        return 1;
    }

    Vec3 lastpos = null;
    ServerLevel lastLevel = null;
    GameType beforgamemode = null;

    private int executewatch(CommandContext<CommandSourceStack> ctx) {
        Entity player = null;
        try {
            player = EntityArgument.getEntity(ctx, "entity");
        } catch (Exception e) {
            try {
                player = ctx.getSource().getPlayerOrException();
            } catch (CommandSyntaxException ex) {
                throw new RuntimeException(ex);
            }
            //throw new RuntimeException(e);
        }
        if (player == null) return 0;

        ServerPlayer source = ctx.getSource().getPlayer();

        if (source == null) return 0;

        HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(source.getUUID());

        if (playerData == null) return 0;

        if (source == player) {

            playerData.setWatchingPlayer(null);

            source.setGameMode(beforgamemode);

            source.setInvisible(false);

            if (lastpos != null) {
                if (lastLevel == null) {
                    source.teleportTo(lastpos.x, lastpos.y, lastpos.z);
                } else {
                    source.teleportTo(lastLevel.getLevel(), lastpos.x, lastpos.y, lastpos.z, 0.0f, 0.0f);
                }
            }
            lastpos = null;
            lastLevel = null;
            beforgamemode = null;
            //source.setCamera(player);
        } else {
            playerData.setWatchingPlayer(player.getUUID().toString());
            source.setInvisible(true);
            beforgamemode = beforgamemode == null ? source.gameMode.getGameModeForPlayer() : beforgamemode;
            lastLevel = lastLevel == null ? source.serverLevel() : lastLevel;
            lastpos = lastpos == null ? source.getPosition(0) : lastpos;

            source.setGameMode(GameType.SPECTATOR);


            //source.setCamera(player);
        }

        plugin.getStorageManager().setPlayerData(source.getUUID(), playerData);
        plugin.getStorageManager().save(source.getUUID());

        ServerPacketHandler.sendoneDataToAll(source.getUUID());

        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(commandHelper.formatMessage("Du schaust jetzt {} zu", player.getName().getString())));

        return 1;
    }

    private static boolean toggleVanish(ServerPlayer player) {
        if (player.getTags().contains("vanished")) {
            player.removeTag("vanished");
            player.setInvisible(false);
            return false;
        } else {
            player.addTag("vanished");
            player.setInvisible(true);
            return true;
        }
    }
}
