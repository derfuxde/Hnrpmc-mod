package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.ICancellableEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.loggers.BankOperator;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

public class ClanBalanceUpdateEvent extends Event implements ICancellableEvent {


    private final BankOperator updater;
    private final Clan clan;
    private final Cause cause;
    private final double balance;
    private double newBalance;
    private boolean cancelled;

    public ClanBalanceUpdateEvent(@NotNull BankOperator updater,
                                  @NotNull Clan clan,
                                  double balance,
                                  double newBalance,
                                  @NotNull Cause cause) {
        this.updater = updater;
        this.balance = balance;
        this.newBalance = newBalance;
        this.clan = clan;
        this.cause = cause;
    }

    /**
     * @return the balance updater
     */
    public @NotNull BankOperator getUpdater() {
        return updater;
    }

    /**
     * @return the Clan involved
     */
    public @NotNull Clan getClan() {
        return clan;
    }

    /**
     * @return the Clan's current balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @return the Clan's new balance
     */
    public double getNewBalance() {
        return newBalance;
    }

    /**
     * Sets the Clan's new balance
     *
     * @param newBalance the new balance
     * @throws IllegalArgumentException if newBalance is negative
     */
    public void setNewBalance(double newBalance) {
        if (newBalance < 0) {
            throw new IllegalArgumentException("newBalance cannot be negative");
        }
        this.newBalance = newBalance;
    }

    /**
     * @return the update cause
     */
    public @NotNull Cause getCause() {
        return cause;
    }

    public enum Cause {
        UPKEEP,
        MEMBER_FEE,
        /**
         * When a command such as /clan bank deposit causes the update
         */
        COMMAND,
        /**
         * When the balance is updated via API methods
         */
        API,
        /**
         * WHen the balance is updated via Internal methods
         */
        INTERNAL,
        /**
         * When the clan data is being loaded and the balance is set, usually on server start up or plugin reload
         */
        LOADING,
        /**
         * When a failed deposit is being refunded, cannot be cancelled
         */
        REVERT
    }
}
