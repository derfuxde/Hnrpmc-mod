package org.emil.hnrpmc.hnessentials.commands.tpa;

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
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TpoCommand extends ClanSBaseCommand {

    private final HNessentials plugin;

    private final CommandHelper commandHelper;

    public TpoCommand(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("tpo");
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
                .requires(ctx -> Conditions.permission(ctx.getPlayer(), "essentials.admin.tpo"))
                .then(Commands.argument("Spieler", StringArgumentType.string())
                        .suggests(Suggestions.allPlayerNameFromHomes(plugin))
                        .executes(this::executetpo)
                );
    }

    private int executetpo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String playername;
            try {
                playername = StringArgumentType.getString(ctx, "Spieler");
            } catch (IllegalArgumentException e) {
                playername = null;
            }
            if (playername != null) {
                UUID uuid = plugin.getServer().getProfileCache().get(playername).get().getId();
                if (uuid != null) {

                    if (!SimpleClans.getInstance().getPermissionsManager().has(player, "essentials.admin.tpo") && !uuid.equals(player.getUUID())) {
                        String msg = commandHelper.formatMessage("§cDu bist nicht berechtigt dich zu offline spieler zu telepotieren");

                        player.sendSystemMessage(Component.literal(msg));

                        return 0;
                    }

                    plugin.getStorageManager().loadAllPlayerDatas();
                    HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);

                    if (playerData == null) {
                        player.sendSystemMessage(Component.literal("§cSpieler war noch nie auf dem Server"));
                        return 0;
                    }
                    Vec3 vec3 = playerData.getLastLocation();
                    if (vec3 != null) {
                        player.teleportTo(vec3.x, vec3.y, vec3.z);
                        player.sendSystemMessage(Component.literal("§aErfolgreich zu Spieler teleportiert"));
                        return 1;
                    }

                }
            }
            return 0;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
