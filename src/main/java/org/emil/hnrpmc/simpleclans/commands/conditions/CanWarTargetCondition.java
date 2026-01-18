package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.WAR_MAX_MEMBERS_DIFFERENCE;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.WAR_START_REQUEST_ENABLED;

public abstract class CanWarTargetCondition extends AbstractParameterCondition<ClanInput> {
    public CanWarTargetCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanInput> getType() {
        return ClanInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context, CommandExecutionContext execContext, ClanInput target) throws InvalidCommandArgument {
        CommandIssuer issuer = execContext.getIssuer();
        Clan issuerClan = Conditions.assertClanMember(clanManager, issuer);
        Clan targetClan = target.getClan();

        if (!issuerClan.isRival(targetClan.getTag())) {
            throw new ConditionFailedException(lang("you.can.only.start.war.with.rivals", (ServerPlayer) issuer));
        }

        if (issuerClan.isWarring(targetClan)) {
            throw new ConditionFailedException(lang("clans.already.at.war", (ServerPlayer) issuer));
        }

        boolean isWarRequestEnabled = settingsManager.is(WAR_START_REQUEST_ENABLED);
        int maxDifference = settingsManager.getInt(WAR_MAX_MEMBERS_DIFFERENCE);

        if (!isWarRequestEnabled && maxDifference >= 0) {
            int difference = Math.abs(issuerClan.getOnlineMembers().size() - targetClan.getOnlineMembers().size());
            if (difference > maxDifference)
                throw new ConditionFailedException(lang("you.cant.start.war.online.members.difference", (ServerPlayer) issuer));
        }
    }

    @Override
    public @NotNull String getId() {
        return "can_war_target";
    }
}
