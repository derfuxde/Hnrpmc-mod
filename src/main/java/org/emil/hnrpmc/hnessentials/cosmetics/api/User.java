package org.emil.hnrpmc.hnessentials.cosmetics.api;


import java.util.Objects;
import java.util.UUID;

/**
 * Represents a cosmetica user.
 */
public final class User {
    public User(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    private final UUID uuid;
    private final String username;

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        User that = (User) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username);
    }

    @Override
    public String toString() {
        return "User[" +
                "uuid=" + uuid + ", " +
                "username=" + username + ']';
    }

}
