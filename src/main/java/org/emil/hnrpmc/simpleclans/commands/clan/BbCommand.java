package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class BbCommand extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final ClanManager cm;
    private final StorageManager storage;
    private final SettingsManager settings;

    public final String primarycommand = "clan";

    public BbCommand(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.cm = plugin.getClanManager();
        this.storage = plugin.getStorageManager();
        this.settings = plugin.getSettingsManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("clan");
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .then(bb());
    }

    /**
     * Register unter /clan bb ...
     */
    public LiteralArgumentBuilder<CommandSourceStack> bb() {
        return Commands.literal("bb")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.BB_ADD.getNeoPermission()) || Conditions.rankPermission(src.getPlayer(), RankPermission.BB_CLEAR.getNeoPermission())))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (!verified(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.member.bb")) return 0;

                    Clan clan = mustClan(player);
                    clan.displayBb(player); // ggf. Signatur anpassen: ServerPlayer statt Bukkit Player
                    return 1;
                })
                .then(clear())
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.BB_CLEAR.getNeoPermission())))
                .then(add())
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.BB_ADD.getNeoPermission())));
    }

    private LiteralArgumentBuilder<CommandSourceStack> clear() {
        return Commands.literal("clear")
                .requires(src -> Conditions.permission(src.getPlayer(), "simpleclans.leader.bb-clear"))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (!verified(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.leader.bb-clear")) return 0;

                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!rank(cp, "BB_CLEAR", ctx.getSource())) return 0;

                    clan.clearBb();
                    ChatBlock.sendMessage(player.createCommandSourceStack(),
                            ChatFormatting.RED + lang("cleared.bb", player));
                    storage.updateClan(clan);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> add() {
        return Commands.literal("add")
                .requires(src -> Conditions.permission(src.getPlayer(), "simpleclans.member.bb-add"))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");

                            if (!verified(player, ctx.getSource())) return 0;
                            if (!hasPerm(player, "simpleclans.member.bb-add")) return 0;

                            Clan clan = mustClan(player);
                            ClanPlayer cp = mustClanPlayer(player);

                            if (!rank(cp, "BB_ADD", ctx.getSource())) return 0;

                            clan.addBb(lang("bulletin.board.message", player, player.getName().getString(), msg));
                            clan.displayBb(player);
                            storage.updateClan(clan);
                            return 1;
                        }));
    }

    // ---------------- helpers ----------------

    private boolean hasPerm(ServerPlayer player, String perm) {
        return plugin.getPermissionsManager().has(player, perm);
    }

    private boolean verified(ServerPlayer player, CommandSourceStack src) {
        // Hier deine REQUIRE_VERIFICATION / ClanVerified-Logik einbauen.
        return true;
    }

    private boolean rank(ClanPlayer cp, String rankPermKey, CommandSourceStack src) {
        // Hier dein Rank-System anbinden (RankPermission / Rank flags).
        return true;
    }

    private Clan mustClan(ServerPlayer player) {
        Clan clan = cm.getClanByPlayerUniqueId(player.getUUID());
        if (clan == null) {
            throw new IllegalStateException("Player not in clan");
        }
        return clan;
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player) {
        ClanPlayer cp = cm.getClanPlayer(player.getUUID());
        if (cp == null) {
            throw new IllegalStateException("ClanPlayer missing");
        }
        return cp;
    }
}
