package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Dieses Event wird gefeuert, wenn Geld vergeben oder abgezogen wird.
 * Das Abbrechen verhindert die Transaktion.
 * * @author RoinujNosde (Ported to NeoForge)
 */
public class EconomyTransactionEvent extends Event implements ICancellableEvent {

    private final UUID playerUuid;
    private double amount;
    private final Cause cause;
    private final TransactionType transactionType;

    public EconomyTransactionEvent(@NotNull UUID affected, double amount,
                                   @NotNull Cause cause, @NotNull TransactionType transactionType) {
        this.playerUuid = affected;
        this.amount = amount;
        this.cause = cause;
        this.transactionType = transactionType;
    }

    public @NotNull UUID getPlayerUuid() {
        return playerUuid;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public @NotNull Cause getCause() {
        return cause;
    }

    public @NotNull TransactionType getTransactionType() {
        return transactionType;
    }

    // ICancellableEvent übernimmt die Verwaltung von isCanceled() und setCanceled().

    public enum Cause {
        CLAN_CREATION,
        CLAN_VERIFICATION,
        CLAN_INVITATION,
        CLAN_REGROUP,
        CLAN_HOME_TELEPORT,
        CLAN_HOME_TELEPORT_SET,
        DISCORD_CREATION,
        PLAYER_KILLED,
        MEMBER_FEE_SET,
        RESET_KDR
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }
}