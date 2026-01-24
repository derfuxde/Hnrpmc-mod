package org.emil.hnrpmc.hnessentials.commands.PlayerData;

import java.util.List;

public record AdminSyncData(
        double money,
        String prefix,
        String suffix,
        List<String> activeCosmeticIds, // Nur IDs als Strings!
        boolean jailed
) {}