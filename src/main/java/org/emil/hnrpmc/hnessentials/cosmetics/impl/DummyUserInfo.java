package org.emil.hnrpmc.hnessentials.cosmetics.impl;

import org.emil.hnrpmc.hnessentials.cosmetics.api.Cape;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.api.ShoulderBuddies;
import org.emil.hnrpmc.hnessentials.cosmetics.api.UserInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DummyUserInfo implements UserInfo {
    @Override
    @Nullable
    public String getSkin() {
        // steve skin
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAABJlBMVEUAAAD////GloC9jnS9jnK9i3K7iXK+iGy2iWy3gnK0hG0AzMytgG21e2eze2KqfWasdlqcclwAr6+Wb1uiakedak8AqKicaUyfaEmcZ0iaZEqcY0acY0WaY0QAnp6WX0GWX0AAmZlra2uQXkOPXj6KWTuIWjmHWDqHVTuDVTuGUzSBUzmEUjGAUzRWScwAf396TjN0SC91Ry93QjVvRSxtQypSPYlqQDBiQy9GOqUAaGgAYGAAW1s/Pz86MYlRMSVSKCYwKHJCKhI/KhU6KBQmIVsoKCg0JRIzJBEyIxAvIhEjIyMvIA0tIBAvHw8sHhEsHg4tHQ4rHg4rHg0qHQ0pHAwoHAsoGwsoGwonGwsoGg0mGgwmGgomGAskGAokGAgjFwkfEAs7wqHyAAAAAXRSTlMAQObYZgAAAyZJREFUeNrt1ltXm0AQAGDtxbbSlnbQtbbKdm1VJDarsRXZIimtWC8V0YBiJKL//0/07G7QXIwhyavzQi7sx+wy58yMjTUjirIoyrKbSjPGBo0oC4Ioy7KKZVqmOQwQBKkEDg8PD63BgSzgewiioYEoiNI0GAUQ63Pg1xBAGsTpDQdEDAHEaZqmUTTwa0yS9CLePzr6VwvOsrQWHR3txxdpkuR1UQRIw7i2V6vV9n7/Cc/2anGYcqBZF32BsJHGp+dXce3lsyfPn777G1+dn8ZpI8zron8G9ZMwuQgP1En19fuJmZmD8CIJT+pJXhcFtpBcXi/P7UyOb76Z2BxXd+aWry+TJMnrogBQbxyv4qVPb19Z1oupb0t49bhR54co66L/GSQnW9PzZuXL542NjY2vpYo5P711koR5XfQFTHPenMWL699/lEprKytr64sfZj/OzptmhddFxSxUC+Vyuby7u7ubf1dkLSk/m5H/jhQFWVWr2h+Q5dwFKIqijAqgqlW1RtpCF+B5vmfbtu35Hr+6lFKH6KWpUknXKaVURwgBICQ/g7gCANwBfhtgU8YoQnqppANQxhjidwNCjDEKCHT+fwfgtQHiRkA6QQCMMQYykIBBoA8DDmMOAOgEAORnAohIQIeHMvB8z3VdlzmOQwhfRojjOI5YTQAwxoaqqqqBMebXdsB1bX4WHHAchxH5UMIch4H4giSgqSo27gNaz4ADAAQJgDGxmhAwJKAJQNNaAM/regs5QMUh3gL8yRww2jPIAe8OALEKSH5gPAxsSAB3ANvbObC9vbCwsEAZpYTwBCRACBHHgCWgYp6JpmotgFyYXz3f8zlou0AoLzLXRYS4tgQ0VWTSBxAZ2bZL8vImtm3zJ2uqphmdGTzGYzzGYwwyL6BeXbnwwKEoykgA6jVYDLKFwkDrvODyPkEpRYD0rrmgP+DfNhodENBmqy8OeH5rp0JdbX0Q4N65oCfQMS/0nAt6A76cF26BvKUVBTq2wAFNAkbbXNAfkFsxBCC6slEwA68DwBIwCgKd8wJu7p2PNnwrBYD2di+erI0CYGxomhht7gX+AwpAOZvrRgeGAAAAAElFTkSuQmCC";
    }

    @Override
    public boolean isSlim() {
        return false;
    }

    @Override
    public String getLore() {
        return "";
    }

    @Override
    public String getPlatform() {
        return "java";
    }

    @Override
    public String getRole() {
        return "none";
    }

    @Override
    public boolean isUpsideDown() {
        return false;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public Optional<String> getClient() {
        return Optional.empty();
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getIcon() {
        return "";
    }

    @Override
    public List<Model> getHats() {
        return new ArrayList<>();
    }

    @Override
    public Optional<ShoulderBuddies> getShoulderBuddies() {
        return Optional.empty();
    }

    @Override
    public Optional<Model> getBackBling() {
        return Optional.empty();
    }

    @Override
    public Optional<Cape> getCape() {
        return Optional.empty();
    }
}
