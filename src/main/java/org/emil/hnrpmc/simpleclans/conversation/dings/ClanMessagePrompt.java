//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import org.emil.hnrpmc.simpleclans.conversation.Prompt;
import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClanMessagePrompt implements Prompt {
    public boolean blocksForInput(@NotNull SCConversation context) {
        return false;
    }

    public @Nullable Prompt acceptInput(@NotNull SCConversation context, @Nullable String input) {
        return this.getNextPrompt(context);
    }

    protected abstract @Nullable Prompt getNextPrompt(@NotNull SCConversation var1);
}
