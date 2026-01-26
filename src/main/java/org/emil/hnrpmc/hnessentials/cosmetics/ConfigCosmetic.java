package org.emil.hnrpmc.hnessentials.cosmetics;

public class ConfigCosmetic {
    private final String ID;
    private final String name;
    private final String Type;
    private final String textureoverride;

    public ConfigCosmetic(String name, String ID, String Type, String textureoverride) {
        this.name = name;
        this.ID = ID;
        this.Type = Type;
        this.textureoverride = textureoverride;
    }

    public ConfigCosmetic(String name, String ID, String Type) {
        this.name = name;
        this.ID = ID;
        this.Type = Type;
        this.textureoverride = "";
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }

    public String getType() {
        return Type;
    }

    public String getTextureoverride() {
        return textureoverride;
    }
}
