package org.emil.hnrpmc.hnessentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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

public final class HomeCommands extends ClanSBaseCommand {

    private final HNessentials plugin;

    private final CommandHelper commandHelper;

    public HomeCommands(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("home");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return home(root);
    }

    private LiteralArgumentBuilder<CommandSourceStack> home(String root) {
        return Commands.literal(root)
                .then(Commands.argument("Home", StringArgumentType.greedyString())
                        .suggests(Suggestions.PlayerHomes(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String homename = StringArgumentType.getString(ctx, "Home");
                            String splithome = "";
                            String splitplayername = "";
                            if (homename.contains(":")) {
                                splitplayername = homename.split(":")[0];
                                splithome = homename.split(":")[1];
                            } else {
                                splithome = homename;
                                splitplayername = player.getName().getString();
                            }
                            UUID uuid = plugin.getServer().getProfileCache().get(splitplayername).get().getId();
                            Home home = plugin.getHomeManager().getHomeByName(splithome, splitplayername, player);
                            if (home == null) {
                                player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§cHaus mit dem namen {} konnte nicht gefunden werden", splithome)));
                                return 0;
                            }
                            if (home.getCoords().y == -1000){
                                player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§cKein Bett gefunden")));
                                return 0;
                            }

                            player.teleportTo(commandHelper.getLevelByName(home.getWorld_name()), home.getCoords().x, home.getCoords().y, home.getCoords().z, 0, 0);

                            player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§7Du wurdest erfolgreich zum Haus {} telepotiert", splithome)));

                            return 1;
                        }

                        )
                );
    }
}
