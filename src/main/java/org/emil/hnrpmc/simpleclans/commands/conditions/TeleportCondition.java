package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Flags;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class TeleportCondition extends AbstractParameterCondition<Clan> {

    public TeleportCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<Clan> getType() {
        return Clan.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext,
                                  Clan value) throws InvalidCommandArgument {
        Player player = (Player) execContext.getIssuer();
        if (value.getHomeLocation() == null) {
            throw new ConditionFailedException(lang("hombase.not.set", player));
        }
        Flags flags = new Flags(value.getFlags());
        String homeServer = flags.getString("homeServer", "");
        if (!homeServer.isEmpty() && !plugin.getProxyManager().getServerName().equals(homeServer)) {
            throw new ConditionFailedException(lang("home.set.in.different.server"));
        }
    }

    @Override
    public @NotNull String getId() {
        return "can_teleport";
    }
}
