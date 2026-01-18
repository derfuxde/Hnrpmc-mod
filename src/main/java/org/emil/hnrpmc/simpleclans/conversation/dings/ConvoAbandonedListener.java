package org.emil.hnrpmc.simpleclans.conversation.dings;

import java.util.EventListener;
import org.jetbrains.annotations.NotNull;

public interface ConvoAbandonedListener extends EventListener {
    void conversationAbandoned(@NotNull ConvoAbandonedEvent var1);
}
