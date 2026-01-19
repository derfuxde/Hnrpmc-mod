package org.emil.hnrpmc.hnessentials.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class homesCommand extends ClanSBaseCommand {

    private final HNessentials plugin;

    private final CommandHelper commandHelper;

    public homesCommand(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable String primarycommand() {
        return "homes";
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return homes(root);
    }

    private LiteralArgumentBuilder<CommandSourceStack> homes(String root) {
        return Commands.literal(root)
                .executes(this::executehomes)
                .then(Commands.argument("Spieler", StringArgumentType.string())
                        .suggests(Suggestions.allPlayerNameFromHomes(plugin))
                    .executes(this::executehomes)
                );
    }

    private int executehomes(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String playername;
            try {
                playername = StringArgumentType.getString(ctx, "Spieler");
            } catch (IllegalArgumentException e) {
                playername = player.getName().getString();
            }
            if (playername != null) {
                UUID uuid = plugin.getServer().getProfileCache().get(playername).get().getId();
                if (uuid != null) {

                    if (!SimpleClans.getInstance().getPermissionsManager().has(player, "essentials.home.other") && !uuid.equals(player.getUUID())) {
                        String msg = commandHelper.formatMessage("§cDu bist nicht berechtigt die Häuser von anderen zu sehen");

                        player.sendSystemMessage(Component.literal(msg));

                        return 0;
                    }

                    List<Home> playerhomes = plugin.getHomeManager().getHomes(uuid);
                    if (playerhomes == null || playerhomes.isEmpty()) {
                        String msg = commandHelper.formatMessage("Der Spieler {} keine Häuser", playername);

                        player.sendSystemMessage(Component.literal(msg));
                        return 1;
                    }

                    String formattedList = playerhomes.stream()
                            .map(home -> "• " + home.getHomename())
                            .collect(Collectors.joining("\n"));
                    String msg = commandHelper.formatMessage("Der Spieler {} hat {} Häuser:\n\n" + formattedList, playername, playerhomes.size());

                    player.sendSystemMessage(Component.literal(msg));
                    return 1;
                }
            }
            return 0;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
