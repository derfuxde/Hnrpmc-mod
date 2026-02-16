package org.emil.hnrpmc.hnessentials.commands.PlayerData;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.network.OpenAdminScreenPayload;
import org.emil.hnrpmc.hnessentials.network.SendCosmeticRegister;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.locks.Condition;

public class registerAdminGui extends ClanSBaseCommand {

    private final HNessentials plugin;
    private StorageManager storage;

    public registerAdminGui(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("playerdata");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return registerAdminGui(root);
    }

    public LiteralArgumentBuilder<CommandSourceStack> registerAdminGui(String root) {
        return Commands.literal(root)
                .requires(ctx -> Conditions.permission(ctx.getPlayer(), "essentials.admin.playerdata"))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer admin = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");

                            HNPlayerData fullData = HNessentials.getInstance().getStorageManager().getOrCreatePlayerData(target.getUUID()); //380df991-f603-344c-a090-369bad2a924a

                            AdminSyncData safeData = new AdminSyncData(
                                    fullData.getMoney(),
                                    fullData.prefix(),
                                    fullData.suffix(),
                                    fullData.hats(),
                                    fullData.isJailed()
                            );

                            String dataJson = new Gson().toJson(fullData);

                            PacketDistributor.sendToAllPlayers(new SendCosmeticRegister(new Gson().toJson(plugin.getStorageManager().getGeneralData().getCosmetics())));

                            PacketDistributor.sendToPlayer(admin, new OpenAdminScreenPayload(
                                    target.getUUID(),
                                    target.getScoreboardName(),
                                    dataJson,
                                    false
                            ));

                            return 1;
                        })
                );
    }
}

