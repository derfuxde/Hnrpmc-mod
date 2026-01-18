//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.emil.hnrpmc.simpleclans.conversation.dings;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class InactivityConvoCanceller implements ConvoCanceller {
    protected SimpleClans plugin;
    protected int timeoutSeconds;
    protected ClanConvo conversation;
    private ScheduledFuture<?> timerTask = null;

    public InactivityConvoCanceller(@NotNull SimpleClans plugin, int timeoutSeconds) {
        this.plugin = plugin;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setConversation(@NotNull ClanConvo conversation) {
        this.conversation = conversation;
        this.startTimer();
    }

    public boolean cancelBasedOnInput(@NotNull SCConversation context, @NotNull String input) {
        this.stopTimer();
        this.startTimer();
        return false;
    }

    public @NotNull ConvoCanceller clone() {
        return new InactivityConvoCanceller(this.plugin, this.timeoutSeconds);
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private void startTimer() {
        // Falls noch ein alter Timer läuft, sicherheitshalber stoppen
        stopTimer();

        // schedule() im ScheduledExecutorService des Servers nutzen
        // Wir nutzen den Server-Executor, da er Thread-sicher ist
        this.timerTask = scheduler.schedule(() -> {

            // WICHTIG: Code, der Spiel-Logik ändert, muss oft zurück auf den Main-Thread
            this.plugin.getServer().execute(() -> {
                if (this.conversation.getState() == ClanConvo.ConversationState.UNSTARTED) {
                    this.startTimer();
                } else if (this.conversation.getState() == ClanConvo.ConversationState.STARTED) {
                    this.cancelling(this.conversation);
                    // Dein Event-Aufruf
                    this.conversation.abandon(new ConvoAbandonedEvent(this.conversation, this));
                }
            });

        }, this.timeoutSeconds, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        if (this.timerTask != null) {
            // cancel(false) unterbricht den Task, wenn er noch nicht ausgeführt wurde
            this.timerTask.cancel(false);
            this.timerTask = null;
        }
    }

    protected void cancelling(@NotNull ClanConvo conversation) {
    }
}
