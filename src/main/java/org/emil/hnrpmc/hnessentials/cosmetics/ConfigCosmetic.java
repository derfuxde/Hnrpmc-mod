package org.emil.hnrpmc.hnessentials.cosmetics;

public class ConfigCosmetic {
    private final String ID;
    private final String name;
    private final String Type;
    private boolean showHelmet = true;

    public ConfigCosmetic(String name, String ID, String Type, boolean showHelmet) {
        this.name = name;
        this.ID = ID;
        this.Type = Type;
        this.showHelmet = showHelmet;
    }

    public ConfigCosmetic(String name, String ID, String Type) {
        this.name = name;
        this.ID = ID;
        this.Type = Type;
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

    public boolean showHelmet() {
        return showHelmet;
    }
}
