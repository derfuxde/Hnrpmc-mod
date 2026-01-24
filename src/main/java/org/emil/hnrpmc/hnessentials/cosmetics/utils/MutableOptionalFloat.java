package org.emil.hnrpmc.hnessentials.cosmetics.utils;


import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MutableOptionalFloat {
    private MutableOptionalFloat() {
        this.isPresent = false;
    }

    private MutableOptionalFloat(float value) {
        this.isPresent = true;
        this.value = value;
    }

    private float value;
    private boolean isPresent;

    public float get() throws NullPointerException {
        if (!this.isPresent) {
            throw new NullPointerException("No value present.");
        }

        return this.value;
    }

    public float orElse(float value) {
        if (!this.isPresent) {
            this.isPresent = true;
            this.value = value;
        }

        return this.value;
    }

    public float orElseGet(FloatSupplier supplier) {
        if (!this.isPresent) {
            this.isPresent = true;
            this.value = supplier.get();
        }

        return this.value;
    }

    public MutableOptionalFloat computeIfAbsent(Supplier<@Nullable Float> ifAbsent) {
        if (!this.isPresent) {
            @Nullable Float newValue = ifAbsent.get();

            if (newValue != null) {
                this.isPresent = true;
                this.value = newValue;
            }
        }

        return this;
    }

    public static MutableOptionalFloat of(float f) {
        return new MutableOptionalFloat(f);
    }

    public static MutableOptionalFloat empty() {
        return new MutableOptionalFloat();
    }

    @FunctionalInterface
    public interface FloatSupplier {
        float get();
    }
}
