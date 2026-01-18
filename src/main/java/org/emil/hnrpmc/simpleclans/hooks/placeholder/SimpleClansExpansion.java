package org.emil.hnrpmc.simpleclans.hooks.placeholder;

import com.mojang.authlib.GameProfile;
import jdk.jfr.Relational;
import net.minecraft.world.entity.player.Player;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.INFO;
import static javax.swing.UIManager.getString;

public class SimpleClansExpansion implements Relational, Configurable {

    private static final Pattern TOP_CLANS_PATTERN = Pattern.compile("(?<strip>^topclans_(?<position>\\d+)_)clan_");
    private static final Pattern TOP_PLAYERS_PATTERN = Pattern.compile("(?<strip>^topplayers_(?<position>\\d+)_)");
    private static final Map<String, PlaceholderResolver> RESOLVERS = new HashMap<>();
    private List<String> placeholders;
    private final SimpleClans plugin;
    private final ClanManager clanManager;

    public SimpleClansExpansion(SimpleClans plugin) {
        this.plugin = plugin;
        clanManager = plugin.getClanManager();
        registerResolvers();
    }

    public @NotNull String getName() {
        return plugin.toString();
    }

    public @NotNull String getIdentifier() {
        return getName().toLowerCase();
    }

    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    public @NotNull List<String> getPlaceholders() {
        if (placeholders == null) {
            this.placeholders = new ArrayList<>();
            addPlaceholders("simpleclans_", ClanPlayer.class, placeholders);
            addPlaceholders("simpleclans_clan_", Clan.class, placeholders);
        }
        return placeholders;
    }

    public boolean canRegister() {
        return true;
    }

    public Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();

        defaults.put("color.rival", "&c");
        defaults.put("color.ally", "&b");
        defaults.put("color.same_clan", "&a");

        return defaults;
    }

    @Nullable
    public String onPlaceholderRequest(Player player1, Player player2, String params) {
        if (player1 == null || player2 == null) {
            return null;
        }
        if (params.equalsIgnoreCase("color")) {
            ClanPlayer cp1 = clanManager.getClanPlayer(player1);
            if (cp1 == null) {
                return "";
            }
            //noinspection ConstantConditions -- getClanPlayer != null == getClan() != null
            if (cp1.getClan().isMember(player2)) {
                return getString("color.same_clan", null);
            }
            if (cp1.isRival(player2)) {
                return getString("color.rival", null);
            }
            if (cp1.isAlly(player2)) {
                return getString("color.ally", null);
            }
            return "";
        }
        return null;
    }

    public String onRequest(@Nullable GameProfile player, @NotNull String params) {
        ClanPlayer cp = null;
        if (player != null) {
            cp = clanManager.getAnyClanPlayer(player.getId());
        }
        if (cp == null) {
            return "";
        }
        Clan clan = cp.getClan();
        Matcher matcher = TOP_CLANS_PATTERN.matcher(params);
        if (matcher.find()) {
            int position = Integer.parseInt(matcher.group("position"));
            clan = getFromPosition(clanManager.getClans(), position, clanManager::sortClansByKDR);
            params = params.replace(matcher.group("strip"), "");
        }
        matcher = TOP_PLAYERS_PATTERN.matcher(params);
        if (matcher.find()) {
            int position = Integer.parseInt(matcher.group("position"));
            cp = getFromPosition(clanManager.getAllClanPlayers(), position, clanManager::sortClanPlayersByKDR);
            params = params.replace(matcher.group("strip"), "");
        }
        return getValue(player, cp, clan, params);
    }

    @Nullable
    private <T> T getFromPosition(List<T> list, int position, Consumer<List<T>> sort) {
        if (isPositionValid(list, position)) {
            sort.accept(list);
            return list.get(position - 1);
        }
        return null;
    }

    private boolean isPositionValid(@NotNull Collection<?> collection, int position) {
        return position >= 1 && position <= collection.size();
    }

    @NotNull
    private String getValue(@Nullable GameProfile player, @Nullable ClanPlayer cp, @Nullable Clan clan,
                            @NotNull String placeholder) {
        if (placeholder.startsWith("clan_")) {
            placeholder = placeholder.replace("clan_", "");
            return getValue(player, clan, placeholder);
        }
        return getValue(player, cp, placeholder);
    }

    @NotNull
    private String getValue(@Nullable GameProfile player, @Nullable Object object, @NotNull String placeholder) {
        if (object != null) {
            for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
                Placeholder[] annotations = declaredMethod.getAnnotationsByType(Placeholder.class);
                for (Placeholder p : annotations) {
                    if (p.value().equals(placeholder)) {
                        return resolve(player, object, declaredMethod, p.resolver(), placeholder, p.config());
                    }
                }
            }
            plugin.getLogger().warn(String.format("Placeholder %s not found" , placeholder));
        }
        return "";
    }

    private String resolve(GameProfile player, @NotNull Object object, @NotNull Method method,
                           @NotNull String resolverId, @NotNull String placeholder, @NotNull String config) {

        PlaceholderResolver resolver = RESOLVERS.get(resolverId);

        if (resolver != null) {
            // Wir wandeln den Config-String (z.B. "color=red;bold=true") in eine Map um
            Map<String, String> configMap = getConfigMap(config);

            try {
                return resolver.resolve(player, object, placeholder, configMap);
            } catch (Exception e) {
                plugin.getLogger().error("Error in resolver {} for placeholder {}" , resolverId , placeholder , e);
                return "ERROR";
            }
        }

        plugin.getLogger().warn("Resolver {} for {} not found" , resolverId , placeholder);
        return "";
    }

    @NotNull
    private Map<String, String> getConfigMap(@NotNull String config) {
        HashMap<String, String> map = new HashMap<>();
        String[] elements = config.split(",");
        for (String element : elements) {
            String[] keyAndValue = element.split(":");
            map.put(keyAndValue[0], keyAndValue.length > 1 ? keyAndValue[1] : null);
        }
        return map;
    }

    private void registerResolvers() {
        Set<Class<? extends PlaceholderResolver>> resolvers =
                Helper.getSubTypesOf("net.sacredlabyrinth.phaed.simpleclans.hooks.papi.resolvers",
                        PlaceholderResolver.class);
        plugin.getLogger().info(String.format("Registering %d placeholder resolvers...", resolvers.size()));
        for (Class<? extends PlaceholderResolver> r : resolvers) {
            try {
                PlaceholderResolver resolver = r.getConstructor(SimpleClans.class).newInstance(plugin);
                RESOLVERS.put(resolver.toString(), resolver);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e) {
                plugin.getLogger().info( "Error registering placeholder resolver", e);
            }
        }
    }

    private void addPlaceholders(String prefix, Class<?> clazz, List<String> placeholders) {
        for (Method method : clazz.getDeclaredMethods()) {
            Placeholder[] annotations = method.getAnnotationsByType(Placeholder.class);
            for (Placeholder annotation : annotations) {
                placeholders.add("%" + prefix + annotation.value() + "%");
                //Commented because the list would be very long
                //placeholders.add("%simpleclans_topplayers_<number>_" + annotation.value() + "%");
            }
        }
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public RequestConfig getConfig() {
        return null;
    }
}
