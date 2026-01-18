//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public interface Convosable {
    boolean isConversing();

    void acceptConversationInput(@NotNull String var1);

    boolean beginConversation(@NotNull ClanConvo var1);

    void abandonConversation(@NotNull ClanConvo var1);

    void abandonConversation(@NotNull ClanConvo var1, @NotNull ConvoAbandonedEvent var2);

    @Obsolete
    void sendRawMessage(@NotNull String var1);

    /** @deprecated */
    @Deprecated
    void sendRawMessage(@Nullable UUID var1, @NotNull String var2);

    @NotNull
    UUID getUUID();

    @NotNull
    ServerPlayer getPlayer();
}
