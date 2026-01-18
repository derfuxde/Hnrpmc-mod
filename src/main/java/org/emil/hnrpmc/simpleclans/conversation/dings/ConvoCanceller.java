//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.jetbrains.annotations.NotNull;

public interface ConvoCanceller extends Cloneable {
    void setConversation(@NotNull ClanConvo var1);

    boolean cancelBasedOnInput(@NotNull SCConversation var1, @NotNull String var2);

    @NotNull ConvoCanceller clone();
}
