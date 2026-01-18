package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.BLACKLISTED_WORLDS;

@SuppressWarnings("unused")
public class NotBlacklistedCondition extends AbstractCommandCondition {

    public NotBlacklistedCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        ServerPlayer player = (ServerPlayer) context.getIssuer();
        if (player != null) {
            Level world = player.level();
            if (world != null) {
                if (settingsManager.getStringList(BLACKLISTED_WORLDS).contains(world.dimension().location().toString())) {
                    throw new ConditionFailedException();
                }
            }
        }
    }

    @Override
    public @NotNull String getId() {
        return "not_blacklisted";
    }
}