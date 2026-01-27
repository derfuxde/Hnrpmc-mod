package org.emil.hnrpmc.hnessentials.ChestLocks.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record LockData(UUID owner, List<UUID> trusted) {
    // Codec für Serialisierung (Speichern auf der Festplatte)
    public static final Codec<LockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("owner").forGetter(LockData::owner),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).listOf().fieldOf("trusted").forGetter(LockData::trusted)
    ).apply(instance, LockData::new));

    public boolean canAccess(UUID player) {
        return player.equals(owner) || trusted.contains(player);
    }

    public LockData addTrusted(UUID friend) {
        List<UUID> newTrusted = new ArrayList<>(trusted);
        if (!newTrusted.contains(friend)) newTrusted.add(friend);
        return new LockData(owner, newTrusted);
    }
}