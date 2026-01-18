package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.data.*;
import org.emil.hnrpmc.simpleclans.utils.VanishUtils;
import org.jetbrains.annotations.Nullable;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class DataCommands extends ClanSBaseCommand {

    private final SimpleClans plugin;

    public DataCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public @Nullable String primarycommand() {
        return "";
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .then(node())
                .then(vitals())
                .then(stats())
                .then(profile())
                .then(roster())
                .then(coords());
    }


    public LiteralArgumentBuilder<CommandSourceStack> node() {
    return Commands.literal("data")
            // optional: /clan data ... (wenn du "data" als Unterknoten willst)
            ;
    }

    public LiteralArgumentBuilder<CommandSourceStack> vitals() {
        return Commands.literal("vitals")
                .requires(src -> src.getEntity() instanceof ServerPlayer) // wie vorher member-only
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!hasPerm(player, "simpleclans.member.vitals")) return 0;
                    if (!rank(cp, "VITALS", ctx.getSource())) return 0;
                    if (!verified(player, ctx.getSource())) return 0;

                    Vitals v = new Vitals(plugin, ctx.getSource(), clan);
                    v.send();
                    return 1;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> stats() {
        return Commands.literal("stats")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!hasPerm(player, "simpleclans.member.stats")) return 0;
                    if (!rank(cp, "STATS", ctx.getSource())) return 0;
                    if (!verified(player, ctx.getSource())) return 0;

                    ClanStats s = new ClanStats(plugin, player.createCommandSourceStack(), clan);
                    s.send();
                    return 1;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> profile() {
        return Commands.literal("profile")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    Clan clan = mustClan(player);

                    if (!hasPerm(player, "simpleclans.member.profile")) return 0;
                    if (!verified(player, ctx.getSource())) return 0;

                    ClanProfile p = new ClanProfile(plugin, ctx.getSource(), clan);
                    p.send();
                    return 1;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> roster() {
        return Commands.literal("roster")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    Clan clan = mustClan(player);

                    if (!hasPerm(player, "simpleclans.member.roster")) return 0;
                    if (!verified(player, ctx.getSource())) return 0;

                    ClanRoster r = new ClanRoster(plugin, player.createCommandSourceStack(), clan);
                    r.send();
                    return 1;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> coords() {
        return Commands.literal("coords")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!hasPerm(player, "simpleclans.member.coords")) return 0;
                    if (!rank(cp, "COORDS", ctx.getSource())) return 0;
                    if (!verified(player, ctx.getSource())) return 0;

                    if (VanishUtils.getNonVanished(player, clan).size() == 1) {
                        ChatBlock.sendMessage(player.createCommandSourceStack(),
                                ChatFormatting.RED + lang("you.are.the.only.member.online", player));
                        return 0;
                    }

                    ClanCoords c = new ClanCoords(plugin, player, clan);
                    c.send();
                    return 1;
                });
    }

    // ----------------- helpers (minimal, passend zu deinem Projektstil) -----------------

    private boolean hasPerm(ServerPlayer player, String perm) {
        return plugin.getPermissionsManager().has(player, perm);
    }

    private boolean verified(ServerPlayer player, CommandSourceStack src) {
        // Wenn du REQUIRE_VERIFICATION nutzt, hänge hier deine Logik an
        // Für jetzt: true
        return true;
    }

    private boolean rank(ClanPlayer cp, String rankPermKey, CommandSourceStack src) {
        // Wenn du dein Rank-System via Conditions.rank(...) hattest:
        // Hier muss die NeoForge-Variante rein (z.B. cp.getClan().getRank(...).hasPermission(rankPermKey))
        // Für jetzt: true
        return true;
    }

    private Clan mustClan(ServerPlayer player) {
        Clan c = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (c == null) throw new IllegalStateException("Player not in clan");
        return c;
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player) {
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUUID());
        if (cp == null) throw new IllegalStateException("ClanPlayer missing");
        return cp;
    }
}
