package org.emil.hnrpmc.simpleclans.commands.staff;

import co.aikar.commands.BaseCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.managers.ChatManager;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class BbCommand extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final ChatManager chatManager;
    private StorageManager storageManager;
    private final ClanManager clanManager;

    public static final String primarycommand = "clan";

    public BbCommand(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.storageManager = plugin.getStorageManager();
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("");
    }


    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .then(Commands.literal("mod")
                    .then(Commands.literal("mod"))
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> display() {
        return Commands.literal("display")
                .requires(src -> Conditions.permission(src.getPlayer(), "simpleclans.mod.bb"))
                .then(Commands.argument("clanTag", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayer();
                            String tag = StringArgumentType.getString(ctx, "clanTag");
                            Clan clan = clanManager.getClan(tag);
                            if (clan != null && ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                clan.displayBb(player);
                            }
                            return 1;
                        })
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> clear() {
        return Commands.literal("clear")
                .requires(src -> Conditions.permission(src.getPlayer(), "simpleclans.mod.bb"))
                .then(Commands.argument("clanTag", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayer();
                            String tag = StringArgumentType.getString(ctx, "clanTag");
                            Clan clan = clanManager.getClan(tag);
                            if (clan != null && ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                clan.clearBb();
                            }
                            return 1;
                        })
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> add() {
        return Commands.literal("add")
                .requires(src -> Conditions.permission(src.getPlayer(), "simpleclans.mod.bb"))
                .then(Commands.argument("clanTag", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .then(Commands.argument("message", StringArgumentType.greedyString()))
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayer();
                            String tag = StringArgumentType.getString(ctx, "clanTag");
                            String msg = StringArgumentType.getString(ctx, "message");
                            Clan clan = clanManager.getClan(tag);
                            if (clan != null && ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                String formattedmsg = lang("bulletin.board.message", player, player.getName().getString(), msg);
                                clan.addBb(formattedmsg);
                                clan.displayBb(player);
                                storageManager.updateClan(clan);

                            }
                            return 1;
                        })
                );
    }
}
