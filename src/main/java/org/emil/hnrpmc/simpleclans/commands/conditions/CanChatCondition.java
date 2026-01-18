package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;

public class CanChatCondition extends AbstractCommandCondition {
    public CanChatCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        String type = context.getConfigValue("type", (String) null).toUpperCase();
        ConfigField chatEnabled = ConfigField.valueOf(type + "CHAT_ENABLE");
        if (!settingsManager.is(chatEnabled)) {
            throw new ConditionFailedException(lang(type.toLowerCase() + ".chat.disabled", (ServerPlayer) context.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "can_chat";
    }
}
