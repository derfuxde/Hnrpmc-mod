package org.emil.hnrpmc.hnessentials.cosmetics.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lädt Kosmetiken lokal aus resource/hnrpmc/cosmetics.
 */
public class CosmeticResourceResponse<T> {
    private final @Nullable T value;
    private final @Nullable RuntimeException exception;
    private final Path resourcePath;

    // Konstruktor für erfolgreiches Laden
    public CosmeticResourceResponse(T t, Path path) {
        if (t == null) throw new IllegalArgumentException("Object t cannot be null.");
        this.value = t;
        this.exception = null;
        this.resourcePath = path;
    }

    // Konstruktor für Fehler beim Laden
    public CosmeticResourceResponse(Exception e, Path path) {
        if (e == null) throw new IllegalArgumentException("Exception cannot be null.");
        this.value = null;
        this.resourcePath = path;
        this.exception = (e instanceof RuntimeException) ?
                (RuntimeException) e : new UncheckedIOException((IOException) e);
    }

    /**
     * Gibt den Pfad zur lokalen Ressource zurück.
     */
    public String getLocalPath() {
        return this.resourcePath.toString();
    }

    // --- Bestehende Logik aus ServerResponse ---

    public T get() throws RuntimeException {
        if (this.value == null) throw this.exception;
        return this.value;
    }

    public boolean isSuccessful() {
        return this.value != null;
    }

    public Optional<T> getAsOptional() {
        return Optional.ofNullable(this.value);
    }

    public void ifSuccessful(Consumer<T> callback) {
        if (this.value != null) callback.accept(this.value);
    }

    public void ifError(Consumer<RuntimeException> callback) {
        if (this.value == null) callback.accept(this.exception);
    }

    // Statische Hilfsmethode zum Laden (Beispiel)
    public static <T> CosmeticResourceResponse<T> loadLocal(String fileName, Function<File, T> loader) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("hnrpmc", "models/cosmetics/" + fileName + ".json");
        Path path = Path.of(location.getPath());
        File file = path.toFile();

        try {
            if (!file.exists()) throw new IOException("Datei nicht gefunden: " + path.toString());
            T data = loader.apply(file);
            return new CosmeticResourceResponse<>(data, path);
        } catch (Exception e) {
            return new CosmeticResourceResponse<>(e, path);
        }
    }
}