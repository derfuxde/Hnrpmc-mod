package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.debug;

@SuppressWarnings("unused")
public class PlayerClanMemberCondition extends AbstractParameterCondition<Player> {
    public PlayerClanMemberCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<Player> getType() {
        return Player.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext,
                                  Player value) throws InvalidCommandArgument {
        debug(String.format("PlayerClanMemberCondition -> %s %s", value.getName(), value.getId()));
        Conditions.assertClanMember(clanManager, context.getIssuer());
    }

    @Override
    public @NotNull String getId() {
        return "clan_member";
    }
}
