package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class RankContextResolver extends AbstractInputOnlyContextResolver<Rank> {
    public RankContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Rank getContext(CommandExecutionContext context) throws InvalidCommandArgument {
        Clan clan = Contexts.assertClanMember(clanManager, context.getIssuer());
        String rankName = context.isLastArg() ? context.joinArgs() : context.popFirstArg();
        Rank rank = clan.getRank(rankName);
        if (rank == null) {
            throw new InvalidCommandArgument(RED + lang("rank.0.does.not.exist", (ServerPlayer) context.getIssuer(), rankName),
                    false);
        }
        return rank;
    }

    @Override
    public Class<Rank> getType() {
        return Rank.class;
    }
}
