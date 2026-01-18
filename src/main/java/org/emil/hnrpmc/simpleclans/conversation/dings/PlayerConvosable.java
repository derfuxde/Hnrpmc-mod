package org.emil.hnrpmc.simpleclans.conversation.dings;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanConvo;
import org.emil.hnrpmc.simpleclans.conversation.dings.ConvoAbandonedEvent;
import org.emil.hnrpmc.simpleclans.conversation.dings.Convosable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerConvosable implements Convosable {
    private final ServerPlayer player;

    public PlayerConvosable(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public boolean isConversing() {
        // Hier prüfen, ob der Spieler gerade in einer Konversation ist
        return false;
    }

    @Override
    public void sendRawMessage(@NotNull String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    // Implementiere die restlichen Methoden des Interfaces...
    @Override public void acceptConversationInput(@NotNull String var1) {}
    @Override public boolean beginConversation(@NotNull ClanConvo var1) { return true; }
    @Override public void abandonConversation(@NotNull ClanConvo var1) {}
    @Override public void abandonConversation(@NotNull ClanConvo var1, @NotNull ConvoAbandonedEvent var2) {}
    @Override public void sendRawMessage(@Nullable UUID var1, @NotNull String var2) {
        sendRawMessage(var2);
    }

    @Override
    @NotNull
    public UUID getUUID() {
        return player.getUUID();
    }

    @Override
    public @NotNull ServerPlayer getPlayer() {
        return player;
    }
}