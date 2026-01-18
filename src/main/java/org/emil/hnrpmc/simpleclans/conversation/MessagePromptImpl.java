package org.emil.hnrpmc.simpleclans.conversation;

import org.emil.hnrpmc.simpleclans.conversation.dings.ClanMessagePrompt;
import org.jetbrains.annotations.NotNull;

/**
 * @author roinujnosde
 */
public class MessagePromptImpl extends ClanMessagePrompt {
    private final String message;
    private Prompt nextPrompt;
    public MessagePromptImpl(String message) {
        this.message = message;
    }

    public MessagePromptImpl(String message, Prompt nextPrompt) {
        this(message);
        this.nextPrompt = nextPrompt;
    }

    @Override
    protected Prompt getNextPrompt(@NotNull SCConversation cc) {
        return nextPrompt != null ? nextPrompt : END_OF_CONVERSATION;
    }

    @Override
    public @NotNull String getPromptText(@NotNull SCConversation cc) {
        return message;
    }
}
