package org.emil.hnrpmc.simpleclans.commands.conditions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class NotBannedCondition {

    private static final SimpleCommandExceptionType BANNED =
            new SimpleCommandExceptionType(Component.literal("You are banned"));

    private NotBannedCondition() {}

    public static void check(CommandSourceStack source) throws CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return; // Konsole darf immer
        }

        if (SimpleClans.getInstance()
                .getSettingsManager()
                .isBanned(player.getUUID())) {

            throw new CommandSyntaxException(
                    BANNED,
                    Component.literal(lang("banned", player))
            );
        }
    }
}
