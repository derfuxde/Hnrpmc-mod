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

    public HomeCommands(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
    }

    @Override
    public @Nullable String primarycommand() {
        return "";
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(home());
        dispatcher.register(sethome());
        dispatcher.register(delhome());
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root);
    }

    private LiteralArgumentBuilder<CommandSourceStack> home() {
        return Commands.literal("home")
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
                            if (home.getCoords().y == -1000){
                                player.sendSystemMessage(Component.literal(formatMessage("§cKein Bett gefunden")));
                                return 0;
                            }
                            if (home == null) {
                                player.sendSystemMessage(Component.literal(formatMessage("§cHaus mit dem namen {} konnte nicht gefunden werden", splithome)));
                                return 0;
                            }

                            player.teleportTo(getLevelByName(home.getWorld_name()), home.getCoords().x, home.getCoords().y, home.getCoords().z, 0, 0);

                            player.sendSystemMessage(Component.literal(formatMessage("§7Du wurdest erfolgreich zum Haus {} telepotiert", splithome)));

                            return 1;
                        }

                        )
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> sethome() {
        return Commands.literal("sethome")
                .then(Commands.argument("Home", StringArgumentType.greedyString())
                        .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                String homename = StringArgumentType.getString(ctx, "Home");
                                String splithome = "";
                                String splitplayername = "";
                                boolean cecklimit = true;
                                if (homename.contains(":")) {
                                    splitplayername = homename.split(":")[0];
                                    splithome = homename.split(":")[1];
                                } else {
                                    splithome = homename;
                                    splitplayername = player.getName().getString();
                                }

                                UUID uuid = plugin.getServer().getProfileCache().get(splitplayername).get().getId();
                                cecklimit = !player.server.getPlayerList().getPlayers().stream().filter(pl -> pl.getUUID().equals(uuid)).toList().isEmpty();
                                Home home = plugin.getHomeManager().getHomeByName(splithome, splitplayername, player);
                                if (cecklimit) {
                                    String maxhomes = plugin.getSettingsManager().getString(MAX_HOMES);
                                    String endmax = plugin.getSettingsManager().parseConditionalMessage(player, maxhomes);
                                    if (plugin.getHomeManager().getHomes(uuid).size() > Integer.parseInt(endmax)) {
                                        player.sendSystemMessage(Component.literal(formatMessage("§cDu hast die maximale Anzahl an Homes erreicht", splithome)));
                                        return 0;
                                    }
                                }
                                if (home != null) {
                                    player.sendSystemMessage(Component.literal(formatMessage("§cDas Haus mit dem namen {} exesiert bereits", splithome)));
                                    return 0;
                                }

                                Home newHome = new Home(uuid, player.getPosition(0), splithome, player.level().dimension().location().toString());
                                HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);
                                if (playerData == null) {
                                    player.sendSystemMessage(Component.literal(formatMessage("§cSpieler Daten für Spieler {} konnten nicht gefunden werden", splitplayername)));
                                    return 0;
                                }
                                List<Home> playerhomelist = playerData.getPlayerHomes();
                                playerhomelist.add(newHome);
                                playerData.setPlayerHomes(playerhomelist);
                                plugin.getStorageManager().setPlayerData(uuid, playerData);

                                player.sendSystemMessage(Component.literal(formatMessage("§7Haus {} wurde erfolgreich erstellt", splithome)));

                                return 1;

                        })
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> delhome() {
        return Commands.literal("delhome")
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
                                player.sendSystemMessage(Component.literal(formatMessage("§cHaus mit dem namen {} konnte nicht gefunden werden", splithome)));
                                return 0;
                            }

                            HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);
                            if (playerData == null) {
                                player.sendSystemMessage(Component.literal(formatMessage("§cSpieler Daten für Spieler {} konnten nicht gefunden werden", splitplayername)));
                                return 0;
                            }
                            List<Home> playerhomelist = playerData.getPlayerHomes();
                            playerhomelist.remove(home);
                            playerData.setPlayerHomes(playerhomelist);
                            plugin.getStorageManager().setPlayerData(uuid, playerData);

                            player.sendSystemMessage(Component.literal(formatMessage("§7Haus {} wurde erfolgreich gelöscht", splithome)));

                            return 1;

                        })
                );
    }

    public String formatMessage(String msg, Object... args) {
        if (!msg.contains("{}")) return msg;
        for (Object arg : args) {
            msg = msg.replaceFirst(java.util.regex.Pattern.quote("{}"), String.valueOf(arg));
        }
        return msg;
    }

    public ServerLevel getLevelByName(String worldName) {
        ResourceLocation location = ResourceLocation.parse(worldName);

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);

        return plugin.getServer().getLevel(key);
    }
}
