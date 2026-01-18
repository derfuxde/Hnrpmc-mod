package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class CanVoteCondition extends AbstractCommandCondition {

    public CanVoteCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        Player player = Conditions.assertPlayer(context.getIssuer());
        ClanPlayer cp = clanManager.getCreateClanPlayer(player.getUUID());
        Clan clan = cp.getClan();
        if (clan != null) {
            if (!requestManager.hasRequest(clan.getTag())) {
                throw new ConditionFailedException(lang("nothing.to.vote", player));
            }
            if (!clan.isLeader(player)) {
                throw new ConditionFailedException(RED + lang("no.leader.permissions", player));
            }
            if (cp.getVote() != null) {
                throw new ConditionFailedException(RED + lang("you.have.already.voted", player));
            }
        } else {
            if (!requestManager.hasRequest(player.getName().getString().toLowerCase())) {
                throw new ConditionFailedException(lang("nothing.to.vote", player));
            }
        }
    }

    @Override
    public @NotNull String getId() {
        return "can_vote";
    }
}
