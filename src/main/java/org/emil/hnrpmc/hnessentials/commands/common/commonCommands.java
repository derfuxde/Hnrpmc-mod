package org.emil.hnrpmc.hnessentials.commands.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.Tpa;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.hnessentials.managers.StorageManager;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

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
        return List.of("fly", "watch");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        if (rootLiteral.equals("fly")) {
            dispatcher.register(fly(rootLiteral));
        } else if (rootLiteral.equals("watch")) {
            dispatcher.register(watch(rootLiteral));
        }
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return fly(root);
    }

    private LiteralArgumentBuilder<CommandSourceStack> fly(String root) {
        return Commands.literal(root)
                .executes(this::executefly)
                .then(Commands.argument("Spieler", EntityArgument.player())
                        .executes(this::executefly)
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> watch(String root) {
        return Commands.literal(root)
                .executes(this::executewatch)
                .then(Commands.argument("Spieler", EntityArgument.player())
                        .executes(this::executewatch)
                );
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

    private int executewatch(CommandContext<CommandSourceStack> ctx) {
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

        ServerPlayer source = ctx.getSource().getPlayer();

        if (source == null) return 0;

        if (source == player) {

            source.setInvisible(false);

            source.setCamera(player);
        } else {
            source.setInvisible(true);

            source.setCamera(player);
        }

        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(commandHelper.formatMessage("Du schaust jetzt {} zu", player.getName().getString())));

        return 1;
    }
}
