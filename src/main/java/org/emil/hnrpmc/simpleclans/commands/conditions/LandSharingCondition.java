package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.LAND_SHARING;

@SuppressWarnings("unused")
public class LandSharingCondition extends AbstractCommandCondition {

    public LandSharingCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        if (!settingsManager.is(LAND_SHARING)) {
            throw new ConditionFailedException(lang("land.sharing.disabled", (ServerPlayer) context.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "land_sharing";
    }
}
