package org.emil.hnrpmc.simpleclans;

public enum ClanColors {
    BLACK("black", "§0"),
    DARK_BLUE("dark_blue", "§1"),
    DARK_GREEN("dark_green", "§2"),
    DARK_AQUA("dark_aqua", "§3"),
    DARK_RED("dark_red", "§4"),
    DARK_PURPLE("dark_purple", "§5"),
    GOLD("gold", "§6"),
    GRAY("gray", "§7"),
    DARK_GRAY("dark_gray", "§8"),
    BLUE("blue", "§9"),
    GREEN("green", "§a"),
    AQUA("aqua", "§b"),
    RED("red", "§c"),
    LIGHT_PURPLE("light_purple", "§d"),
    YELLOW("yellow", "§e"),
    WHITE("white", "§f"),
    MINECOIN_GOLD("minecoin_gold", "§g"),
    MATERIAL_QUARTZ("material_quartz", "§h"),
    MATERIAL_IRON("material_iron", "§i"),
    MATERIAL_NETHERITE("material_netherite", "§j"),
    MATERIAL_REDSTONE("material_redstone", "§m"),
    MATERIAL_COPPER("material_copper", "§n"),
    MATERIAL_GOLD("material_gold", "§p"),
    MATERIAL_EMERALD("material_emerald", "§q"),
    MATERIAL_DIAMOND("material_diamond", "§s"),
    MATERIAL_LAPIS("material_lapis", "§t"),
    MATERIAL_AMETHYST("material_amethyst", "§u"),
    MATERIAL_RESIN("material_resin", "§v");

    private final String colorname;
    private final String colorhex;

    ClanColors(String colorname, String defaultValue) {
        this.colorname = colorname;
        this.colorhex = defaultValue;
    }

    public String getColorname() {
        return colorname;
    }

    ClanColors(String colorname) {
        this.colorname = colorname;
        colorhex = null;
    }

    public String getColorhex() {
        return colorhex;
    }
}

