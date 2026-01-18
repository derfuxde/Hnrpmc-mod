package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import org.emil.hnrpmc.simpleclans.utils.VanishUtils;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.debug;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class OnlineCondition extends AbstractParameterCondition<ClanPlayerInput> {

    public OnlineCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanPlayerInput> getType() {
        return ClanPlayerInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context, CommandExecutionContext execContext, ClanPlayerInput value) throws InvalidCommandArgument {
        ClanPlayer clanPlayer = value.getClanPlayer();
        debug(String.format("OnlineCondition -> %s %s", clanPlayer.getName(), clanPlayer.getUniqueId()));
        Player player = clanPlayer.toPlayer();

        if (player != null) {
            boolean isVanished = VanishUtils.isVanished((ServerPlayer) execContext.getIssuer(), (ServerPlayer) player);
            if (!isVanished || !context.hasConfig("ignore_vanished")) {
                return;
            }
        }

        throw new ConditionFailedException(lang("other.player.must.be.online", (ServerPlayer) execContext.getIssuer()));
    }

    @Override
    @NotNull
    public String getId() {
        return "online";
    }
}
