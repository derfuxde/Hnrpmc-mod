package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class ToggleCommand extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private StorageManager storage;

    public ToggleCommand(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.storage = plugin.getStorageManager();
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
                .then(teleport());
    }


    public LiteralArgumentBuilder<CommandSourceStack> teleport() {
                return Commands.literal("toggle")
                            .then(Commands.literal("invite")
                                    .executes(this::toggleInvite)
                            )
                            .then(Commands.literal("bb")
                                    .executes(this::toggleBb)
                            )
                            .then(Commands.literal("tag")
                                    .executes(this::toggleTag)
                            )
                            .then(Commands.literal("deposit")
                                    .executes(this::toggleDeposit)
                            )
                            .then(Commands.literal("fee")
                                    .executes(this::toggleFee)
                            )
                            .then(Commands.literal("withdraw")
                                    .executes(this::toggleWithdraw)
                            )
        ;
    }

    private int toggleInvite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        // Bedingungen, die du früher über @Conditions hattest:
        Conditions.verified(player);
        ClanPlayer cp = Conditions.clanPlayer(player);

        // Permission (ersetzt @CommandPermission):
        Conditions.permission(player, "simpleclans.anyone.invite-toggle");

        toggle(player, "inviteon", "inviteoff", cp.isInviteEnabled(), cp::setInviteEnabled);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClanPlayer(cp);
        return 1;
    }

    private int toggleBb(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Conditions.verified(player);
        Conditions.verified(player);

        ClanPlayer cp = Conditions.clanPlayer(player);
        Conditions.permission(player, "simpleclans.member.bb-toggle");

        toggle(player, "bbon", "bboff", cp.isBbEnabled(), cp::setBbEnabled);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClanPlayer(cp);
        return 1;
    }

    private int toggleTag(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        //Conditions.basic(player);
        Conditions.verified(player);

        ClanPlayer cp = Conditions.clanPlayer(player);
        Conditions.permission(player, "simpleclans.member.tag-toggle");

        toggle(player, "tagon", "tagoff", cp.isTagEnabled(), cp::setTagEnabled);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        this.storage.updateClanPlayer(cp);
        return 1;
    }

    private int toggleDeposit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Conditions.verified(player);

        Clan clan = Conditions.clan(player);
        Conditions.permission(player, "simpleclans.leader.deposit-toggle");
        Conditions.leader(player, clan);

        toggle(player, "depositon", "depositoff", clan.isAllowDeposit(), clan::setAllowDeposit);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        this.storage.updateClan(clan);
        return 1;
    }

    private int toggleFee(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Conditions.basic(player);
        Conditions.verified(player);

        Clan clan = Conditions.clan(player);
        Conditions.permission(player, "simpleclans.leader.fee");

        // ersetzte @Conditions("rank:name=FEE_ENABLE|change_fee")
        Conditions.rankPermission(player, "FEE_ENABLE");
        Conditions.changeFee(plugin, player, clan, player.createCommandSourceStack());

        toggle(player, "feeon", "feeoff", clan.isMemberFeeEnabled(), clan::setMemberFeeEnabled);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan);
        return 1;
    }

    private int toggleWithdraw(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Conditions.basic(player);
        Conditions.verified(player);

        Clan clan = Conditions.clan(player);
        Conditions.permission(player, "simpleclans.leader.withdraw-toggle");
        Conditions.leader(player, clan);

        toggle(player, "withdrawon", "withdrawoff", clan.isAllowWithdraw(), clan::setAllowWithdraw);
        if (storage == null) {
            this.storage = plugin.getStorageManager();
        }
        storage.updateClan(clan);
        return 1;
    }

    private void toggle(ServerPlayer player,
                        String onMessageKey,
                        String offMessageKey,
                        boolean status,
                        Consumer<Boolean> consumer) {

        String messageOn = lang(onMessageKey, player);
        String messageOff = lang(offMessageKey, player);

        player.sendSystemMessage(Component.literal(status ? messageOff : messageOn));
        consumer.accept(!status);
    }
}
