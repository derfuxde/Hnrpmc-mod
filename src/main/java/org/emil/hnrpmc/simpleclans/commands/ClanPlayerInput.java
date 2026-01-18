package org.emil.hnrpmc.simpleclans.commands;

import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClanPlayerInput {

    private final ClanPlayer clanPlayer;

    public ClanPlayerInput(@NotNull ClanPlayer clanPlayer) {
        this.clanPlayer = clanPlayer;
    }

    public ClanPlayer getClanPlayer() {
        return clanPlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClanPlayerInput that = (ClanPlayerInput) o;
        return clanPlayer.equals(that.clanPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clanPlayer);
    }
}
