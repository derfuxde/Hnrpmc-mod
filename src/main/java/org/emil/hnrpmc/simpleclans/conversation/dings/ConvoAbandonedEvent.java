//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import java.util.EventObject;

import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConvoAbandonedEvent extends EventObject {
    private SCConversation context;
    private ConvoCanceller canceller;

    public ConvoAbandonedEvent(@NotNull ClanConvo conversation) {
        this(conversation, (ConvoCanceller)null);
    }

    public ConvoAbandonedEvent(@NotNull ClanConvo conversation, @Nullable ConvoCanceller canceller) {
        super(conversation);
        this.context = conversation.getContext();
        this.canceller = canceller;
    }

    public @Nullable ConvoCanceller getCanceller() {
        return this.canceller;
    }

    public @NotNull SCConversation getContext() {
        return this.context;
    }

    public boolean gracefulExit() {
        return this.canceller == null;
    }
}
