package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanConvo;
import org.emil.hnrpmc.simpleclans.conversation.dings.Convosable;
import org.emil.hnrpmc.simpleclans.conversation.dings.InactivityConvoCanceller;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class InactivityCanceller extends InactivityConvoCanceller {

    /**
     * Creates an InactivityConversationCanceller.
     *
     * @param plugin         The owning plugin.
     * @param timeoutSeconds The number of seconds of inactivity to wait.
     */
    public InactivityCanceller(@NotNull SimpleClans plugin, int timeoutSeconds) {
        super(plugin, timeoutSeconds);
    }

    @Override
    protected void cancelling(@NotNull ClanConvo conversation) {
        Convosable forWhom = conversation.getForWhom();
        forWhom.sendRawMessage(ChatFormatting.RED + lang("you.did.not.answer.in.time", forWhom.getUUID()));
    }
}
