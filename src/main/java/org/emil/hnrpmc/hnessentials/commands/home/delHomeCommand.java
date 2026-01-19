package org.emil.hnrpmc.hnessentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class delHomeCommand extends ClanSBaseCommand {

    private final HNessentials plugin;
    private final CommandHelper commandHelper;

    public delHomeCommand(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable String primarycommand() {
        return "delhome";
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return delhome(root);
    }

    private LiteralArgumentBuilder<CommandSourceStack> delhome(String root) {
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

                            HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);
                            if (playerData == null) {
                                player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§cSpieler Daten für Spieler {} konnten nicht gefunden werden", splitplayername)));
                                return 0;
                            }
                            List<Home> playerhomelist = playerData.getPlayerHomes();
                            playerhomelist.remove(home);
                            playerData.setPlayerHomes(playerhomelist);
                            plugin.getStorageManager().setPlayerData(uuid, playerData);

                            player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§7Haus {} wurde erfolgreich gelöscht", splithome)));

                            return 1;

                        })
                );
    }
}
