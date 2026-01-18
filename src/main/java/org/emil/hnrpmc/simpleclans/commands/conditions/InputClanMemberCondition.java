package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static net.minecraft.ChatFormatting.RED;

public abstract class InputClanMemberCondition extends AbstractParameterCondition<ClanPlayerInput> {
    public InputClanMemberCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanPlayerInput> getType() {
        return ClanPlayerInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext, ClanPlayerInput value)
            throws InvalidCommandArgument {
        if (value.getClanPlayer().getClan() == null) {
            throw new ConditionFailedException(RED + lang("player.not.a.member.of.any.clan",
                    (ServerPlayer) execContext.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "clan_member";
    }
}
