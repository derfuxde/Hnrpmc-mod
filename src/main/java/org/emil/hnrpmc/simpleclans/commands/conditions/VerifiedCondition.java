package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class VerifiedCondition extends AbstractCommandCondition {

    public VerifiedCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (!clan.isVerified()) {
            throw new ConditionFailedException(lang("clan.is.not.verified",
                    (ServerPlayer) context.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "verified";
    }
}
