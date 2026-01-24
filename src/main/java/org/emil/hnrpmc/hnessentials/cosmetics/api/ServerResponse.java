package org.emil.hnrpmc.hnessentials.cosmetics.api;

import org.emil.hnrpmc.hnessentials.cosmetics.utils.SafeURL;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerResponse<T> {
    public ServerResponse(T t, SafeURL url) throws IllegalArgumentException {
        if (t == null) throw new IllegalArgumentException("Object t cannot be null.");
        this.value = t;
        this.exception = null;
        this.url = url;
    }

    public ServerResponse(RuntimeException e, SafeURL url) throws IllegalArgumentException {
        if (e == null) throw new IllegalArgumentException("RuntimeException e cannot be null.");
        this.value = null;
        this.exception = e;
        this.url = url;
    }

    public ServerResponse(IOException ie, SafeURL url) throws IllegalArgumentException {
        if (ie == null) throw new IllegalArgumentException("IOException ie cannot be null.");
        this.value = null;
        this.exception = new UncheckedIOException(ie);
        this.url = url;
    }

    @Nullable
    private final T value;
    @Nullable
    private final RuntimeException exception;
    private final SafeURL url;

    /**
     * @return the url that was contacted to receive this response. Will not include the token.
     */
    public String getURL() {
        return this.url.safeUrl();
    }

    /**
     * Tries to get the value stored. If that value is an exception, it will be thrown.
     * @throws RuntimeException if the object stores an exception. The exception thrown is the exact same as the one stored.
     */
    public T get() throws RuntimeException {
        if (this.value == null) throw this.exception;
        return this.value;
    }

    @Nullable
    public T getOrNull() {
        return this.value;
    }

    public Optional<T> getAsOptional() {
        return Optional.ofNullable(this.value);
    }

    @Nullable
    public Exception getException() {
        return this.exception;
    }

    /**
     * @param errorResolver the function to handle errors and resolve a value.
     * @return the resolved value.
     * @apiNote nullable if and only if the error resolver passed can return null.
     */
    @Nullable
    public T resolve(Function<Exception, @Nullable T> errorResolver) {
        if (this.value == null) return errorResolver.apply(this.exception);
        return this.value;
    }

    /**
     * @param fallback the value to return if there was an error.
     * @return the resolved value.
     * @apiNote nullable if and only if the error resolver passed can return null.
     */
    @Nullable
    public T or(T fallback) {
        if (this.value == null) return fallback;
        return this.value;
    }

    /**
     * Retrieves the value stored. If there is an error instead, it logs the exception and returns the fallback value.
     *
     * @param fallback the value to return if there was an error.
     * @return the resolved value.
     * @apiNote nullable if and only if the error resolver passed can return null.
     */
    @Nullable
    public T orLogAndUse(T fallback) {
        if (this.value == null) {
            this.exception.printStackTrace();
            return fallback;
        }

        return this.value;
    }

    /**
     * Basically your generic "isPresent" optional function.
     * @return whether there is a value stored in here (as opposed to an error).
     */
    public boolean isSuccessful() {
        return this.value != null;
    }

    /**
     * Runs the given callback with the value if successful.
     */
    public void ifSuccessful(Consumer<T> callback) {
        if (this.value != null) {
            callback.accept(this.value);
        }
    }

    /**
     * Runs the given callback with the error if there was an error.
     */
    public void ifError(Consumer<RuntimeException> callback) {
        if (this.value == null) {
            callback.accept(this.exception);
        }
    }

    /**
     * Runs the given callback with the value if successful, otherwise runs the "or else" branch with the error.
     */
    public void ifSuccessfulOrElse(Consumer<T> successCallback, Consumer<RuntimeException> errorCallback) {
        if (this.value == null) {
            errorCallback.accept(this.exception);
        } else {
            successCallback.accept(this.value);
        }
    }
}
