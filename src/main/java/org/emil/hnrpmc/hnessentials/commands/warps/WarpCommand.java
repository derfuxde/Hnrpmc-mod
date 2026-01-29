package org.emil.hnrpmc.hnessentials.commands.warps;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.GeneralDefaultData;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.*;

public class WarpCommand extends ClanSBaseCommand {
    private final HNessentials plugin;
    private final CommandHelper commandHelper;

    public WarpCommand(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("warp");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return warp(root);
    }

    private LiteralArgumentBuilder<CommandSourceStack> warp(String root) {
        return Commands.literal(root)
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests(Suggestions.allWarps(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String warpname = StringArgumentType.getString(ctx, "name");

                            plugin.getStorageManager().loadGeneralData();

                            GeneralDefaultData GDD = plugin.getStorageManager().getGeneralData();
                            Map<String, Vec3> allWarps = GDD.getWarps();
                            if (!allWarps.containsKey(warpname)) {
                                player.sendSystemMessage(Component.literal(plugin.getCommandHelper().formatMessage("§cWarp mit dem Namen {} existiert nicht", warpname)));
                                return 0;
                            }

                            Vec3 warppos = allWarps.get(warpname);

                            player.teleportTo(warppos.x, warppos.y, warppos.z);

                            player.sendSystemMessage(Component.literal(plugin.getCommandHelper().formatMessage("§aerfolgreich zu Warp {} telepotiert", warpname)));
                            return 1;

                        })
                );
    }
}
