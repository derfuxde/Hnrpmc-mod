package org.emil.hnrpmc.simpleclans.commands.conditions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class DifferentClanCondition {

    public static Clan parse(
            CommandContext<CommandSourceStack> ctx,
            String name,
            SimpleClans plugin
    ) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String tag = StringArgumentType.getString(ctx, name);

        Clan target = plugin.getClanManager().getClan(tag);
        if (target == null) {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                    Component.literal("Clan not found")
            );
        }

        ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(player.getUUID());
        if (cp != null && cp.getClan() != null && cp.getClan().equals(target)) {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    Component.literal(SimpleClans.lang("cannot.be.same.clan", player))
            );
        }

        return target;
    }
}
