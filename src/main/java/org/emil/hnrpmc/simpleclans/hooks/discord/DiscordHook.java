package org.emil.hnrpmc.simpleclans.hooks.discord;

import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.sdlink.api.accounts.DiscordUser;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.events.VerificationEvent;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.Permission;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.*;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.Channel;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.concrete.Category;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.exceptions.ErrorResponseException;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.managers.channel.concrete.CategoryManager;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.requests.Response;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.requests.RestAction;
import com.hypherionmc.sdlink.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.simpleclans.hooks.discord.exceptions.ChannelExistsException;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.events.*;
import org.emil.hnrpmc.simpleclans.hooks.discord.exceptions.*;
import org.emil.hnrpmc.simpleclans.managers.ChatManager;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.hypherionmc.sdlink.shaded.dv8tion.jda.api.Permission.*;
import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.CLAN;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.DISCORD;
import static org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook.DiscordAction.ADD;
import static org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook.DiscordAction.REMOVE;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

/**
 * Hooks SimpleClans and Discord, using DiscordSRV.
 * <p>
 * On server' startup:
 * </p>
 * <ul>
 *  <li>Creates categories and channels, respecting discord's limits.</li>
 *  <li>Removes invalid channels, resets permissions and roles.</li>
 * </ul>
 * <p>
 * Manages events:
 * </p>
 * <ul>
 *     <li>Clan creation/deletion</li>
 *     <li>ClanPlayer joining/resigning</li>
 *     <li>Player linking/unlinking</li>
 *     <li>ClanPlayer promoting/demoting</li>
 * </ul>
 * <p>
 * Currently, works with clan chat only.
 */
public final class DiscordHook {

    private static final int MAX_CHANNELS_PER_CATEGORY = 50;
    private static final int MAX_CHANNELS_PER_GUILD = 500;
    private final SimpleClans plugin;
    private final SettingsManager settingsManager;
    private final ChatManager chatManager;
    private final ClanManager clanManager;
    private final List<String> textCategories;
    private final List<String> clanTags;
    private final List<String> clanNames;
    private final List<String> whitelist;
    private CacheManager cacheManager;
    private DatabaseManager databaseManager;

    public DiscordHook(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
        settingsManager = plugin.getSettingsManager();
        chatManager = plugin.getChatManager();
        clanManager = plugin.getClanManager();

        textCategories = settingsManager.getStringList(DISCORDCHAT_TEXT_CATEGORY_IDS).stream().
                filter(this::categoryExists).collect(Collectors.toList());
        whitelist = settingsManager.getStringList(DISCORDCHAT_TEXT_WHITELIST);

        clanTags = clanManager.getClans().stream().map(Clan::getTag).collect(Collectors.toList());
        clanNames = clanManager.getClans().stream().map(Clan::getStringName).collect(Collectors.toList());

        this.databaseManager = DatabaseManager.INSTANCE;
        setupDiscord();
    }

    @CraterEventListener(priority = 100)
    public void onMessageReceived(MessageReceivedEvent event) {
        Optional<TextChannel> channel = getCachedChannel(event.getChannel().getName());

        if (channel.isPresent()) {
            Message eventMessage = event.getMessage();
            Member Author = event.getMember();
            TextChannel textChannel = channel.get();
            UUID uuid = MinecraftAccount.fromDiscordId(Author.getId()).getUuid();


            if (uuid == null) {
                sendPrivateMessage(textChannel, eventMessage, lang("you.did.not.link.your.account"));
                return;
            }

            ClanPlayer clanPlayer = clanManager.getClanPlayer(uuid);
            if (clanPlayer == null) {
                return;
            }

            Clan clan = clanPlayer.getClan();
            if (clan == null) {
                return;
            }

            if (clanPlayer.toPlayer() == null) {
                return;
            }

            if (!Objects.equals(textChannel.getName(), clan.getTag())) {
                String channelLink = "<#" + textChannel.getId() + ">";
                sendPrivateMessage(textChannel, eventMessage, lang("cannot.send.discord.message", clanPlayer.toPlayer(), clanPlayer, channelLink));
                return;
            }
            // DiscordSRV start

            shadow.kyori.adventure.text.Component component = SDLinkChatUtils.format(eventMessage.getContentRaw());
            String message = SDLinkChatUtils.parse(String.valueOf(component));

            // DiscordSRV end
            chatManager.processChat(DISCORD, CLAN, clanPlayer, message);
        }
    }

    @SubscribeEvent
    public void onClanDisband(DisbandClanEvent event) {
        plugin.getLogger().debug("clan {} wird gelöscht", event.getClan().getStringName());
        deleteChannelsandRoles(event.getClan().getTag());
    }

    @SubscribeEvent
    public void onClanCreate(CreateClanEvent event) {
        try {
            if (settingsManager.is(DISCORDCHAT_AUTO_CREATION)) {
                createChannel(event.getClan().getTag());
            }
        } catch (DiscordHookException ex) {
            // Clan is not following the conditions, categories are fulled or discord reaches the limit, nothing to do here.
            SimpleClans.debug(ex.getMessage());
        }
    }

    @SubscribeEvent
    public void onPlayerClanLeave(PlayerKickedClanEvent event) {
        ClanPlayer clanPlayer = event.getClanPlayer();
        Clan clan = event.getClan();
        Member member = getMember(clanPlayer);
        if (member == null || clan == null) {
            return;
        }

        updateViewPermission(member, clan, REMOVE);
        updateLeaderRole(member, clanPlayer, REMOVE);
        updateClanRole(clanPlayer.getUniqueId(), clan.getTag(), false);
    }

    @SubscribeEvent
    public void onPlayerClanJoin(PlayerJoinedClanEvent event) {
        ClanPlayer clanPlayer = event.getClanPlayer();
        Clan clan = event.getClan();
        Member member = getMember(clanPlayer);
        if (member == null || clan == null) {
            return;
        }

        if (!createChannelSilently(clanPlayer)) {
            return;
        }

        updateClanRole(clanPlayer.getUniqueId(), clan.getTag(), true);
        updateViewPermission(member, clan, ADD);
    }

    @SubscribeEvent
    public void onPlayerPromote(PlayerPromoteEvent event) {
        ClanPlayer clanPlayer = event.getClanPlayer();
        Member member = getMember(clanPlayer);
        if (member == null) {
            return;
        }

        updateLeaderRole(member, clanPlayer, ADD);
    }

    @SubscribeEvent
    public void onPlayerDemote(PlayerDemoteEvent event) {
        ClanPlayer clanPlayer = event.getClanPlayer();
        Member member = getMember(clanPlayer);
        if (member == null) {
            return;
        }

        updateLeaderRole(member, clanPlayer, REMOVE);
    }

    @CraterEventListener(priority = 100)
    public void onPlayerLinking(VerificationEvent.PlayerVerified event) {
        ClanPlayer clanPlayer = clanManager.getClanPlayer(event.getAccount().getUuid());
        Member member = getGuild().getMemberById(event.getAccount().getDiscordUser().getUserId());
        if (clanPlayer == null || member == null) {
            return;
        }

        Clan clan = clanPlayer.getClan();
        if (clan == null) {
            return;
        }

        if (!createChannelSilently(clanPlayer)) {
            return;
        }

        updateViewPermission(member, clan, ADD);
        updateLeaderRole(member, clanPlayer, ADD);
    }

    @CraterEventListener(priority = 100)
    public void onPlayerUnlinking(VerificationEvent.PlayerUnverified event) {
        ClanPlayer clanPlayer = clanManager.getClanPlayer(event.getAccount().getUuid());
        Member member = getGuild().getMemberById(event.getAccount().getDiscordUser().getUserId());
        if (clanPlayer == null || clanPlayer.getClan() == null || member == null) {
            return;
        }

        updateViewPermission(member, clanPlayer.getClan(), REMOVE);
        updateLeaderRole(member, clanPlayer, REMOVE);
    }

    protected void setupDiscord() {
        Map<String, TextChannel> discordTagChannels = getChannels().stream().
                collect(Collectors.toMap(TextChannel::getName, textChannel -> textChannel));
        Map<String, List<GuildChannel>> discordNameCat = getGuild().getCategories().stream().
                collect(Collectors.toMap(cat -> cat.getId(), cat -> cat.getChannels()));
        SimpleClans.debug("DiscordTagChannels before clearing: " + String.join(",", discordTagChannels.keySet()));

        //clearChannels(discordNameCat);
        SimpleClans.debug("DiscordTagChannels after clearing: " + String.join(",", discordTagChannels.keySet()));

        //resetPermissions(discordTagChannels);

        SimpleClans.debug("ClanTags before creating: " + String.join(",", clanTags));
        createChannels(discordTagChannels, discordNameCat);
        SimpleClans.debug("ClanTags after creating: " + String.join(",", clanTags));
    }

    @NotNull
    public Guild getGuild() {
        return ChannelManager.getConsoleChannel().getGuild();
    }

    /**
     * @return A leader role from guild, otherwise creates one.
     */
    @NotNull
    public Role getLeaderRole() {
        Role role = getGuild().getRoleById(settingsManager.getString(DISCORDCHAT_LEADER_ID));

        if (role == null || !role.getName().equals(settingsManager.getString(DISCORDCHAT_LEADER_ROLE))) {
            role = getGuild().createRole().
                    setName(settingsManager.getString(DISCORDCHAT_LEADER_ROLE)).
                    setColor(getLeaderColor()).
                    setMentionable(true).
                    complete();

            settingsManager.set(DISCORDCHAT_LEADER_ID, role.getId());
            settingsManager.save();
        }

        return role;
    }

    /**
     * @return A leader color from configuration
     */
    public Color getLeaderColor() {
        String[] colors = settingsManager.getString(DISCORDCHAT_LEADER_COLOR).
                replaceAll("\\s", "").split(",");
        try {
            int red = Integer.parseInt(colors[0]);
            int green = Integer.parseInt(colors[1]);
            int blue = Integer.parseInt(colors[2]);
            int alpha = Integer.parseInt(colors[3]);

            return new Color(red, green, blue, alpha);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warn("Color is invalid, using default color: " + ex.getMessage());
            return Color.RED;
        }
    }

    /**
     * Creates a new SimpleClans {@link Category}
     *
     * @return Category or null, if reached the limit
     */
    @Nullable
    public Category createCategory() {
        if (getGuild().getChannels().size() >= MAX_CHANNELS_PER_GUILD) {
            return null;
        }

        String categoryName = settingsManager.getString(DISCORDCHAT_TEXT_CATEGORY_FORMAT);
        Category category = null;
        try {
            category = getGuild().createCategory(categoryName).
                    addRolePermissionOverride(
                            getGuild().getPublicRole().getIdLong(),
                            Collections.emptyList(),
                            Collections.singletonList(VIEW_CHANNEL)).
                    addMemberPermissionOverride(getGuild().getSelfMember().getIdLong(),
                            Arrays.asList(VIEW_CHANNEL, MANAGE_CHANNEL),
                            Collections.emptyList()).
                    submit().get();

            textCategories.add(category.getId());
            settingsManager.set(DISCORDCHAT_TEXT_CATEGORY_IDS, textCategories);
            settingsManager.save();
        } catch (InterruptedException | ExecutionException ex) {
            plugin.getLogger().error("Error while trying to create {0} category: " +
                    ex.getMessage(), categoryName);
        }

        return category;
    }

    public static String convertToFont(String text) {
        if (text == null) return "";

        StringBuilder out = new StringBuilder();

        // Wir iterieren über CodePoints, um auch 32-Bit Zeichen korrekt zu behandeln
        text.codePoints().forEach(cp -> {
            // Kleinbuchstaben a-z (0x61 - 0x7A)
            if (cp >= 0x61 && cp <= 0x7A) {
                out.appendCodePoint(0x1D41A + (cp - 0x61));
            }
            // Großbuchstaben A-Z (0x41 - 0x5A)
            else if (cp >= 0x41 && cp <= 0x5A) {
                out.appendCodePoint(0x1D400 + (cp - 0x41));
            }
            // Alles andere bleibt gleich
            else {
                out.appendCodePoint(cp);
            }
        });

        return out.toString();
    }

    private void clearchannels() {


        List<String> exids = new ArrayList<>();
        exids.add("1343191900408123404");
        exids.add("1424101546421125150");
        exids.add("1406233707588751404");
        exids.add("1343191900408123405");
        exids.add("1343191900860973117");
        exids.add("1343191900860973118");
        exids.add("1343191900408123406");
        exids.add("1411257985484722206");
        exids.add("1343191900408123403");

        for (Channel channel : getGuild().getChannels()) {
            if (!exids.contains(channel.getId()) ) {
                channel.delete().complete();
            }
        }

        for (Category cat : getGuild().getCategories()){
            if (!exids.contains(cat.getId()) ) {
                cat.delete().complete();
            }
        }
    }

    public void createChannel(@NotNull String clanTag)
            throws InvalidChannelException, CategoriesLimitException, ChannelsLimitException, ChannelExistsException {

        validateChannel(clanTag);
        plugin.getLogger().debug("Creating clan channels for: {}", clanTag);

        Clan clan = clanManager.getClan(clanTag);
        if (clan == null) return;

        // 1. Config laden und Master-Map vorbereiten
        settingsManager.load();
        Map<String, Object> masterMap = new HashMap<>();
        Object rawMaster = settingsManager.getMap(DISCORDCHAT_CATEGORYS_AND_CHANNELS);
        if (rawMaster instanceof Map) {
            masterMap.putAll((Map<String, Object>) rawMaster);
        }

        // 2. Prüfen, ob für diesen Clan bereits Kanäle existieren
        if (masterMap.containsKey(clanTag)) {
            throw new ChannelExistsException("Channels already exist for this clan", "discord.channels.exist");
        }

        // 3. Clan-Rolle erstellen
        createClanRole(clan.getStringName());

        try {
            // 4. Kategorie erstellen
            Category cat = getGuild().createCategory(clan.getStringName().toUpperCase())
                    .addRolePermissionOverride(getGuild().getPublicRole().getIdLong(), null, Collections.singletonList(VIEW_CHANNEL))
                    .addMemberPermissionOverride(getGuild().getSelfMember().getIdLong(), Arrays.asList(VIEW_CHANNEL, MANAGE_CHANNEL), null)
                    .complete();

            String catId = cat.getId();

            // 5. Kanäle erstellen
            TextChannel textChannel = cat.createTextChannel("\uD83E\uDDF3》" + convertToFont(clanTag + "-Chat")).complete();
            VoiceChannel voiceChannel = cat.createVoiceChannel("\uD83E\uDDF3》" + convertToFont(clanTag + "-Talk")).complete();

            // 6. Daten für diesen Clan strukturieren
            List<String> channelIds = new ArrayList<>();
            channelIds.add(textChannel.getId());
            channelIds.add(voiceChannel.getId());

            Map<String, List<String>> clanData = new HashMap<>();
            clanData.put(catId, channelIds);

            // 7. In Master-Map einfügen und speichern (überschreibt keine anderen Clans!)
            masterMap.put(clanTag, clanData);
            settingsManager.set(DISCORDCHAT_CATEGORYS_AND_CHANNELS, masterMap);
            settingsManager.save();

            // 8. Berechtigungen für Mitglieder setzen
            Map<ClanPlayer, Member> discordClanPlayers = getDiscordPlayers(clan);
            for (Map.Entry<ClanPlayer, Member> entry : discordClanPlayers.entrySet()) {
                updateViewPermission(entry.getValue(), clan, ADD);
                updateLeaderRole(entry.getValue(), entry.getKey(), ADD);
            }

        } catch (Exception e) {
            plugin.getLogger().error("Failed to create Discord channels for clan " + clanTag, e);
            throw new RuntimeException("Discord API error during channel creation", e);
        }
    }

    public int getClanRank(Clan targetClan) {
        List<Clan> allClans = plugin.getClanManager().getClans();

        List<Clan> sorted = allClans.stream()
                .sorted(Comparator
                        .<Clan>comparingInt(c -> c.getMembers().size())
                        .thenComparing(Clan::getStringName)
                )
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).equals(targetClan)) {
                return i + 6; // Start bei 6
            }
        }
        return -1;
    }

    public void updateClanRole(UUID playerUUID, String clanTag, boolean add) {
        // 1. Suche den Discord-Account des Spielers (SDLink API)
        BridgedGameProfile bgp = BridgedGameProfile.of(plugin.getServer().getProfileCache().get(playerUUID).get());
        Optional<MinecraftAccount> account = Optional.of(MinecraftAccount.of(bgp));

        account.ifPresent(acc -> {
            long discordId = acc.getDiscordUser().getUserId();
            // Hole die Guild (Server) von deinem Bot
            Guild guild = getGuild();
            if (guild == null) return;

            guild.retrieveMemberById(discordId).queue(member -> {
                // Suche die Rolle für den Clan (z.B. Name des Clans oder eine generische Clan-Rolle)
                Role clanRole = guild.getRolesByName(clanTag, true).stream().findFirst().orElse(null);

                if (clanRole != null) {
                    if (add) {
                        guild.addRoleToMember(member, clanRole).queue();
                    } else {
                        guild.removeRoleFromMember(member, clanRole).queue();
                    }
                }
            });
        });
    }

    private void createClanRole(@NotNull String clanName) {
        Clan targetClan = clanManager.getClanByName(clanName);
        if (targetClan == null) return;

        settingsManager.load();
        // Sicherstellen, dass wir eine bearbeitbare Map bekommen
        Map<String, String> allRoles = new HashMap<>();
        Object raw = settingsManager.getMap(DISCORDCHAT_ROLE_LIST);
        if (raw instanceof Map) {
            allRoles.putAll((Map<String, String>) raw);
        }

        // Wenn der Clan schon eine Rolle hat, abbrechen
        if (allRoles.containsKey(targetClan.getTag())) return;

        int finalPosition = getrolepos(targetClan);

        getGuild().createRole()
                .setName(targetClan.getStringName())
                .setHoisted(true)
                .queue(role -> {
                    // 1. Position setzen
                    getGuild().modifyRolePositions().selectPosition(role).moveTo(finalPosition).queue();

                    // 2. In Config speichern (den String der ID nutzen)
                    allRoles.put(targetClan.getTag(), role.getId());
                    settingsManager.set(DISCORDCHAT_ROLE_LIST, allRoles);
                    settingsManager.save();

                    // 3. Mitgliedern zuweisen
                    Map<ClanPlayer, Member> discordClanPlayers = getDiscordPlayers(targetClan);
                    discordClanPlayers.values().forEach(member -> {
                        getGuild().addRoleToMember(member, role).queue();
                    });

                    Object rawMaster = settingsManager.getMap(DISCORDCHAT_CATEGORYS_AND_CHANNELS);

                    plugin.getLogger().debug("deleting roles and channels 1 {}", rawMaster);
                    if (rawMaster instanceof Map<?, ?> masterMap) {
                        plugin.getLogger().debug("deleting roles and channels 2 {}", rawMaster);

                        if (masterMap.containsKey(targetClan.getTag())) {


                            if (masterMap.get(targetClan.getTag()) instanceof Map<?, ?> channelMap) {
                                CategoryManager cat = getGuild().getCategoryById(channelMap.keySet().toString()).getManager();

                                cat.putRolePermissionOverride(role.getIdLong(), Arrays.asList(VIEW_CHANNEL, MESSAGE_SEND, VOICE_SPEAK), null);
                            }
                        }
                    }
                });


    }

    private int getrolepos(Clan targetClan) {
        // 1. Die Grenz-Rollen von der Guild holen
        Role upperRole = getGuild().getRoleById(settingsManager.getString(DISCORDCHAT_LOWEST_ROLE)); // Die obere Grenze
        Role lowerRole = getGuild().getRoleById(settingsManager.getString(DISCORDCHAT_LOWEST_ROLE)); // Die untere Grenze

        if (upperRole == null || lowerRole == null) {
            plugin.getLogger().error("Grenz-Rollen für Discord konnten nicht gefunden werden!");
            return 0;
        }

        int basePosition = lowerRole.getPosition() + 1;

        int finalPosition = basePosition + (getClanRank(targetClan) - 6);

        if (finalPosition >= upperRole.getPosition()) {
            finalPosition = upperRole.getPosition() - 1;
        }

        plugin.getLogger().debug("got pos {} for role von low {} to high {}", finalPosition, lowerRole.getPosition(), upperRole.getPosition());

        return finalPosition;
    }

    private void deleteChannelsandRoles(@NotNull String clanTag) {
        settingsManager.load(); // WICHTIG: Frische Daten von der Platte laden

        // 1. KANÄLE UND KATEGORIEN LÖSCHEN
        Object rawMaster = settingsManager.getMap(DISCORDCHAT_CATEGORYS_AND_CHANNELS);

        plugin.getLogger().debug("deleting roles and channels 1 {}", rawMaster);
        if (rawMaster instanceof Map<?, ?> masterMap) {
            plugin.getLogger().debug("deleting roles and channels 2 {}", rawMaster);
            // Kopie erstellen, damit wir sie bearbeiten können
            Map<String, Object> editableMaster = new HashMap<>((Map<String, Object>) masterMap);

            if (editableMaster.containsKey(clanTag)) {
                plugin.getLogger().debug("deleting roles and channels 3");
                Object clanData = editableMaster.get(clanTag);

                if (clanData instanceof Map<?, ?> channelMap) {
                    plugin.getLogger().debug("deleting roles and channels 4");
                    // Alle Kanäle in der Liste löschen
                    channelMap.values().forEach(val -> {
                        if (val instanceof List<?> ids) {
                            plugin.getLogger().debug("deleting roles and channels 5");
                            for (Object id : ids) {
                                GuildChannel gc = getGuild().getGuildChannelById(id.toString());
                                plugin.getLogger().debug("deleting roles and channels 6");
                                if (gc != null) gc.delete().queue(); // .queue() ist Pflicht!
                            }
                        }
                    });

                    // Kategorie löschen (CategoryID ist der Key der channelMap)
                    if (!channelMap.isEmpty()) {
                        plugin.getLogger().debug("deleting roles and channels 7");
                        String catId = channelMap.keySet().iterator().next().toString();
                        Category cat = getGuild().getCategoryById(catId);
                        if (cat != null) cat.delete().queue();
                    }
                }

                // Aus der Master-Map entfernen
                editableMaster.remove(clanTag);
                plugin.getLogger().debug("deleting roles and channels 8");
                settingsManager.set(DISCORDCHAT_CATEGORYS_AND_CHANNELS, editableMaster);
            }
        }

        // 2. ROLLEN LÖSCHEN
        Object rawRoles = settingsManager.getMap(DISCORDCHAT_ROLE_LIST);
        if (rawRoles instanceof Map<?, ?> roleMap) {
            plugin.getLogger().debug("deleting roles and channels 9");
            Map<String, Object> editableRoles = new HashMap<>((Map<String, Object>) roleMap);

            if (editableRoles.containsKey(clanTag)) {
                plugin.getLogger().debug("deleting roles and channels 10");
                String roleId = editableRoles.get(clanTag).toString();
                Role role = getGuild().getRoleById(roleId);
                if (role != null) {
                    plugin.getLogger().debug("deleting roles and channels 11");
                    role.delete().queue();
                }

                // Aus der Rollen-Map entfernen
                editableRoles.remove(clanTag);
                plugin.getLogger().debug("deleting roles and channels 12");
                settingsManager.set(DISCORDCHAT_ROLE_LIST, editableRoles);
            }
        }

        // 3. SPEICHERN
        settingsManager.save();
        plugin.getLogger().debug("deleting roles and channels 13");
    }

    /**
     * Retrieves channel in SimpleClans categories.
     *
     * @param channelName the channel name
     * @return the channel
     * @see #getCachedCategories() retreive categories.
     */
    public Optional<TextChannel> getCachedChannel(@NotNull String channelName) {
        return getCachedChannels().stream().filter(textChannel -> textChannel.getName().equals(channelName)).findFirst();
    }

    /**
     * Checks if a category can be obtained by id.
     *
     * @param categoryId the category id
     * @return true if the category exists
     * @see #channelExists(String)
     */
    public boolean categoryExists(String categoryId) {
        return getGuild().getCategoryById(categoryId) != null;
    }

    /**
     * Checks if a channel with the specified clan tag exists
     *
     * @see #categoryExists(String)
     */
    public boolean channelExists(String clanTag) {
        return getChannel(clanTag).isPresent();
    }

    /**
     * Retrieves the channel in SimpleClans categories.
     *
     * @param channelName the channel name
     * @return the channel
     * @see #getCachedChannel(String) retreive the <b>cached</b> channel.
     */
    public Optional<TextChannel> getChannel(@NotNull String channelName) {
        return getChannels().stream().filter(textChannel -> textChannel.getName().equals(channelName)).findAny();
    }

    /**
     * Deletes channel from SimpleClans categories.
     * If there are no channels, removes category as well.
     *
     * @param channelName the channel name
     * @return true, if channel was deleted and false if not.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean deleteChannel(@NotNull String channelName) {
        boolean deleted = false;

        if (channelExists(channelName)) {
            for (Category category : getCachedCategories()) {
                if (!category.getTextChannels().isEmpty()) {
                    for (TextChannel textChannel : category.getTextChannels()) {
                        if (textChannel.getName().equals(channelName)) {
                            textChannel.delete().complete();
                            deleted = true;
                            break;
                        }
                    }

                    if (category.getTextChannels().isEmpty()) {
                        textCategories.remove(category.getId());
                        settingsManager.set(DISCORDCHAT_TEXT_CATEGORY_IDS, textCategories);
                        settingsManager.save();
                        category.delete().complete();
                    }

                    return deleted;
                }
            }
        }

        return false;
    }

    /**
     * @return categories from config
     */
    public List<Category> getCachedCategories() {
        return textCategories.stream().
                filter(this::categoryExists).
                map(getGuild()::getCategoryById).
                collect(Collectors.toList());
    }

    /**
     * In most cases, you will use {@link #getCachedCategories()}.
     *
     * @return categories from guild
     */
    public List<Category> getCategories() {
        return getGuild().getCategoriesByName(settingsManager.getString(DISCORDCHAT_TEXT_CATEGORY_FORMAT), false);
    }

    /**
     * In most cases, you will use {@link #getCachedChannels()}.
     *
     * @return all channels from guild
     */
    public List<TextChannel> getChannels() {
        return getCategories().stream().map(Category::getTextChannels).flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    /**
     * @return All channels in categories
     */
    public List<TextChannel> getCachedChannels() {
        return getCachedCategories().stream().map(Category::getTextChannels).flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    @Nullable
    public Member getMember(@NotNull ClanPlayer clanPlayer) {
        if (clanPlayer == null) return null;
        BridgedGameProfile bgp = BridgedGameProfile.of(plugin.getServer().getProfileCache().get(clanPlayer.getUniqueId()).get());
        long discordId = MinecraftAccount.of(bgp).getDiscordUser().getUserId();
        return getGuild().getMemberById(discordId);
    }

    private void clearChannels(Map<String, TextChannel> discordTagChannels) {
        // Removes abandoned channels
        ArrayList<String> clansToDelete = new ArrayList<>(discordTagChannels.keySet());
        clansToDelete.removeAll(clanNames);
        clansToDelete.forEach(clanChannel -> {

            //deleteChannelsandRoles(clanChannel);
            discordTagChannels.remove(clanChannel);
        });

        // Removes invalid channels
        Iterator<String> iterator = discordTagChannels.keySet().iterator();
        while (iterator.hasNext()) {
            String clanChannel = iterator.next();
            try {
                validateChannel(clanChannel);
            } catch (InvalidChannelException ex) {
                SimpleClans.debug(ex.getMessage());
                deleteChannel(clanChannel);
                iterator.remove();
            } catch (ChannelExistsException | ChannelsLimitException ex) {
                SimpleClans.debug(ex.getMessage());
            }
        }
    }

    private void resetPermissions(Map<String, TextChannel> discordClanChannels) {
        for (Map.Entry<String, TextChannel> channelEntry : discordClanChannels.entrySet()) {
            TextChannel channel = channelEntry.getValue();
            Clan clan = clanManager.getClan(channelEntry.getKey());
            Map<ClanPlayer, Member> discordPlayers = getDiscordPlayers(clan);

            for (Member member : discordPlayers.values()) {
                PermissionOverride override = channel.getPermissionOverride(member);
                if (override != null) {
                    override.delete().queue(afterSuccess -> updateViewPermission(member, channel, ADD));
                }
            }
        }
    }

    private void createChannels(Map<String, TextChannel> discordClanChannels, Map<String, List<GuildChannel>> discordNameCat) {
        if (!settingsManager.is(DISCORDCHAT_AUTO_CREATION)) {
            return;
        }

        // Removes already used discord channels from creation
        clanTags.removeAll(discordClanChannels.keySet());
        clanNames.removeAll(discordNameCat.keySet());

        for (String clan : clanTags) {
            try {
                createChannel(clan);
            } catch (CategoriesLimitException | ChannelsLimitException ex) {
                SimpleClans.debug(ex.getMessage());
                break;
            } catch (InvalidChannelException | ChannelExistsException ignored) {
                // There is already debug on #clearChannels
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void sendPrivateMessage(TextChannel textChannel, Message eventMessage, String message) {
        RestAction<PrivateChannel> privateChannelAction = eventMessage.getAuthor().openPrivateChannel();
        textChannel.deleteMessageById(eventMessage.getId()).queue(unused ->
                privateChannelAction.flatMap(privateChannel -> privateChannel.sendMessage(message)));
    }

    private void validateChannel(@NotNull String clanTag)
            throws InvalidChannelException, ChannelExistsException, ChannelsLimitException {
        settingsManager.load();
        Clan clan = clanManager.getClan(clanTag);
        if (clan == null) {
            throw new InvalidChannelException(String.format("Clan %s is null", clanTag));
        }
        if (!clan.isVerified() && !clan.isPermanent()) {
            throw new InvalidChannelException(String.format("Clan %s is not verified or permanent", clanTag));
        }

        Map<ClanPlayer, Member> discordClanPlayers = getDiscordPlayers(clan);
        if (discordClanPlayers.isEmpty()) {
            throw new InvalidChannelException(String.format("Clan %s doesn't have any linked players", clanTag),
                    "your.clan.doesnt.have.any.linked.player");
        }

        if (discordClanPlayers.size() < settingsManager.getInt(DISCORDCHAT_MINIMUM_LINKED_PLAYERS)) {
            throw new InvalidChannelException(String.format("Clan %s doesn't have minimum linked players", clanTag),
                    "your.clan.doesnt.have.minimum.linked.player");
        }

        if (!whitelist.isEmpty() && !whitelist.contains(clan.getTag())) {
            throw new InvalidChannelException(String.format("Clan %s is not listed on the whitelist", clanTag),
                    "your.clan.is.not.on.the.whitelist");
        }

        settingsManager.load();
        Map<String, Object> masterMap = new HashMap<>();
        Object rawMaster = settingsManager.getMap(DISCORDCHAT_CATEGORYS_AND_CHANNELS);
        if (rawMaster instanceof Map) {
            masterMap.putAll((Map<String, Object>) rawMaster);
        }

        // 2. Prüfen, ob für diesen Clan bereits Kanäle existieren
        if (masterMap.containsKey(clanTag)) {
            throw new ChannelExistsException(String.format("Channel %s is already exist", clanTag),
                    "your.clan.already.has.channel");
        }
    }

    @NotNull
    private Map<ClanPlayer, Member> getDiscordPlayers(@NotNull Clan clan) {
        Map<ClanPlayer, Member> discordClanPlayers = new HashMap<>();
        for (ClanPlayer cp : clan.getMembers()) {
            Member member = getMember(cp);
            if (member != null) {
                discordClanPlayers.put(cp, member);
            }
        }
        return discordClanPlayers;
    }

    void updateLeaderRole(@NotNull Member member, @NotNull ClanPlayer clanPlayer, DiscordAction action) {
        if (action == ADD && clanPlayer.isLeader()) {
            getGuild().addRoleToMember(member, getLeaderRole()).queue();
            SimpleClans.debug(String.format("Added leader role to %s (%s) discord member", member.getNickname(), member.getId()));
        } else {
            getGuild().removeRoleFromMember(member, getLeaderRole()).queue();
            SimpleClans.debug(String.format("Revoked leader role from %s (%s) discord member", member.getNickname(), member.getId()));
        }
    }

    private void updateViewPermission(@Nullable Member member, @NotNull GuildChannel channel, @NotNull DiscordAction action) {
        if (member == null) {
            return;
        }

        if (action == ADD) {
            channel.getPermissionContainer().upsertPermissionOverride(member).
                    setPermissions(Collections.singletonList(VIEW_CHANNEL), Collections.emptyList()).queue();
            SimpleClans.debug(String.format("Added view permission to %s (%s) discord member", member.getNickname(), member.getId()));
        } else {
            channel.getPermissionContainer().getManager().removePermissionOverride(member).queue();
            SimpleClans.debug(String.format("Revoked view permission from %s (%s) discord member", member.getNickname(), member.getId()));
        }
    }

    void updateViewPermission(@NotNull Member member, @NotNull Clan clan, DiscordAction action) {
        String tag = clan.getTag();
        Optional<TextChannel> channel = getChannel(tag);
        if (channel.isPresent()) {
            TextChannel textChannel = channel.get();
            updateViewPermission(member, textChannel, action);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean createChannelSilently(ClanPlayer clanPlayer) {
        Clan clan = clanPlayer.getClan();
        if (clan == null || !settingsManager.is(DISCORDCHAT_AUTO_CREATION)) {
            return false;
        }

        try {
            createChannel(clan.getTag());
        } catch (DiscordHookException ex) {
            // Clan is not following the conditions, categories are fulled or discord reaches the limit, nothing to do here.
            SimpleClans.debug(ex.getMessage());
        }

        return true;
    }

    enum DiscordAction {
        ADD, REMOVE
    }
}
