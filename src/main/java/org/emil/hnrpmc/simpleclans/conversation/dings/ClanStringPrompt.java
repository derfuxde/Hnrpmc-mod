//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import org.emil.hnrpmc.simpleclans.conversation.Prompt;
import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClanStringPrompt implements Prompt {
    public boolean blocksForInput(@NotNull SCConversation context) {
        return true;
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull SCConversation var1, @Nullable String var2) {
        return null;
    }
}
