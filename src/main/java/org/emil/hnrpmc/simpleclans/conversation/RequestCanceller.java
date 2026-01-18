package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanConvo;
import org.emil.hnrpmc.simpleclans.conversation.dings.ConvoCanceller;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;


public class RequestCanceller implements ConvoCanceller {

    @NotNull
    private final String cancelledMessage;
    @NotNull
    private final String escapeSequence;

    public RequestCanceller(@NotNull String escapeSequence, @NotNull String cancelledMessage) {
        this.escapeSequence = escapeSequence;
        this.cancelledMessage = cancelledMessage;
    }

    public RequestCanceller(@NotNull CommandSourceStack sender, @NotNull String cancelledMessage) {
        this(lang("cancel", sender), cancelledMessage);
    }

    @Override
    public void setConversation(@NotNull ClanConvo conversation) {
    }

    @Override
    public boolean cancelBasedOnInput(@NotNull SCConversation context, @NotNull String input) {
        if (input.equalsIgnoreCase(escapeSequence)) {
            context.getForWhom().sendRawMessage(cancelledMessage);
            return true;
        }

        return false;
    }

    //a clone that is not a clone, nice one, Bukkit
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NotNull
    @Override
    public ConvoCanceller clone() {
        return new RequestCanceller(this.escapeSequence, this.cancelledMessage);
    }
}
