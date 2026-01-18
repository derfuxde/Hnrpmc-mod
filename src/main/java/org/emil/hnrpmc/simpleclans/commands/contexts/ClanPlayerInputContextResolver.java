package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.USERNAME_REGEX;

@SuppressWarnings("unused")
public class ClanPlayerInputContextResolver extends AbstractInputOnlyContextResolver<ClanPlayerInput> {

    private final Pattern validUsername;

    public ClanPlayerInputContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
        validUsername = Pattern.compile(plugin.getSettingsManager().getString(USERNAME_REGEX));
    }

    @Override
    public ClanPlayerInput getContext(CommandExecutionContext context) throws InvalidCommandArgument {
        String arg = context.popFirstArg();
        if (!validUsername.matcher(arg).matches()) {
            throw new InvalidCommandArgument(MessageKeys.COULD_NOT_FIND_PLAYER, "{name}", arg);
        }

        ClanPlayer cp = clanManager.getAnyClanPlayer(arg);
        if (cp == null) {
            GameProfile player = SimpleClans.getInstance().getServer().getProfileCache().get(arg).get();
            if (player == null) {
                throw new InvalidCommandArgument(lang("user.hasnt.played.before", (ServerPlayer) context.getIssuer()));
            }
            cp = clanManager.getCreateClanPlayer(player.getId());
        }

        return new ClanPlayerInput(cp);
    }

    @Override
    public Class<ClanPlayerInput> getType() {
        return ClanPlayerInput.class;
    }

}
