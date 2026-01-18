package org.emil.hnrpmc.simpleclans.hooks.placeholder;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

@FunctionalInterface
public interface PlaceholderResolver {
    /**
     * @param player Das Profil des Spielers (null bei Konsole/Global)
     * @param context Das Objekt, auf dem der Platzhalter basiert (z.B. ein Clan-Objekt)
     * @param placeholder Der vollständige Platzhalter-String
     * @param config Die konfigurierten Parameter für diesen Resolver
     */
    String resolve(@Nullable GameProfile player, @NotNull Object context, @NotNull String placeholder, @NotNull Map<String, String> config);
}