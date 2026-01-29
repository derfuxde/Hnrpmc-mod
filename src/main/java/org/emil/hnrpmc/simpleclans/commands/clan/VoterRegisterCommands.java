package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoterRegisterCommands extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final SettingsManager settings;
    private final ClanManager cm;
    private final StorageManager storage;
    private final PermissionsManager permissions;
    private final RequestManager requestManager;

    public VoterRegisterCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.settings = plugin.getSettingsManager();
        this.cm = plugin.getClanManager();
        this.storage = plugin.getStorageManager();
        this.permissions = plugin.getPermissionsManager();
        this.requestManager = plugin.getRequestManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return java.util.List.of("accept", "deny");
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        if (rootLiteral.equals("accept")) {
            dispatcher.register(root(dispatcher, rootLiteral));
        }else if (rootLiteral.equals("deny")) {
            dispatcher.register(root2(dispatcher, "deny"));
        }
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(VoterRegisterCommands::canVote)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ClanPlayer requester = mustClanPlayer(player);
                    requestManager.accept(requester);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> root2(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(VoterRegisterCommands::canVote)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ClanPlayer requester = mustClanPlayer(player);
                    requestManager.deny(requester);
                    return 1;
                });
    }

    private boolean hasPermission(CommandSourceStack src, String perm) {
        return permissions.has(src.getPlayer(), perm);
    }

    private Clan mustClan(ServerPlayer player) {
        Clan c = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (c == null) throw new IllegalStateException("Player not in clan");
        return c;
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getCreateClanPlayer(player.getUUID());
        if (cp == null) throw new IllegalStateException("ClanPlayer missing");
        return cp;
    }

    private Clan mustClanByTag(String tagOrName, CommandSourceStack src) {
        Clan c = SimpleClans.getInstance().getClanManager().getClan(tagOrName);
        if (c == null) {
            src.sendFailure(Component.translatable("simpleclans.command.clan_not_found", tagOrName));
            throw new IllegalStateException("Clan not found");
        }
        return c;
    }

    private static boolean canVote(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return false;

        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(player);

        // Prüfe Bedingungen: Hat er einen Clan? Ist er kein Gast? etc.
        if (cp == null || cp.getClan() != null) return false;

        // Beispiel: Nur Leader dürfen voten
        return true;
    }

}
