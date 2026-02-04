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
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
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
        return List.of("fly", "watch", "restart", "vanish");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        if (rootLiteral.equals("fly")) {
            dispatcher.register(fly(rootLiteral));
        } else if (rootLiteral.equals("watch")) {
            dispatcher.register(watch(rootLiteral));
        }else if (rootLiteral.equals("restart")) {
            dispatcher.register(restart(rootLiteral));
        }else if (rootLiteral.equals("vanish")) {
            dispatcher.register(vanish(rootLiteral));
        }
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return fly(root);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> vanish(String root) {
        return Commands.literal(root)
                .requires(s -> Conditions.permission(s.getPlayer(), "essentials.admin.vanish"))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    boolean isVanished = toggleVanish(player);

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

        if (source == player) {

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
            source.setCamera(player);
        } else {
            source.setInvisible(true);
            beforgamemode = beforgamemode == null ? source.gameMode.getGameModeForPlayer() : beforgamemode;
            lastLevel = lastLevel == null ? source.serverLevel() : lastLevel;
            lastpos = lastpos == null ? source.getPosition(0) : lastpos;

            source.setGameMode(GameType.SPECTATOR);


            source.setCamera(player);
        }

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
