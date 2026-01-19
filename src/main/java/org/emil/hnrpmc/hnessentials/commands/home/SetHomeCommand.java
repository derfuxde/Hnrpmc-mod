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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.MAX_HOMES;

public class SetHomeCommand extends ClanSBaseCommand {

    private final HNessentials plugin;

    private final CommandHelper commandHelper;

    public SetHomeCommand(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable String primarycommand() {
        return "sethome";
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return sethome(root);
    }


    private LiteralArgumentBuilder<CommandSourceStack> sethome(String root) {
        return Commands.literal(root)
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
                                    player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§cDu hast die maximale Anzahl an Homes erreicht", splithome)));
                                    return 0;
                                }
                            }
                            if (home != null) {
                                player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§cDas Haus mit dem namen {} exesiert bereits", splithome)));
                                return 0;
                            }

                            Home newHome = new Home(uuid, player.getPosition(0), splithome, player.level().dimension().location().toString());
                            HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);
                            if (playerData == null) {
                                player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§cSpieler Daten für Spieler {} konnten nicht gefunden werden", splitplayername)));
                                return 0;
                            }
                            List<Home> playerhomelist = playerData.getPlayerHomes();
                            playerhomelist.add(newHome);
                            playerData.setPlayerHomes(playerhomelist);
                            plugin.getStorageManager().setPlayerData(uuid, playerData);

                            player.sendSystemMessage(Component.literal(commandHelper.formatMessage("§7Haus {} wurde erfolgreich erstellt", splithome)));

                            return 1;

                        })
                );
    }
}
