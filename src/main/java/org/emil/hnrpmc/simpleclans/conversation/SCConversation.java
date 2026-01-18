package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanConvo;
import org.emil.hnrpmc.simpleclans.conversation.dings.ConvoCanceller;
import org.emil.hnrpmc.simpleclans.conversation.dings.Convosable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SCConversation extends ClanConvo {
    private static final Map<UUID, SCConversation> conversations = new HashMap<>();

    private final Convosable forWhom;
    //private final Map<Object, Object> sessionData;
    //private final SimpleClans plugin;

    private final Map<Object, Object> internalSessionData;
    private final SimpleClans pluginInstance;

    public SCConversation(@NotNull SimpleClans plugin, @NotNull Convosable forWhom, @Nullable Prompt firstPrompt) {
        this(plugin, forWhom, firstPrompt, new HashMap<>(), 10);
    }

    public SCConversation(@NotNull SimpleClans plugin, @NotNull Convosable forWhom, @Nullable Prompt firstPrompt, int timeout) {
        this(plugin, forWhom, firstPrompt, new HashMap<>(), timeout);
    }

    public SCConversation(@NotNull SimpleClans plugin, @NotNull Convosable forWhom, @Nullable Prompt firstPrompt, @NotNull Map<Object, Object> initialSessionData) {
        this(plugin, forWhom, firstPrompt, initialSessionData, 10);
    }

    public SCConversation(@NotNull SimpleClans plugin, @NotNull Convosable forWhom, @Nullable Prompt firstPrompt, @NotNull Map<Object, Object> initialSessionData, int timeout) {
        super(plugin, forWhom, firstPrompt, initialSessionData);

        // Zuweisung an die protected Felder der Superklasse ClanConvo
        this.context = this;
        this.currentPrompt = firstPrompt;
        this.pluginInstance = plugin;
        this.internalSessionData = initialSessionData;
        this.forWhom = forWhom;

        this.setLocalEchoEnabled(true);
        this.addConversationCanceller(new InactivityCanceller(plugin, timeout));
    }

    @Override
    public void begin() {
        UUID uniqueId = getForWhom().getUUID();
        SCConversation oldConversation = conversations.get(uniqueId);

        if (oldConversation != this && oldConversation != null) {
            getForWhom().abandonConversation(oldConversation);
        }

        conversations.put(uniqueId, this);
        super.begin();
    }

    public void addConversationCanceller(@NotNull ConvoCanceller canceller) {
        canceller.setConversation(this);
        cancellers.add(canceller);
    }

    public @NotNull Convosable getForWhom() {
        return this.forWhom;
    }

    public static @Nullable SCConversation getConversation(@NotNull UUID uuid) {
        return conversations.get(uuid);
    }

    public static void removeConversation(@NotNull UUID uuid) {
        conversations.remove(uuid);
    }


    public @NotNull SCConversation getContext() {
        return this.context;
    }

    public @NotNull Map<Object, Object> getAllSessionData() {
        return this.internalSessionData;
    }

    public @Nullable Object getSessionData(@NotNull Object key) {
        return this.internalSessionData.get(key);
    }

    public void setSessionData(@NotNull Object key, @Nullable Object value) {
        this.internalSessionData.put(key, value);
    }

    public @Nullable SimpleClans getPlugin() {
        return this.pluginInstance;
    }
}
