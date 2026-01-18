//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.Prompt;
import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanConvo {
    private Prompt firstPrompt;
    private boolean abandoned;
    protected Prompt currentPrompt;
    protected SCConversation context;
    protected boolean modal;
    protected boolean localEchoEnabled;
    protected ConvoPrefix prefix;
    protected List<ConvoCanceller> cancellers;
    protected List<ConvoAbandonedListener> abandonedListeners;

    public ClanConvo(@Nullable SimpleClans plugin, @NotNull Convosable forWhom, @Nullable Prompt firstPrompt) {
        this(plugin, forWhom, firstPrompt, new HashMap());
    }

    public ClanConvo(@Nullable SimpleClans plugin, @NotNull Convosable forWhom, @Nullable Prompt firstPrompt, @NotNull Map<Object, Object> initialSessionData) {
        this.firstPrompt = firstPrompt;
        this.currentPrompt = firstPrompt; // WICHTIG: Hier direkt setzen
        this.modal = true;
        this.localEchoEnabled = true;
        this.prefix = new NullConvoPrefix();
        this.cancellers = new ArrayList<>();
        this.abandonedListeners = new ArrayList<>();
    }

    public @NotNull Convosable getForWhom() {
        return this.context.getForWhom();
    }

    public boolean isModal() {
        return this.modal;
    }

    void setModal(boolean modal) {
        this.modal = modal;
    }

    public boolean isLocalEchoEnabled() {
        return this.localEchoEnabled;
    }

    public void setLocalEchoEnabled(boolean localEchoEnabled) {
        this.localEchoEnabled = localEchoEnabled;
    }

    public @NotNull ConvoPrefix getPrefix() {
        return this.prefix;
    }

    void setPrefix(@NotNull ConvoPrefix prefix) {
        this.prefix = prefix;
    }

    void addConversationCanceller(@NotNull ConvoCanceller canceller) {
        canceller.setConversation(this);
        this.cancellers.add(canceller);
    }

    public @NotNull List<ConvoCanceller> getCancellers() {
        return this.cancellers;
    }

    public @NotNull SCConversation getContext() {
        return this.context;
    }

    public void begin() {
        this.abandoned = false;
        // Entferne die Prüfung auf null, falls sie stört, aber wichtig ist:
        if (this.context == null) {
            SimpleClans.getInstance().getLogger().error("Context is NULL in ClanConvo!");
            return;
        }
        this.currentPrompt = this.firstPrompt;

        if (this.context.getForWhom().beginConversation(this)) {
            this.outputNextPrompt();
        }
    }

    public @NotNull ConversationState getState() {
        if (this.currentPrompt != null) {
            return ClanConvo.ConversationState.STARTED;
        } else {
            return this.abandoned ? ClanConvo.ConversationState.ABANDONED : ClanConvo.ConversationState.UNSTARTED;
        }
    }

    public void acceptInput(@NotNull String input) {
        if (this.currentPrompt != null) {
            if (this.localEchoEnabled) {
                Convosable var10000 = this.context.getForWhom();
                String var10001 = this.prefix.getPrefix(this.context);
                var10000.sendRawMessage(var10001 + input);
            }

            for(ConvoCanceller canceller : this.cancellers) {
                if (canceller.cancelBasedOnInput(this.context, input)) {
                    this.abandon(new ConvoAbandonedEvent(this, canceller));
                    return;
                }
            }

            this.currentPrompt = this.currentPrompt.acceptInput(this.context, input);
            this.outputNextPrompt();
        }

    }

    public synchronized void addConversationAbandonedListener(@NotNull ConvoAbandonedListener listener) {
        this.addConversationAbandonedListener(details -> {
            SCConversation.removeConversation(getForWhom().getUUID());
        });

    }

    public synchronized void removeConversationAbandonedListener(@NotNull ConvoAbandonedListener listener) {
        this.abandonedListeners.remove(listener);
    }

    public void abandon() {
        this.abandon(new ConvoAbandonedEvent(this, new ManuallyAbandonedConvoCanceller()));
    }

    public synchronized void abandon(@NotNull ConvoAbandonedEvent details) {
        if (!this.abandoned) {
            this.abandoned = true;
            this.currentPrompt = null;
            this.context.getForWhom().abandonConversation(this);

            for(ConvoAbandonedListener listener : this.abandonedListeners) {
                listener.conversationAbandoned(details);
            }
        }

    }

    public void outputNextPrompt() {
        if (this.currentPrompt == null) {
            this.abandon(new ConvoAbandonedEvent(this));
        } else {
            Convosable var10000 = this.context.getForWhom();
            String var10001 = this.prefix.getPrefix(this.context);
            var10000.sendRawMessage(var10001 + this.currentPrompt.getPromptText(this.context));
            if (!this.currentPrompt.blocksForInput(this.context)) {
                this.currentPrompt = this.currentPrompt.acceptInput(this.context, (String)null);
                this.outputNextPrompt();
            }
        }

    }

    public static enum ConversationState {
        UNSTARTED,
        STARTED,
        ABANDONED;
    }
}
