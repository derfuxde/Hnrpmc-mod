package org.emil.hnrpmc.hnessentials.cosmetics.impl;


import com.google.gson.JsonObject;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Cape;
import org.emil.hnrpmc.hnessentials.cosmetics.api.User;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.Yootil;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

class BaseCape implements Cape {
    BaseCape(String id, String name, String origin, String image, boolean cosmeticaAlternative, int frameDelay) {
        this.id = id;
        this.name = name;
        this.origin = origin;
        this.image = image;
        this.cosmeticaAlternative = cosmeticaAlternative;
        this.frameDelay = frameDelay;
    }

    private final String id;
    private final String name;
    private final String origin;
    private final String image;
    private final boolean cosmeticaAlternative;
    private final int frameDelay;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOrigin() {
        return this.origin;
    }

    @Override
    public String getImage() {
        return this.image;
    }

    @Override
    public boolean isCosmeticaAlternative() {
        return this.cosmeticaAlternative;
    }

    @Override
    public int getFrameDelay() {
        return this.frameDelay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseCape baseCape = (BaseCape) o;
        return id.equals(baseCape.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    static Optional<Cape> parse(@Nullable JsonObject data) {
        if (data == null) return Optional.empty();

        String id = data.get("id").getAsString();
        String origin = data.get("origin").getAsString();
        String image = data.get("image").getAsString();
        String name = data.get("name").getAsString();
        int frameDelay = data.get("extraInfo").getAsInt();

        boolean cosmeticaAlternative = data.get("isCosmeticaAlternative").getAsBoolean();

        if ("Cosmetica".equals(origin)) {
            User owner = new User(Yootil.toUUID(data.get("owner").getAsString()), data.get("ownerName").getAsString());
            return Optional.of(new CosmeticaCape(id, name, origin, image, cosmeticaAlternative, frameDelay, data.get("uploaded").getAsLong(), owner));
        }
        else {
            return Optional.of(new BaseCape(id, name, origin, image, cosmeticaAlternative, frameDelay));
        }
    }
}