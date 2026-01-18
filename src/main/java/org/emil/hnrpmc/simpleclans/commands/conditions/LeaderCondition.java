package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class LeaderCondition extends AbstractCommandCondition {
    public LeaderCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        Player player = Conditions.assertPlayer(context.getIssuer());
        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (!clan.isLeader(player)) {
            throw new ConditionFailedException(ChatFormatting.RED + lang("no.leader.permissions", player));
        }
    }

    @Override
    public @NotNull String getId() {
        return "leader";
    }
}
