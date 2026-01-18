package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class InputVerifiedCondition extends AbstractParameterCondition<ClanInput> {

    public InputVerifiedCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanInput> getType() {
        return ClanInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext,
                                  ClanInput value)
            throws InvalidCommandArgument {
        if (!value.getClan().isVerified() && !context.getIssuer().hasPermission("simpleclans.mod.bypass")) {
            throw new ConditionFailedException(lang("other.clan.not.verified", (ServerPlayer) execContext.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "verified";
    }
}
