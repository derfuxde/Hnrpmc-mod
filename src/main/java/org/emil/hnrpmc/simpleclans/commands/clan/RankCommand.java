package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.events.DeleteRankEvent;
import org.emil.hnrpmc.simpleclans.events.PlayerRankUpdateEvent;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class RankCommand extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private StorageManager storage;
    private final PermissionsManager permissions;

    public RankCommand(SimpleClans plugin) {
        super(plugin);

        this.plugin = plugin;
        this.storage = plugin.getStorageManager();
        this.permissions = plugin.getPermissionsManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    /**
     * /clan rank ...
     *
     * Erwartung: Du hast bereits bei /clan ein "leader|verified|basic" Gate.
     * Hier mache ich es pragmatisch: requires() mit Permissions + Leader-Check, sobald Clan auflösbar ist.
     */
    private List<RankPermission> getreqPerms() {
        List<RankPermission> reqPerms = new ArrayList<>();
        reqPerms.add(RankPermission.RANK_DELETE);
        reqPerms.add(RankPermission.RANK_MEMBER_ADD);
        reqPerms.add(RankPermission.RANK_LIST);
        reqPerms.add(RankPermission.RANK_CREATE);
        reqPerms.add(RankPermission.RANK_DISPLAYNAME);
        reqPerms.add(RankPermission.RANK_MANAGE);

        return reqPerms;
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .then(Commands.literal("rank")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || getreqPerms().stream().anyMatch(perm -> Conditions.rankPermission(src.getPlayer(), perm.getNeoPermission()))))
                        .then(assign()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_MEMBER_ADD.getNeoPermission())))
                        ).then(unassign())
                            .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_MEMBER_ADD.getNeoPermission()))
                        )
                        // create: ohne Conversation -> direkte Erstellung via Argument (oder du baust später GUI/Chat flow)
                        .then(create()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_CREATE.getNeoPermission()))))
                        .then(delete()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_DELETE.getNeoPermission())))
                        ).then(list()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_LIST.getNeoPermission())))
                        ).then(setDisplayName()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_DISPLAYNAME.getNeoPermission())))
                        ).then(setDefault()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_MANAGE.getNeoPermission())))
                        ).then(removeDefault()

                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_MANAGE.getNeoPermission())))
                        ).then(permissionsRoot()
                                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RANK_MANAGE.getNeoPermission())))
                        )
                );
    }

    // ------------------------------------------------------------
    // /clan rank assign <rank> <member>
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> assign() {
        return Commands.literal("assign")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        // ...dann den Member
                        .then(Commands.argument("member", StringArgumentType.string())
                                // Jetzt kannst du das "rank" Argument für die Suggestions nutzen!
                                .suggests(Suggestions.notsamerank(plugin))
                                .executes(this::execAssign)));
    }

    private int execAssign(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String memberName = StringArgumentType.getString(ctx, "member");
        UUID memberUuid = plugin.getClanManager().getClanPlayer(memberName).getUniqueId();
        ClanPlayer memberCp = plugin.getClanManager().getClanPlayer(memberUuid);

        if (memberCp == null || memberCp.getClan() == null || !memberCp.getClan().equals(clan)) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Member not found in your clan.");
            return 0;
        }

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank newRank = clan.getRank(rankName);
        if (newRank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        Rank oldRank = clan.getRank(memberCp.getRankId());

        PlayerRankUpdateEvent event = new PlayerRankUpdateEvent(clan.getLeaders().getFirst(), memberCp, clan, oldRank, newRank);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        if (memberCp.getRankId() != null && memberCp.getRankId().equals(newRank.getName())) {
            ChatBlock.sendMessage(ctx.getSource(), lang("player.already.has.that.rank", leader));
            return 0;
        }

        memberCp.setRank(newRank.getName());
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClanPlayer(memberCp);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("player.rank.changed", leader));
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank unassign <member>
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> unassign() {
        return Commands.literal("unassign")
                .then(Commands.argument("member", StringArgumentType.word())
                        .executes(this::execUnassign));
    }

    private int execUnassign(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String memberName = StringArgumentType.getString(ctx, "member");
        UUID memberUuid = plugin.getClanManager().getClanPlayer(memberName).getUniqueId();
        ClanPlayer memberCp = plugin.getClanManager().getClanPlayer(memberUuid);

        if (memberCp == null || memberCp.getClan() == null || !memberCp.getClan().equals(clan)) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Member not found in your clan.");
            return 0;
        }

        Rank oldRank = clan.getRank(memberCp.getRankId());
        PlayerRankUpdateEvent event = new PlayerRankUpdateEvent(clan.getLeaders().getFirst(), memberCp, clan, oldRank, null);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        memberCp.setRank(null);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClanPlayer(memberCp);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("player.unassigned.from.rank", leader));
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank create <name>
    // (Conversation ersetzt: Brigadier greedyString)
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(this::execCreate));
    }

    private int execCreate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String name = StringArgumentType.getString(ctx, "name");

        if (!Helper.isValidRankName(name)) { // falls du so etwas nicht hast: simple sanitize/length check
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Invalid rank name.");
            return 0;
        }

        if (clan.getRank(name) != null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank already exists.");
            return 0;
        }

        clan.createRank(name);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + "Rank created: " + name);
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank delete <rank>
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> delete() {
        return Commands.literal("delete")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        .executes(this::execDelete));
    }

    private int execDelete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        DeleteRankEvent event = new DeleteRankEvent(leader, clan, rank);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        clan.deleteRank(rank.getName());
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        ChatBlock.sendMessage(ctx.getSource(),
                ChatFormatting.AQUA + lang("rank.0.deleted", leader, rank.getDisplayName()));
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank list
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> list() {
        return Commands.literal("list")
                .executes(this::execList);
    }

    private int execList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        List<Rank> ranks = clan.getRanks();
        if (ranks.isEmpty()) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + lang("no.ranks", leader));
            return 0;
        }

        ranks.sort(Comparator.reverseOrder());
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("clans.ranks", leader));

        int count = 1;
        for (Rank rank : ranks) {
            ChatBlock.sendMessage(ctx.getSource(),
                    ChatFormatting.AQUA + lang("ranks.list.item", leader, count,
                            ChatUtils.parseColors(rank.getDisplayName()) + ChatFormatting.AQUA, rank.getName()));
            count++;
        }
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank setdisplayname <rank> <displayname...>
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> setDisplayName() {
        return Commands.literal("setdisplayname")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        .then(Commands.argument("displayname", StringArgumentType.greedyString())
                                .executes(this::execSetDisplayName)));
    }

    private int execSetDisplayName(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        String displayName = StringArgumentType.getString(ctx, "displayname");
        if (displayName.contains("&") && !permissions.has(leader, "simpleclans.leader.coloredrank")) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + lang("you.cannot.set.colored.ranks", leader));
            return 0;
        }

        rank.setDisplayName(displayName);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("rank.displayname.updated", leader));
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank setdefault <rank>
    // /clan rank removedefault
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> setDefault() {
        return Commands.literal("setdefault")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        .executes(this::execSetDefault));
    }

    private int execSetDefault(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        clan.setDefaultRank(rank.getName());
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        ChatBlock.sendMessage(ctx.getSource(),
                ChatFormatting.AQUA + lang("rank.setdefault", leader, rank.getDisplayName()));
        return 1;
    }

    private LiteralArgumentBuilder<CommandSourceStack> removeDefault() {
        return Commands.literal("removedefault")
                .executes(this::execRemoveDefault);
    }

    private int execRemoveDefault(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        clan.setDefaultRank(null);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("rank.removedefault", leader));
        return 1;
    }

    // ------------------------------------------------------------
    // /clan rank permissions ...
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> permissionsRoot() {
        return Commands.literal("permissions")
                .then(permsAvailable())
                .then(permsList())
                .then(permsAdd())
                .then(permsRemove());
    }

    // /clan rank permissions available
    private LiteralArgumentBuilder<CommandSourceStack> permsAvailable() {
        final String validPermissionsToMessage = String.join(",", Helper.fromPermissionArray());
        return Commands.literal("available")
                .executes(ctx -> {
                    ServerPlayer leader = ctx.getSource().getPlayerOrException();
                    ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("available.rank.permissions", leader));
                    ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + validPermissionsToMessage);
                    return 1;
                });
    }

    // /clan rank permissions list <rank>
    private LiteralArgumentBuilder<CommandSourceStack> permsList() {
        return Commands.literal("list")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        .executes(this::execPermsList));
    }

    private int execPermsList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        Set<String> perms = rank.getPermissions();
        if (perms.isEmpty()) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + lang("rank.no.permissions", leader));
            return 0;
        }

        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("rank.0.permissions", leader, rank.getDisplayName()));
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + String.join(",", perms));
        return 1;
    }

    // /clan rank permissions add <rank> <permission>
    private LiteralArgumentBuilder<CommandSourceStack> permsAdd() {
        return Commands.literal("add")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        .then(Commands.argument("permission", StringArgumentType.word())
                                .suggests(Suggestions.getRankPerms(plugin))
                                .executes(this::execPermsAdd)));
    }

    private int execPermsAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        String permission = StringArgumentType.getString(ctx, "permission");
        rank.getPermissions().add(permission);

        ChatBlock.sendMessage(ctx.getSource(),
                ChatFormatting.AQUA + lang("permission.0.added.to.rank.1", leader, permission, rank.getDisplayName()));
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        return 1;
    }

    // /clan rank permissions remove <rank> <permission>
    private LiteralArgumentBuilder<CommandSourceStack> permsRemove() {
        return Commands.literal("remove")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests(Suggestions.getAllRanks(plugin))
                        .then(Commands.argument("permission", StringArgumentType.word())
                                .suggests(Suggestions.getRankPerms(plugin))
                                .executes(this::execPermsRemove)));
    }

    private int execPermsRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer leader = ctx.getSource().getPlayerOrException();
        Clan clan = requireClanOf(leader);

        String rankName = StringArgumentType.getString(ctx, "rank");
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Rank not found.");
            return 0;
        }

        String permission = StringArgumentType.getString(ctx, "permission");
        rank.getPermissions().remove(permission);

        ChatBlock.sendMessage(ctx.getSource(),
                ChatFormatting.AQUA + lang("permission.0.removed.from.rank.1", leader, permission, rank.getDisplayName()));
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan, true);
        return 1;
    }

    // ------------------------------------------------------------
    // CLAN RESOLUTION / GUARDS
    // ------------------------------------------------------------

    private Clan requireClanOf(ServerPlayer player) {
        Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (clan == null) {
            throw new IllegalStateException("Player has no clan.");
        }
        if (!clan.isLeader(player.getUUID())) {
            throw new IllegalStateException("Player is not leader.");
        }
        if (!clan.isVerified() && plugin.getSettingsManager().is(SettingsManager.ConfigField.REQUIRE_VERIFICATION)) {
            throw new IllegalStateException("Clan not verified.");
        }
        return clan;
    }

    private boolean has(CommandSourceStack src, String perm) {
        if (!(src.getEntity() instanceof ServerPlayer sp)) return false;
        return plugin.getPermissionsManager().has(sp, perm);
    }
}
