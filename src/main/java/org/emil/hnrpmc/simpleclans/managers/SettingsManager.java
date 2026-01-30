package org.emil.hnrpmc.simpleclans.managers;

import com.electronwill.nightconfig.core.Config;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import org.emil.hnrpmc.ConditionalSetting;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.hooks.placeholder.PlaceholderContext;
import org.emil.hnrpmc.simpleclans.overlay.ClanScoreboard;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Hinweis: Du benötigst eine Library wie SnakeYAML oder eine eigene YAML-Wrapper Klasse,
// da 'FileConfiguration' eine Bukkit-Klasse ist.
// In diesem Beispiel gehen wir davon aus, dass wir einen einfachen Wrapper haben.
import org.emil.hnrpmc.simpleclans.utils.YamlConfig;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;
import static org.emil.hnrpmc.simpleclans.utils.RankingNumberResolver.RankingType;
import static org.openjdk.nashorn.internal.runtime.JSType.remExact;
import static org.openjdk.nashorn.internal.runtime.JSType.toDouble;

public final class SettingsManager {

    private final SimpleClans plugin;
    private final YamlConfig config; // Ersetzt FileConfiguration
    private final File configFile;

    public SettingsManager(SimpleClans plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getGameDirectory().toFile(), "config/simpleclans/config.json");
        this.config = new YamlConfig(configFile);
        this.config.load();
        this.load();

        loadAndSave();
        // checkDependencies(); // In Forge prüfen wir Mods statt Plugins
    }

    public <T> void set(ConfigField field, T value) {
        config.set(field.path, value);
    }

    public void load() {
        // 1. Zuerst die echten Daten von der Festplatte laden
        config.load();

        // 2. Jetzt prüfen, ob neue Felder dazugekommen sind (Defaults setzen)
        boolean changed = false;
        for (ConfigField field : ConfigField.values()) {
            if (config.get(field.getPath()) == null) {
                config.set(field.getPath(), field.getDefaultValue());
                plugin.getLogger().debug("changed in conf");
                changed = true;
            }
        }

        // 3. Nur speichern, wenn wir wirklich Defaults hinzugefügt haben
        if (changed) {
            config.save();
        }
    }

    public List<String> getList(ConfigField field) {
        // Hier rufen wir den Wert aus deiner internen Config-Struktur ab
        // Falls du NeoForge 'ModConfig' nutzt:
        Object value = config.get(field.getPath());

        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }

        // Fallback: Wenn das Feld keine Liste ist, geben wir eine leere Liste zurück
        return new ArrayList<>();
    }

    public boolean isUnrivable(String clanTag) {
        if (clanTag == null || clanTag.isBlank()) return false;

        String clean = Helper.cleanTag(clanTag);

        for (String entry : getStringList(UNRIVABLE_CLANS)) {
            if (entry == null || entry.isBlank()) continue;
            if (Helper.cleanTag(entry).equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }

    public double getPercent(ConfigField field) {
        double value = getDouble(field);
        return (value >= 0 && value <= 100)
                ? value
                : toDouble((Double) field.getDefaultValue());
    }

    public boolean isBanned(UUID uuid) {
        return false;
    }


    public int getInt(ConfigField field) {
        Object val = config.get(field.path);
        if (val instanceof Number n) return n.intValue();
        return (field.defaultValue instanceof Integer i) ? i : 0;
    }

    public double getDouble(ConfigField field) {
        Object val = config.get(field.path);
        if (val instanceof Number n) return n.doubleValue();
        return (field.defaultValue instanceof Double d) ? d : 0.0;
    }

    public List<String> getStringList(ConfigField field) {
        return config.getStringList(field.path);
    }

    public String getString(ConfigField field) {
        return config.getString(field.path, String.valueOf(field.defaultValue));
    }

    public String getColored(ConfigField field) {
        String value = getString(field);
        // Nutzt in NeoForge meist 'Component' Formatierung, aber wir behalten ChatUtils bei
        return (value.length() == 1) ? ChatUtils.getColorByChar(value.charAt(0)) : ChatUtils.parseColors(value);
    }

    public int getMinutes(ConfigField field) {
        int value = getInt(field);
        return (value >= 1) ? value * 20 * 60 : getIntFromDefault(field) * 20 * 60;
    }

    public int getSeconds(ConfigField field) {
        int value = getInt(field);
        return (value >= 1) ? value * 20 : getIntFromDefault(field) * 20;
    }

    public Locale getLanguage() {
        String language = getString(LANGUAGE);
        String mylang = "de_DE";
        String[] split = mylang.split("_");

        if (split.length == 2) {
            return new Locale(split[0], split[1]);
        }

        return new Locale(language);
    }


    /**
     * Gibt das interne Config-Objekt zurück.
     */
    public YamlConfig getConfig() {
        return config;
    }

    /**
     * Prüft, ob ein Wort auf der Blacklist steht.
     */
    public boolean isDisallowedWord(String word) {
        for (String disallowedTag : getStringList(DISALLOWED_TAGS)) {
            if (disallowedTag.equalsIgnoreCase(word)) {
                return true;
            }
        }

        return word.equalsIgnoreCase(getString(COMMANDS_CLAN)) ||
                word.equalsIgnoreCase(getString(COMMANDS_MORE)) ||
                word.equalsIgnoreCase(getString(COMMANDS_DENY)) ||
                word.equalsIgnoreCase(getString(COMMANDS_ACCEPT));
    }

    /**
     * Gibt den Ranking-Typ zurück (Standard: DENSE).
     */
    @NotNull
    public RankingType getRankingType() {
        try {
            return RankingType.valueOf(getString(RANKING_TYPE).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return RankingType.DENSE;
        }
    }

    private int getIntFromDefault(ConfigField field) {
        return (field.defaultValue instanceof Integer i) ? i : 0;
    }

    public boolean is(ConfigField field) {
        return config.getBoolean(field.path, (Boolean) field.defaultValue);
    }

    private Map<String, Object> datas;
    public void loadAndSave() {
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
            }
            config.load();
            loadSettingsFromConfig();
        } catch (Exception ex) {
            plugin.getLogger().info("Could not load config!");
        }
        save();
    }

    public void save() {
        config.save();
    }

    public String parseConditionalMessage(ServerPlayer player, String settingKey) {
        loadSettingsFromConfig();

        Pattern pattern = Pattern.compile("%hnph_\\s*(.*?)\\s*%", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(settingKey);

        String output = settingKey;

        while (m.find()) {
            String fullMatch = m.group(0);
            String key = m.group(1).toLowerCase().trim();

            ConditionalSetting setting = loadedSettings.get(key);

            if (setting != null) {
                List<String> rulesFromConfig = setting.getRules();
                if (rulesFromConfig.isEmpty()) continue;

                String placeholderValue = ClanScoreboard.formatplaceholder(plugin, setting.getPlaceholder(), player);

                String cleanValue;
                try {
                    cleanValue = ChatUtils.parseColors(placeholderValue).trim();
                    //value = Double.parseDouble(cleanValue);
                } catch (NumberFormatException e) {
                    output = output.replace(fullMatch, rulesFromConfig.get(rulesFromConfig.size() - 1));
                    continue;
                }

                String resultMessage = rulesFromConfig.get(rulesFromConfig.size() - 1);

                resultMessage = ClanScoreboard.formatplaceholder(plugin, getFormattedName(cleanValue, setting.getRules()), player);
                /*for (String rawRule : rulesFromConfig) {
                    ConditionRule rule = new ConditionRule(rawRule);
                    if (rule.matches(value)) {
                        resultMessage = rule.getMessage();
                        break;
                    }
                }*/

                output = output.replace(fullMatch, resultMessage);
            }
        }

        return output;
    }


    static ScriptEngineManager manager = new ScriptEngineManager();

    public static <T> String getFormattedName(T value, List<String> rules) {
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        for (String rule : rules) {
            if (!rule.contains(";")) continue;

            String[] parts = rule.split(";");
            String condition = parts[0].replace("#>", ";");
            if (!condition.contains("val")){
                condition = "val" + condition;
            }
            String resultText = parts[1];

            try {
                engine.put("val", value);

                Object result = engine.eval(condition);

                if (result instanceof Boolean && (Boolean) result) {
                    return resultText;
                }
            } catch (ScriptException e) {
            }
        }

        // Standard-Rückgabe (hallo)
        return rules.stream()
                .filter(rule -> !rule.contains(";"))
                .findFirst()
                .orElse("");
    }


    public List<String> getStarterRankIds() {
        List<String> list = config.getStringList("ranks.starter");
        if (list == null || list.isEmpty()) {
            String single = config.getString("ranks.starter", "");
            if (single != null && !single.isBlank()) {
                list = List.of(single);
            } else {
                return List.of();
            }
        }

        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String s : list) {
            if (s == null) continue;
            String clean = s.trim();
            if (!clean.isEmpty()) out.add(clean);
        }
        return new ArrayList<>(out);
    }

    /**
     * Gibt die Starter-Ränge als Rank-Objekte zurück.
     * Voraussetzung: Du hast irgendwo eine Rank-Auflösung (z.B. ClanManager/Storage/RankRegistry).
     *
     * Falls du (noch) keine zentrale Rank-Quelle hast, nutze erstmal getStarterRankIds()
     * und mappe später an der Stelle, wo du einem Clan/einem Member Ränge zuweist.
     */
    public @NotNull Collection<Rank> getStarterRanks() {
        List<Rank> ranks = new ArrayList<>();
        List<Map<?, ?>> mapList = config.getMapList("clan.starter-ranks");
        for (Map<?, ?> rankMap : mapList) {
            Set<String> names = (Set<String>) rankMap.keySet();
            for (String name : names) {
                Map<String, Object> rank = (Map<String, Object>) rankMap.get(name);
                String displayName = (String) rank.get("display-name");
                List<String> permissions = (List<String>) rank.get("permissions");
                ranks.add(new Rank(name, displayName, new HashSet<>(permissions)));
            }
        }
        return ranks;
    }

    /**
     * Rank-Auflösung – HIER musst du je nach deinem Datenmodell anpassen.
     *
     * Optionen:
     * - Wenn Ranks global in Settings stehen: aus config lesen und Rank bauen.
     * - Wenn Ranks pro Clan gespeichert werden: hier NULL lassen und beim Clan-Kontext auflösen.
     * - Wenn du eine Registry im ClanManager hast: clanManager.getRankById(id)
     */
    private Rank resolveRank(String id) {
        // Minimaler, sicherer Default: kein globales Rank-Repository -> null
        // Dann nutzt du getStarterRankIds() an der Stelle, wo du Clan-spezifische Ranks kennst.
        return null;
    }


    /**
     * In NeoForge arbeiten wir mit Item-Registries statt Bukkit-Materials.
     */
    public List<Item> getItemList() {
        List<Item> itemsList = new ArrayList<>();
        for (String materialName : getStringList(ITEM_LIST)) {
            ResourceLocation rl = ResourceLocation.tryParse(materialName.toLowerCase());
            if (rl != null) {
                Item item = BuiltInRegistries.ITEM.get(rl);
                if (item != Items.AIR) {
                    itemsList.add(item);
                    continue;
                }
            }
            plugin.getLogger().warn("Error with Material (Item): " + materialName);
        }
        return itemsList;
    }

    public List<Map<?,?>> getMapList(ConfigField field) {
        Object val = config.get(field.path);
        if (val instanceof List) return (List<Map<?,?>>) val;
        return new ArrayList<>();
    }

    public Map<?,?> getMap(ConfigField field) {
        Object val = config.get(field.path);
        if (val instanceof Map) return (Map<?,?>) val;
        return new HashMap<>();
    }

    public List<?> getListItems(ConfigField configField) {
        Object value = config.get(configField.getPath());

        // Prüfen, ob der Wert existiert und wirklich eine Liste ist
        if (value instanceof List<?> list) {
            return list;
        }

        // Falls es keine Liste ist, aber ein Default-Wert im Enum existiert
        if (configField.getDefaultValue() instanceof List<?> defaultList) {
            return defaultList;
        }

        // Letzter Ausweg: Eine leere Liste zurückgeben, damit nichts abstürzt
        return new ArrayList<>();
    }

    public boolean hasDisallowedColor(String str) {
        String loweredString = str.toLowerCase();
        return getStringList(DISALLOWED_TAG_COLORS).stream().map(String::toLowerCase).anyMatch(color -> loweredString.contains("&" + color));
    }

    /**
     * @return a comma delimited string with all disallowed colors
     */
    public String getDisallowedColorString() {
        return String.join(", ", getStringList(DISALLOWED_TAG_COLORS));
    }

    public void addBanned(UUID uuid) {
        return;
    }

    public Object getWorldBlacklist() {
        return null;
    }



    private static final List<Rank> DEFAULT_RANKS = createDefaultRanks();

    private static List<Rank> createDefaultRanks() {
        Set<RankPermission> perms = Set.of(
                RankPermission.BANK_DEPOSIT,
                RankPermission.COORDS,
                RankPermission.BANK_WITHDRAW,
                RankPermission.RANK_LIST,
                RankPermission.HOME_TP
        );

        Set<String> stringPerms = new HashSet<>();
        for (RankPermission perm : perms) {
            stringPerms.add(perm.toString());
        }

        // WICHTIG: Erstelle hier die Liste mit dem Rank-Objekt
        List<Rank> defaultRanks = new ArrayList<>();
        defaultRanks.add(new Rank("mitglied", "Mitglied", stringPerms));
        return defaultRanks;
    }

    private static Map<String, Object> createDefaultStarterRanks() {
        Map<String, Object> ranks = new LinkedHashMap<>();
        Map<String, Object> mitglied = new LinkedHashMap<>();

        mitglied.put("display-name", "&7Mitglied");
        mitglied.put("permissions", List.of(
                "bank.deposit",
                "coords",
                "bank.withdraw",
                "home.tp",
                "rank.list"
        ));

        ranks.put("mitglied", mitglied);
        return ranks;
    }

    private static List<ConditionalSetting> DefaultCS() {
        List<ConditionalSetting> settingg = new ArrayList<>();

        List<String> cs1Rules = List.of("<3;§cDu stirbst gleich!", "<6;§eAlles gut, du Heulsuse!", "§aKerngesund!");
        ConditionalSetting cs1 = new ConditionalSetting("player_health", "%player_health%", cs1Rules);

        List<String> cs2Rules = List.of("<= 50; §a▂▃▅▆█", "<= 120; §e▂▃▅▆█", "§c▂▃▅▆█");
        ConditionalSetting cs2 = new ConditionalSetting("player_ping", "%player_ping%", cs2Rules);
        settingg.add(cs1);
        settingg.add(cs2);

        return settingg;
    }

    public Map<String, ConditionalSetting> loadedSettings = new HashMap<>();

    public void loadSettingsFromConfig() {
        Object rawList = config.get(CONDITONAL_RULES.getPath());

        if (rawList instanceof List<?> list) {
            loadedSettings.clear();
            for (Object obj : list) {
                if (obj instanceof Map<?, ?> map) {
                    String name = String.valueOf(map.get("name"));
                    String placeholder = String.valueOf(map.get("placeholder"));

                    List<String> rules = new ArrayList<>();
                    if (map != null && map.get("rules") instanceof List<?> ruleList) {
                        rules = ruleList.stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .toList();
                    } else {
                        rules = new ArrayList<>();
                    }

                    loadedSettings.put(name, new ConditionalSetting(name, placeholder, rules));
                }
                else if (obj instanceof ConditionalSetting setting) {
                    loadedSettings.put(setting.getName(), setting);
                }
            }
        }
    }

    public void sendStatus(ServerPlayer player, String settingKey) {
        ConditionalSetting setting = loadedSettings.get(settingKey);
        if (setting == null) return;

        ClanScoreboard.formatplaceholder(plugin, setting.getPlaceholder(), player);
        PlaceholderContext plc = new PlaceholderContext(plugin, player);
        String currentVal = plugin.getPlaceholderService().apply(plc, setting.getPlaceholder());

        String message = parseConditionalMessage(player, currentVal);

        String finalText = "§7[" + setting.getName() + "§7] " + message.replace("&", "§");
        player.sendSystemMessage(Component.literal(finalText));
    }

    public enum ConfigField {
        /*
        ================
        > General Settings
        ================
         *
         */
        ENABLE_GUI("settings.enable-gui", true),
        DISABLE_MESSAGES("settings.disable-messages", false),
        TAMABLE_MOBS_SHARING("settings.tameable-mobs-sharing", false),
        TELEPORT_BLOCKS("settings.teleport-blocks", false),
        TELEPORT_HOME_ON_SPAWN("settings.teleport-home-on-spawn", false),
        DROP_ITEMS_ON_CLAN_HOME("settings.drop-items-on-clan-home", false),
        KEEP_ITEMS_ON_CLAN_HOME("settings.keep-items-on-clan-home", false),
        ITEM_LIST("settings.item-list"),
        DEBUG("settings.show-debug-info", false),
        ENABLE_AUTO_GROUPS("settings.enable-auto-groups", false),
        CHAT_COMPATIBILITY_MODE("settings.chat-compatibility-mode", true),
        RIVAL_LIMIT_PERCENT("settings.rival-limit-percent", 50),
        COLOR_CODE_FROM_PREFIX_FOR_NAME("settings.use-colorcode-from-prefix-for-name", true),
        DISPLAY_CHAT_TAGS("settings.display-chat-tags", true),
        GLOBAL_FRIENDLY_FIRE("settings.global-friendly-fire", false),
        UNRIVABLE_CLANS("settings.unrivable-clans"),
        SHOW_UNVERIFIED_ON_LIST("settings.show-unverified-on-list", false),
        BLACKLISTED_WORLDS("settings.blacklisted-worlds"),
        BANNED_PLAYERS("settings.banned-players"),
        DISALLOWED_TAGS("settings.disallowed-tags"),
        LANGUAGE("settings.language", "en"),
        LANGUAGE_SELECTOR("settings.user-language-selector", true),
        DISALLOWED_TAG_COLORS("settings.disallowed-tag-colors"),
        SERVER_NAME("settings.server-name", "&4SimpleClans"),
        REQUIRE_VERIFICATION("settings.new-clan-verification-required", true),
        ALLOW_REGROUP("settings.allow-regroup-command", true),
        ALLOW_RESET_KDR("settings.allow-reset-kdr", false),
        REJOIN_COOLDOWN("settings.rejoin-cooldown", 60),
        ENABLE_REJOIN_COOLDOWN("settings.rejoin-cooldown-enabled", false),
        RANKING_TYPE("settings.ranking-type", "DENSE"),
        LIST_DEFAULT_ORDER_BY("settings.list-default-order-by", "kdr"),
        LORE_LENGTH("settings.lore-length", 36),
        PVP_ONLY_WHILE_IN_WAR("settings.pvp-only-while-at-war", false),
        PAST_CLANS_LIMIT("settings.past-clans-limit", 10),
        USERNAME_REGEX("settings.username-regex", "^\\**[a-zA-Z0-9_$]{1,16}$"),
        TAG_REGEX("settings.tag-regex", ""),
        ACCEPT_OTHER_ALPHABETS_LETTERS("settings.accept-other-alphabets-letters-on-tag", false),
        DATE_TIME_PATTERN("settings.date-time-pattern", "HH:mm - dd/MM/yyyy"),
        BUNGEE_SERVERS("settings.bungee-servers"),
        /*
        ================
        > Tag Settings
        ================
         *
         */
        TAG_DEFAULT_COLOR("tag.default-color", "8"),
        TAG_BRACKET_COLOR("tag.bracket.color", "8"),
        TAG_BRACKET_LEADER_COLOR("tag.bracket.leader-color", "4"),
        TAG_BRACKET_LEFT("tag.bracket.left", ""),
        TAG_MAX_LENGTH("tag.max-length", 5),
        TAG_MIN_LENGTH("tag.min-length", 2),
        TAG_BRACKET_RIGHT("tag.bracket.right", ""),
        TAG_SEPARATOR_COLOR("tag.separator.color", "8"),
        TAG_SEPARATOR_LEADER_COLOR("tag.separator.leader-color", "4"),
        TAG_SEPARATOR_CHAR("tag.separator.char", " ."),
        @Deprecated
        TAG_SEPARATOR_char("tag.separator.char", " ."),
        /*
        ================
        > War and Protection Settings
        ================
         *
         */
        ENABLE_WAR("war-and-protection.war-enabled", false),
        LAND_SHARING("war-and-protection.land-sharing", true),
        LAND_PROTECTION_PROVIDERS("war-and-protection.protection-providers"),
        WAR_LISTENERS_PRIORITY("war-and-protection.listeners.priority", "HIGHEST"),
        WAR_LISTENERS_IGNORED_LIST_PLACE("war-and-protection.listeners.ignored-list.PLACE"),
        WAR_LISTENERS_IGNORED_LIST_BREAK("war-and-protection.listeners.ignored-list.BREAK"),
        LAND_SET_BASE_ONLY_IN_LAND("war-and-protection.set-base-only-in-land", false),
        WAR_NORMAL_EXPIRATION_TIME("war-and-protection.war-normal-expiration-time", 0),
        WAR_DISCONNECT_EXPIRATION_TIME("war-and-protection.war-disconnect-expiration-time", 0),
        LAND_EDIT_ALL_LANDS("war-and-protection.edit-all-lands", false),
        LAND_CREATION_ONLY_LEADERS("war-and-protection.land-creation.only-leaders", false),
        LAND_CREATION_ONLY_ONE_PER_CLAN("war-and-protection.land-creation.only-one-per-clan", false),
        WAR_ACTIONS_CONTAINER("war-and-protection.war-actions.CONTAINER", true),
        WAR_ACTIONS_INTERACT("war-and-protection.war-actions.INTERACT", true),
        WAR_ACTIONS_BREAK("war-and-protection.war-actions.BREAK", true),
        WAR_ACTIONS_PLACE("war-and-protection.war-actions.PLACE", true),
        WAR_ACTIONS_DAMAGE("war-and-protection.war-actions.DAMAGE", true),
        WAR_ACTIONS_INTERACT_ENTITY("war-and-protection.war-actions.INTERACT_ENTITY", true),
        WAR_START_REQUEST_ENABLED("war-and-protection.war-start.request-enabled", true),
        WAR_MAX_MEMBERS_DIFFERENCE("war-and-protection.war-start.members-online-max-difference", 5),
        /*
        ================
        > KDR Grinding Prevention Settings
        ================
         *
         */
        KDR_ENABLE_MAX_KILLS("kdr-grinding-prevention.enable-max-kills", false),
        KDR_MAX_KILLS_PER_VICTIM("kdr-grinding-prevention.max-kills-per-victim", 10),
        KDR_ENABLE_KILL_DELAY("kdr-grinding-prevention.enable-kill-delay", false),
        KDR_DELAY_BETWEEN_KILLS("kdr-grinding-prevention.delay-between-kills", 5),
        /*
        ================
        > Commands Settings
        ================
         *
         */
        COMMANDS_MORE("commands.more", "more"),
        COMMANDS_ALLY("commands.ally", "ally"),
        COMMANDS_CLAN("commands.clan", "clan"),
        COMMANDS_ACCEPT("commands.accept", "accept"),
        COMMANDS_DENY("commands.deny", "deny"),
        COMMANDS_GLOBAL("commands.global", "global"),
        COMMANDS_CLAN_CHAT("commands.clan_chat", "."),
        COMMANDS_FORCE_PRIORITY("commands.force-priority", true),
        /*
        ================
        > Economy Settings
        ================
         *
         */
        ECONOMY_CREATION_PRICE("economy.creation-price", 100.0),
        ECONOMY_PURCHASE_CLAN_CREATE("economy.purchase-clan-create", false),
        ECONOMY_VERIFICATION_PRICE("economy.verification-price", 1000.0),
        ECONOMY_PURCHASE_CLAN_VERIFY("economy.purchase-clan-verify", false),
        ECONOMY_INVITE_PRICE("economy.invite-price", 20),
        ECONOMY_PURCHASE_CLAN_INVITE("economy.purchase-clan-invite", false),
        ECONOMY_HOME_TELEPORT_PRICE("economy.home-teleport-price", 5.0),
        ECONOMY_PURCHASE_HOME_TELEPORT("economy.purchase-home-teleport", false),
        ECONOMY_HOME_TELEPORT_SET_PRICE("economy.home-teleport-set-price", 5.0),
        ECONOMY_PURCHASE_HOME_TELEPORT_SET("economy.purchase-home-teleport-set", false),
        ECONOMY_REGROUP_PRICE("economy.home-regroup-price", 5.0),
        ECONOMY_PURCHASE_HOME_REGROUP("economy.purchase-home-regroup", false),
        ECONOMY_PURCHASE_DISCORD_CREATE("economy.purchase-discord-create", false),
        ECONOMY_DISCORD_CREATION_PRICE("economy.discord-creation-price", 1000.0),
        ECONOMY_UNIQUE_TAX_ON_REGROUP("economy.unique-tax-on-regroup", true),
        ECONOMY_ISSUER_PAYS_REGROUP("economy.issuer-pays-regroup", true),
        ECONOMY_MONEY_PER_KILL("economy.money-per-kill", false),
        ECONOMY_MONEY_PER_KILL_KDR_MULTIPLIER("economy.money-per-kill-kdr-multipier", 10),
        ECONOMY_RESET_KDR_PRICE("economy.reset-kdr-price", 10000.0),
        ECONOMY_PURCHASE_RESET_KDR("economy.purchase-reset-kdr", true),
        ECONOMY_PURCHASE_MEMBER_FEE_SET("economy.purchase-member-fee-set", false),
        ECONOMY_MEMBER_FEE_SET_PRICE("economy.member-fee-set-price", 1000.0),
        ECONOMY_MEMBER_FEE_ENABLED("economy.member-fee-enabled", false),
        ECONOMY_MEMBER_FEE_LAST_MINUTE_CHANGE_INTERVAL("economy.member-fee-last-minute-change-interval", 8),
        ECONOMY_MAX_MEMBER_FEE("economy.max-member-fee", 200.0),
        ECONOMY_UPKEEP("economy.upkeep", false),
        ECONOMY_UPKEEP_ENABLED("economy.upkeep-enabled", false),
        ECONOMY_MULTIPLY_UPKEEP_BY_CLAN_SIZE("economy.multiply-upkeep-by-clan-size", false),
        ECONOMY_UPKEEP_REQUIRES_MEMBER_FEE("economy.charge-upkeep-only-if-member-fee-enabled", true),
        ECONOMY_BANK_LOG_ENABLED("economy.bank-log.enable", true),
        /*
        ================
        > Kill Weights Settings
        ================
         *
         */
        KILL_WEIGHTS_RIVAL("kill-weights.rival", 2.0),
        KILL_WEIGHTS_CIVILIAN("kill-weights.civilian", 0.0),
        KILL_WEIGHTS_NEUTRAL("kill-weights.neutral", 1.0),
        KILL_WEIGHTS_ALLY("kill-weights.ally", -1.0),
        KILL_WEIGHTS_DENY_SAME_IP_KILLS("kill-weights.deny-same-ip-kills", false),
        /*
        ================
        > Clan Settings
        ================
         *
         */

        CLAN_TELEPORT_DELAY("clan.homebase-teleport-wait-secs", 10),
        CLAN_HOMEBASE_CAN_BE_SET_ONLY_ONCE("clan.homebase-can-be-set-only-once", true),
        CLAN_MIN_SIZE_TO_SET_RIVAL("clan.min-size-to-set-rival", 3),
        CLAN_MIN_SIZE_TO_SET_ALLY("clan.min-size-to-set-ally", 3),
        CLAN_MAX_LENGTH("clan.max-length", 25),
        CLAN_MIN_LENGTH("clan.min-length", 2),
        CLAN_MAX_DESCRIPTION_LENGTH("clan.max-description-length", 120),
        CLAN_MIN_DESCRIPTION_LENGTH("clan.min-description-length", 10),
        CLAN_MAX_MEMBERS("clan.max-members", 25),
        CLAN_UNVERIFIED_MAX_MEMBERS("clan.unverified-max-members", 10),
        CLAN_MAX_ALLIANCES("clan.max-alliances", -1),
        CLAN_CONFIRMATION_FOR_PROMOTE("clan.confirmation-for-promote", false),
        CLAN_TRUST_MEMBERS_BY_DEFAULT("clan.trust-members-by-default", false),
        CLAN_CONFIRMATION_FOR_DEMOTE("clan.confirmation-for-demote", false),
        CLAN_PERCENTAGE_ONLINE_TO_DEMOTE("clan.percentage-online-to-demote", 100.0),
        CLAN_FF_ON_BY_DEFAULT("clan.ff-on-by-default", false),
        CLAN_MIN_TO_VERIFY("clan.min-to-verify", 3),
        CLAN_STATER_RANK("clan.stater-rank", DEFAULT_RANKS),
        CLAN_DEFAULT_RANK("clan.default-rank", "mitglied"),
        /*
        ================
        > Tasks Settings
        ================
         *
         */
        TASKS_COLLECT_UPKEEP_HOUR("tasks.collect-upkeep.hour", 1),
        TASKS_COLLECT_UPKEEP_MINUTE("tasks.collect-upkeep.minute", 30),
        TASKS_COLLECT_UPKEEP_WARNING_HOUR("tasks.collect-upkeep-warning.hour", 12),
        TASKS_COLLECT_UPKEEP_WARNING_MINUTE("tasks.collect-upkeep-warning.minute", 0),
        TASKS_COLLECT_FEE_HOUR("tasks.collect-fee.hour", 1),
        TASKS_COLLECT_FEE_MINUTE("tasks.collect-fee.minute", 0),
        /*
        ================
        > Page Settings
        ================
         */
        PAGE_LEADER_COLOR("page.leader-color", "4"),
        PAGE_UNTRUSTED_COLOR("page.untrusted-color", "8"),
        PAGE_TRUSTED_COLOR("page.trusted-color", "f"),
        PAGE_CLAN_NAME_COLOR("page.clan-name-color", "b"),
        PAGE_SUBTITLE_COLOR("page.subtitle-color", "7"),
        PAGE_HEADINGS_COLOR("page.headings-color", "8"),
        PAGE_SEPARATOR("page.separator", "-"),
        PAGE_SIZE("page.size", 100),
        HELP_SIZE("page.help-size", 10),
        /*
        ================
        > Clan Chat Settings
        ================
         *
         */
        CLANCHAT_ENABLE("clanchat.enable", true),
        CLANCHAT_TAG_BASED("clanchat.tag-based-clan-chat", false),
        CLANCHAT_ANNOUNCEMENT_COLOR("clanchat.announcement-color", "e"),
        CLANCHAT_FORMAT("clanchat.format", "&b[%clan%&b] &4<%nick-color%%player%&4> %rank%: &b%message%"),
        CLANCHAT_SPYFORMAT("clanchat.spy-format", "&8[Spy] [&bC&8] <%clan%&8> <%nick-color%*&8%player%>&8 %rank%: %message%"),
        CLANCHAT_RANK("clanchat.rank", "&f[%rank%&f]"),
        CLANCHAT_LEADER_COLOR("clanchat.leader-color", "4"),
        CLANCHAT_TRUSTED_COLOR("clanchat.trusted-color", "f"),
        CLANCHAT_MEMBER_COLOR("clanchat.member-color", "7"),
        CLANCHAT_BRACKET_COLOR("clanchat.tag-bracket.color", "e"),
        CLANCHAT_BRACKET_LEFT("clanchat.tag-bracket.left", ""),
        CLANCHAT_BRACKET_RIGHT("clanchat.tag-bracket.right", ""),
        CLANCHAT_NAME_COLOR("clanchat.name-color", "e"),
        CLANCHAT_PLAYER_BRACKET_LEFT("clanchat.player-bracket.left", ""),
        CLANCHAT_PLAYER_BRACKET_RIGHT("clanchat.player-bracket.right", ""),
        CLANCHAT_MESSAGE_COLOR("clanchat.message-color", "b"),
        CLANCHAT_LISTENER_PRIORITY("clanchat.listener-priority", "LOW"),
        /*
        ================
        > Request Settings
        ================
         *
         */
        REQUEST_MESSAGE_COLOR("request.message-color", "b"),
        REQUEST_FREQUENCY("request.ask-frequency-secs", 60),
        REQUEST_MAX("request.max-asks-per-request", 1440),
        /*
        ================
        > BB Settings
        ================
         */
        BB_PREFIX("bb.prefix", "&8* &e"),
        BB_SHOW_ON_LOGIN("bb.show-on-login", true),
        BB_SIZE("bb.size", 6),
        BB_LOGIN_SIZE("bb.login-size", 6),
        /*
        ================
        > Ally Chat Settings
        ================
         */
        ALLYCHAT_ENABLE("allychat.enable", true),
        ALLYCHAT_FORMAT("allychat.format", "&b[Ally Chat] &4<%clan%&4> <%nick-color%%player%&4> %rank%: &b%message%"),
        ALLYCHAT_SPYFORMAT("allychat.spy-format", "&8[Spy] [&cA&8] <%clan%&8> <%nick-color%*&8%player%>&8 %rank%: %message%"),
        ALLYCHAT_RANK("allychat.rank", "&f[%rank%&f]"),
        ALLYCHAT_LEADER_COLOR("allychat.leader-color", "4"),
        ALLYCHAT_TRUSTED_COLOR("allychat.trusted-color", "f"),
        ALLYCHAT_MEMBER_COLOR("allychat.member-color", "7"),
        ALLYCHAT_BRACKET_COLOR("allychat.tag-bracket.color", "8"),
        ALLYCHAT_BRACKET_lEFT("allychat.tag-bracket.left", ""),
        ALLYCHAT_BRACKET_RIGHT("allychat.tag-bracket.right", ""),
        ALLYCHAT_PLAYER_BRACKET_LEFT("allychat.player-bracket.left", ""),
        ALLYCHAT_PLAYER_BRACKET_RIGHT("allychat.player-bracket.right", ""),
        ALLYCHAT_MESSAGE_COLOR("allychat.message-color", "3"),
        ALLYCHAT_TAG_COLOR("allychat.tag-color", ""),
        /*
        ================
        > Discord Chat Settings
        ================
         */
        DISCORDCHAT_ENABLE("discordchat.enable", true),
        DISCORDCHAT_AUTO_CREATION("discordchat.auto-creation", false),
        DISCORDCHAT_FORMAT_TO("discordchat.discord-format", "%player% » %message%"),
        DISCORDCHAT_FORMAT("discordchat.format", "&b[&9D&b] &b[%clan%&b] &4<%nick-color%%player%&4> %rank%: &b%message%"),
        DISCORDCHAT_SPYFORMAT("discordchat.spy-format", "&8[Spy] [&9D&8] <%clan%&8> <%nick-color%*&8%player%>&8 %rank%: %message%"),
        DISCORDCHAT_RANK("discordchat.rank", "[%rank%]"),
        DISCORDCHAT_LEADER_ROLE("discordchat.leader-role", "Leader"),
        DISCORDCHAT_LEADER_ID("discordchat.leader-id", "0"),
        DISCORDCHAT_LEADER_COLOR("discordchat.leader-color", "231, 76, 60, 100"),
        DISCORDCHAT_TEXT_CATEGORY_FORMAT("discordchat.text.category-format", "SC - TextChannels"),
        DISCORDCHAT_TEXT_CATEGORY_IDS("discordchat.text.category-ids"),
        DISCORDCHAT_TEXT_WHITELIST("discordchat.text.whitelist"),
        DISCORDCHAT_TEXT_LIMIT("discordchat.text.clans-limit", 100),
        DISCORDCHAT_MINIMUM_LINKED_PLAYERS("discordchat.min-linked-players-to-create", 3),
        DISCORDCHAT_ROLE_LIST("discordchat.role-list", new HashMap<String, String>()),
        DISCORDCHAT_LOWEST_ROLE("discordchat.lowest-role", ""),
        DISCORDCHAT_HIGHEST_ROLE("discordchat.highes-role", ""),
        DISCORDCHAT_CATEGORYS_AND_CHANNELS("discordchat.categorys-and-channels", new HashMap<String, Map<String, List<String>>>()),

        /*
        ================
        > Purge Settings
        ================
         */
        PURGE_INACTIVE_PLAYER_DAYS("purge.inactive-player-data-days", 30),
        PURGE_INACTIVE_CLAN_DAYS("purge.inactive-clan-days", 7),
        PURGE_UNVERIFIED_CLAN_DAYS("purge.unverified-clan-days", 2),
        /*
        ================
        > MySQL Settings
        ================
         */
        MYSQL_USERNAME("mysql.username", ""),
        MYSQL_HOST("mysql.host", "localhost"),
        MYSQL_PORT("mysql.port", 3306),
        MYSQL_ENABLE("mysql.enable", false),
        MYSQL_PASSWORD("mysql.password", ""),
        MYSQL_DATABASE("mysql.database", ""),
        MYSQL_TABLE_PREFIX("mysql.table_prefix", "sc_"),
        /*
        ================
        > Permissions Settings
        ================
         */
        PERMISSIONS_AUTO_GROUP_GROUPNAME("permissions.auto-group-groupname", false),
        PERMISSIONS_YourClanNameHere("permissions.YourClanNameHere", List.of("- test.permission")),
        /*
        ================
        > Performance Settings
        ================
         */
        PERFORMANCE_SAVE_PERIODICALLY("performance.save-periodically", false),
        PERFORMANCE_SAVE_INTERVAL("performance.save-interval", 10),
        PERFORMANCE_USE_THREADS("performance.use-threads", true),
        PERFORMANCE_USE_BUNGEECORD("performance.use-bungeecord", false),
        PERFORMANCE_HEAD_CACHING("performance.cache-player-heads", false),

        SAFE_CIVILIANS("safe-civilians", false),
        SETTINGS_WORLD_BLACKLIST("settings.world-blacklist", java.util.Collections.emptyList()),

        /*
        ================
        > Claim and Homes Settings
        ================
         */
        CONDITONAL_RULES("conditonal-rules", DefaultCS()),
        MAX_HOMES("general.max-homes", "5"),
        MAX_CLAIMS("general.max-claims", "-1"),
        MAX_CLAIMED_BLOCK_PER_CLAIM("general.max-block-per-claim", "5000"),
        MAX_CLAIMED_BLOCKS("general.max-claimed-blocks", "40000");



        private final String path;
        private final Object defaultValue;

        ConfigField(String path, Object defaultValue) {
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public String getPath() {
            return path;
        }

        ConfigField(String path) {
            this.path = path;
            defaultValue = null;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

    }

}