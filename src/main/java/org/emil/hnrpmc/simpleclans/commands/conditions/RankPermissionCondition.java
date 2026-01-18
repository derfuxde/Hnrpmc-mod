package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class RankPermissionCondition extends AbstractCommandCondition {

    public RankPermissionCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        String name = context.getConfigValue("name", (String) null);
        Player player = (Player) context.getIssuer();
        if (player == null || name == null) {
            return;
        }
        RankPermission rankPermission = org.emil.hnrpmc.simpleclans.RankPermission.valueOf(name);

        if (!permissionsManager.has((ServerPlayer) player, rankPermission, true)) {
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "rank";
    }
}
