package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.conversation.dings.Convosable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Prompt {
    Prompt END_OF_CONVERSATION = null;

    @NotNull String getPromptText(@NotNull SCConversation var1);

    boolean blocksForInput(@NotNull SCConversation var1);

    @Nullable Prompt acceptInput(@NotNull SCConversation var1, @Nullable String var2);
}
