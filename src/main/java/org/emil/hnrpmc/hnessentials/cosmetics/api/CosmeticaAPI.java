package org.emil.hnrpmc.hnessentials.cosmetics.api;

import com.google.gson.JsonObject;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.CosmeticaWebAPI;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A general interface with the Cosmetica Web API. Methods that throw IOException typically throw it when there is an issue contacting the API server, and {@link CosmeticaAPIException} if the api server can be contacted, but returns an error.
 */
public interface CosmeticaAPI {
    //////////////////////
    //  Web-API Methods //
    //////////////////////

    /**
     * Sends a version check request to the cosmetica servers and retrieves text to give to the user if there is an update, otherwise returns an empty string.
     * @param minecraftVersion the version of the cosmetica mod.
     * @param cosmeticaVersion the minecraft version, duh. {@code (Use SharedConstants.getCurrentVersion().getId()} if you're a minecraft mod using this API).
     * @return an object with a message sent by the API if the cosmetica version is outdated or old enough that it may not function correctly.
     */
    ServerResponse<VersionInfo> checkVersion(String minecraftVersion, String cosmeticaVersion);

    /**
     * Head on the safari to check out the lion king's new cosmetics! I mean, uh, ping this to get updates on any cosmetic changes you may have missed in the last 4 minutes from users on the server you're on, and allow other cosmetica users on the same server to receive cosmetics updates for you.<br>
     * If you provide a timestamp of 0, the endpoint will not send any users nor notifications, but instead only respond with a timestamp to use next time. The cosmetica mod calls this endpoint with a timestamp of 0 upon first joining a server to get its initial timestamp for this server.
     * @param serverAddress the address of the minecraft server you're on. This {@link InetSocketAddress} must have an IP and port associated.
     * @return the updates from this endpoint.
     * @throws IllegalArgumentException if the InetSocketAddress does not have an IP and port.
     * @apiNote the response to this endpoint provides a timestamp to use when you next call it from the same server.
     */
    ServerResponse<CosmeticsUpdates> everyThirtySecondsInAfricaHalfAMinutePasses(InetSocketAddress serverAddress, long timestamp) throws IllegalArgumentException;

    /**
     * Retrieves user info from the api server via either the UUID, username, or both. UUID is used preferentially.
     * @param uuid the uuid of the player to retrieve data of.
     * @param username the username of the player to retrieve data of.
     * @return a representation of the cosmetics data of the given player.
     * @throws IllegalArgumentException if both {@code uuid} and {@code username} are null.
     */
    default ServerResponse<UserInfo> getUserInfo(@Nullable UUID uuid, @Nullable String username) throws IllegalArgumentException {
        return this.getUserInfo(uuid, username, false, false, false);
    }

    /**
     * Retrieves user info from the api server via either the UUID, username, or both. UUID is used preferentially.
     * @param uuid the uuid of the player to retrieve data of.
     * @param username the username of the player to retrieve data of.
     * @param noThirdParty whether the api should only send cosmetica capes, regardless of the user's cape server settings.
     * @return a representation of the cosmetics data of the given player.
     * @throws IllegalArgumentException if both {@code uuid} and {@code username} are null.
     */
    default ServerResponse<UserInfo> getUserInfo(@Nullable UUID uuid, @Nullable String username, boolean noThirdParty) throws IllegalArgumentException {
        return this.getUserInfo(uuid, username, noThirdParty, false, false);
    }

    /**
     * Retrieves user info from the api server via either the UUID, username, or both. UUID is used preferentially.
     * @param uuid the uuid of the player to retrieve data of.
     * @param username the username of the player to retrieve data of.
     * @param excludeModels whether to exclude all models and textures from the response. Cape/Model instances from this will have an empty string for these fields instead.
     * @param forceShow whether to ignore your cosmetic visibility settings when retrieving data for yourself. Only has an effect when getting your own user info.
     * @return a representation of the cosmetics data of the given player.
     * @throws IllegalArgumentException if both {@code uuid} and {@code username} are null.
     * @deprecated use {@link CosmeticaAPI#getUserInfo(UUID, String, boolean, boolean, boolean)}.
     */
    @Deprecated
    default ServerResponse<UserInfo> getUserInfo(@Nullable UUID uuid, @Nullable String username, boolean excludeModels, boolean forceShow) throws IllegalArgumentException {
        return this.getUserInfo(uuid, username, false, excludeModels, forceShow);
    }

    /**
     * Retrieves user info from the api server via either the UUID, username, or both. UUID is used preferentially.
     * @param uuid the uuid of the player to retrieve data of.
     * @param username the username of the player to retrieve data of.
     * @param noThirdParty whether the api should only send cosmetica capes, regardless of the user's cape server settings.
     * @param excludeModels whether to exclude all models and textures from the response. Cape/Model instances from this will have an empty string for these fields instead.
     * @param forceShow whether to ignore your cosmetic visibility settings when retrieving data for yourself. Only has an effect when getting your own user info.
     * @return a representation of the cosmetics data of the given player.
     * @throws IllegalArgumentException if both {@code uuid} and {@code username} are null.
     */
    ServerResponse<UserInfo> getUserInfo(@Nullable UUID uuid, @Nullable String username, boolean noThirdParty, boolean excludeModels, boolean forceShow) throws IllegalArgumentException;

    /**
     * Retrieves the settings of the user associated with the token and some basic data.
     * @return the user's settings, as JSON.
     */
    ServerResponse<UserSettings> getUserSettings();

    /**
     * Gets a page of 16 cosmetics, sorted by upload date.
     * @param type the type of cosmetic to search for.
     * @param page the page number to browse.
     * @return a page of cosmetics.
     */
    default <T extends CustomCosmetic> ServerResponse<CosmeticsPage<T>> getRecentCosmetics(CosmeticType<T> type, int page) {
        return getRecentCosmetics(type, page, 16, Optional.empty());
    }

    /**
     * Gets a page of cosmetics that match the given query, sorted by upload date.
     * @param type the type of cosmetic to search for.
     * @param page the page number to browse.
     * @param pageSize how large each page should be. For example, the desktop website uses 16, whereas mobile uses 8.
     * @param query the search term. If a query is provided, 'official' cosmetica cosmetics may be returned in addition to user-uploaded cosmetics.
     * @return a page of cosmetics sorted by upload date.
     */
    <T extends CustomCosmetic> ServerResponse<CosmeticsPage<T>> getRecentCosmetics(CosmeticType<T> type, int page, int pageSize, Optional<String> query);

    /**
     * Gets a page of 16 cosmetics sorted by popularity.
     * @param page the page number to browse.
     * @return a page of cosmetics sorted by popularity.
     */
    default ServerResponse<CosmeticsPage<CustomCosmetic>> getPopularCosmetics(int page) {
        return getPopularCosmetics(page, 16);
    }

    /**
     * Gets a page of official ("system") cosmetics.
     * @param page the page number to browse.
     * @param pageSize how large each page should be. For example, the desktop website uses 16, whereas mobile uses 8.
     * @return a page of official cosmetics.
     */
    ServerResponse<CosmeticsPage<CustomCosmetic>> getOfficialCosmetics(int page, int pageSize);

    /**
     * Gets a page of 16 official ("system") cosmetics.
     * @param page the page number to browse.
     * @return a page of official cosmetics.
     */
    default ServerResponse<CosmeticsPage<CustomCosmetic>> getOfficialCosmetics(int page) {
        return getOfficialCosmetics(page, 16);
    }

    /**
     * Gets a page of cosmetics sorted by popularity.
     * @param page the page number to browse.
     * @param pageSize how large each page should be. For example, the desktop website uses 16, whereas mobile uses 8.
     * @return a page of cosmetics sorted by popularity.
     */
    ServerResponse<CosmeticsPage<CustomCosmetic>> getPopularCosmetics(int page, int pageSize);

    /**
     * Retrieves user info from the api server via either the UUID or username. If both are provided, UUID is used preferentially.
     * This will only show {@linkplain UploadState#APPROVED approved} cosmetics unless you are requesting cosmetics you own while authenticated.
     * @param uuid the uuid of the player to retrieve data of.
     * @param username the username of the player to retrieve data of.
     * @return a list of cosmetics owned by the given player.
     * @throws IllegalArgumentException if both {@code uuid} and {@code username} are null.
     * @apiNote do note that the data sent for each cosmetic by this endpoint is slightly different to the data sent by others.
     * To get data not available from this endpoint, use {@link CosmeticaAPI#getCosmetic(CosmeticType, String)} after {@linkplain OwnedCosmetic#getId() retrieving the id} using this endpoint.
     */
    ServerResponse<List<OwnedCosmetic>> getCosmeticsOwnedBy(@Nullable UUID uuid, @Nullable String username);

    /**
     * Gets the list of available lore of that type the user can set to.
     * @param type a type of lore that uses a list of options. Namely {@link LoreType#PRONOUNS} or {@link LoreType#TITLES}.
     * @return a list of lore strings the user can select from.
     * @throws IllegalArgumentException if the lore type does not have an associated lore list (if it's not "Pronouns" or "Titles").
     */
    ServerResponse<List<String>> getLoreList(LoreType type) throws IllegalArgumentException;

    /**
     * Gets a cosmetic from the cosmetica servers.
     * @param type the type of cosmetic.
     * @param id the id of the cosmetic.
     * @return an object representing the cosmetic.
     */
    <T extends CustomCosmetic> ServerResponse<T> getCosmetic(CosmeticType<T> type, String id);

    /**
     * Gets the list of panoramas the user can select from. The cosmetica website displays a panorama behind the user on their user page.
     *
     * @return a list of panoramas the user can use.
     * @apiNote if no token is given, returns the panoramas all users can select.
     */
    ServerResponse<List<Panorama>> getPanoramas();

    /**
     * Sets the cosmetic at the given position for this user.
     * @param position the position of the cosmetic to set.
     * @param id the id of the cosmetic. Set the id to "none" to remove a cosmetic.
     * @return true if successful. Otherwise the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    default ServerResponse<Boolean> setCosmetic(CosmeticPosition position, String id) {
        return this.setCosmetic(position, id, false);
    }

    /**
     * Sets the cosmetic at the given position for this user.
     * @param position the position of the cosmetic to set.
     * @param id the id of the cosmetic. Set the id to "none" to remove a cosmetic.
     * @param requireOfficial whether to only allow official capes. If this is set, then trying to set a non-official cape will return an error.
     * @return true if successful. Otherwise the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    ServerResponse<Boolean> setCosmetic(CosmeticPosition position, String id, boolean requireOfficial);

    /**
     * Sets the lore for this user.
     * @param type the type of lore to be set. Can be either {@link LoreType#PRONOUNS}, {@link LoreType#TITLES}, or {@link LoreType#NONE}.
     * @param lore the lore string to set as the lore.
     * @return the new lore string of the player (including colour codes) if successful. Otherwise the server response will have an error.
     * @throws IllegalArgumentException if the lore type cannot be set through this endpoint (if it's not "Pronouns," "Titles," or "None").
     * @apiNote requires full authentication (a master token).
     */
    ServerResponse<String> setLore(LoreType type, String lore) throws IllegalArgumentException;

    /**
     * Removes this user's lore. Equivalent to {@code setLore(LoreType.NONE, "")}.
     * @return the new lore string of the player (including colour codes) if successful. Otherwise the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    ServerResponse<String> removeLore();

    /**
     * Sets the panorama for this user.
     * @param id the id of the panorama to set. Panorama ids this user can use can be gotten with {@link CosmeticaAPI#getPanoramas}.
     * @return true if successful. Otherwise the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    ServerResponse<Boolean> setPanorama(int id);

    /**
     * Sets how cosmetica should handle each cape service for this user. In addition, <b>ANY CAPE NOT SPECIFIED IS RESET TO THE DEFAULT VALUE. You should call {@link CosmeticaAPI#getUserSettings()} at least ONCE before calling this to get the current settings of the user! You have been warned.</b>
     * @param settings the settings to set as the settings.
     * @return the new cape settings if successful. Otherwise, the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    ServerResponse<Map<String, CapeDisplay>> setCapeServerSettings(Map<String, CapeDisplay> settings);

    /**
     * Updates the specified settings for the user. You do not need to specify every setting, unlike {@link CosmeticaAPI#setCapeServerSettings(Map)}
     * @param settings
     * @return true if successful. Otherwise the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    ServerResponse<Boolean> updateUserSettings(Map<String, Object> settings);

    /**
     * Updates the icon settings for the given user. These affect how the icon
     * @apiNote requires full authentication (a master token).<br>
     * This is identical to calling {@linkplain CosmeticaAPI#updateUserSettings(Map)} with the "iconsettings" property set
     * as the {@linkplain IconSettings#packToInt() packed integer representation} of the given icon settings.
     */
    ServerResponse<Boolean> updateIconSettings(IconSettings iconSettings);

    /**
     * Uploads a model-based cosmetic to the server under this account.
     * @param type the type of cosmetic to upload.
     * @param name the name of the cosmetic to upload.
     * @param base64Texture the 32x32 texture in base64 form. Ensure it is a png that starts with "data:image/png;base64,"
     * @param model the json model to upload
     * @return the id of the cosmetic if successful. Otherwise the server response will have an error.
     * @deprecated use {@link CosmeticaAPI#uploadModel(CosmeticType, String, String, JsonObject, int)}.
     * @apiNote requires full authentication (a master token).
     */
    //@Deprecated
    //default ServerResponse<String> uploadModel(CosmeticType<Model> type, String name, String base64Texture, JsonObject model) {
        //return this.uploadModel(type, name, base64Texture, model, type == CosmeticType.SHOULDER_BUDDY ? 1 : 0);
    //}

    /**
     * Uploads a model-based cosmetic to the server under this account.
     * @param type the type of cosmetic to upload.
     * @param name the name of the cosmetic to upload.
     * @param base64Texture the 32x32 texture in base64 form. Ensure it is a png that starts with "data:image/png;base64,"
     * @param model the json model to upload
     * @param flags the flags of the model to upload. See the constants in {@link Model}.
     * @return the id of the cosmetic if successful. Otherwise the server response will have an error.
     * @apiNote requires full authentication (a master token).
     */
    //ServerResponse<String> uploadModel(CosmeticType<Model> type, String name, String base64Texture, JsonObject model, int flags);

    ///////////////////////////
    //   Non-Web-API Methods //
    ///////////////////////////

    /**
     * Get the login info from the authentication's token exchange, if it was done to create this instance.
     * @return the login info as an optional
     */
    Optional<LoginInfo> getLoginInfo();

    /**
     * Pass a consumer to be invoked with the URL whenever a URL is contacted. This can be useful for debug logging purposes.
     * @param logger the logger to pass.
     */
    void setUrlLogger(@Nullable Consumer<String> logger);

    /**
     * Sets the request timeout for this API instance. Default is 20 seconds.
     * @param timeout the request timeout, in milliseconds.
     */
    void setRequestTimeout(int timeout);

    /**
     * Sets whether this instance of {@linkplain CosmeticaAPI} will require https for all connections.
     * When set, this instance will ignore the default value set in {@link CosmeticaAPI#setDefaultForceHttps(boolean)} and instead use the value given.
     * This is off by default. However, Cosmetica API will ALWAYS use HTTPS for sensitive data
     * transactions regardless of this setting (anything that uses your master token). The kind of
     * endpoints this will affect the HTTPS status of are non-information-sensitive ones such as getting
     * another player's cosmetics.
     * @param forceHttps whether to force https for all connections.
     */
    void setForceHttps(boolean forceHttps);

    /**
     * @return whether this cosmetica api instance has a master API token.
     */
    boolean isFullyAuthenticated();

    /**
     * @return whether this cosmetica api instance has any API token (master or limited).
     */
    boolean isAuthenticated();

    /**
     * Gets whether this instance of {@linkplain CosmeticaAPI} will require https for all connections.
     * This is off by default. However, Cosmetica API will ALWAYS use HTTPS for sensitive data
     * transactions regardless of this setting (anything that uses your master token). The kind of
     * endpoints this will affect the HTTPS status of are non-information-sensitive ones such as getting
     * another player's cosmetics.
     * @return whether HTTPS connections for all connections are forced.
     */
    boolean isHttpsForced();

    /**
     * Gets whether instances of {@linkplain CosmeticaAPI} will require https by default for all connections.
     * Individual instances {@linkplain CosmeticaAPI#setForceHttps(boolean) can override this}.
     * This is off by default. However, Cosmetica API will ALWAYS use HTTPS for sensitive data
     * transactions regardless of this setting (anything that uses your master token). The kind of
     * endpoints this will affect the HTTPS status of are non-information-sensitive ones such as getting
     * another player's cosmetics.
     * @return whether HTTPS connections for all connections are forced.
     */
    static boolean isHttpsForcedByDefault() {
        return CosmeticaWebAPI.getDefaultForceHttps();
    }

    /**
     * Sets whether instances of {@linkplain CosmeticaAPI} will require https by default for all connections.
     * Individual instances {@linkplain CosmeticaAPI#setForceHttps(boolean) can override this}.
     * This is off by default. However, Cosmetica API will ALWAYS use HTTPS for sensitive data
     * transactions regardless of this setting (anything that uses your master token). The kind of
     * endpoints this will affect the HTTPS status of are non-information-sensitive ones such as getting
     * another player's cosmetics.
     * @return whether HTTPS connections for all connections are forced.
     */
    static void setDefaultForceHttps(boolean forceHttps) {
        CosmeticaWebAPI.setDefaultForceHttps(forceHttps);
    }

    /**
     * Create an instance with which to access the cosmetica web api via one token. Cannot accept a temporary token. To use a cosmetica temporary authentication token, see {@link CosmeticaAPI#fromTemporaryToken(String, UUID)}.
     * @param token a cosmetica token. Can be a master token, or a token.
     * @return an instance of the cosmetica web api, configured with the given token. The instance will behave in the following way for each case:<br>
     * <h2>Master Token</h2>
     *   Uses only the master token for an account. This instance will only make requests on https, unlike other instances which make non-sensitive "get" requests under http for speed.
     * <h2>Limited Token</h2>
     *   Uses only a cosmetica 'limited' or 'get' token, a special token for use over HTTP which only has access to specific "get" endpoints. This instance will only make requests on http, so is less secure.
     * @throws IllegalStateException if an api instance cannot be retrieved.
     * @throws IllegalArgumentException if the token given does not match the format for any of the 3 token types.
     */
    static CosmeticaAPI fromToken(String token) throws IllegalStateException, IllegalArgumentException {
        switch (token.charAt(0)) {
            case 'm':
                return CosmeticaWebAPI.fromTokens(token, null);
            case 'l':
                return CosmeticaWebAPI.fromTokens(null, token);
            case 't':
                throw new IllegalArgumentException("For temporary tokens, use fromTempToken(UUID, token)");
            default:
                throw new IllegalArgumentException("Cannot determine type of token " + token);
        }
    }

    /**
     * Creates a new authenticated {@link CosmeticaAPI} instance using a cosmetica temporary authentication token: a special token used as an intermediate step between initial authentication and receiving the master and limited token.
     * @param temporaryToken the temporary token.
     * @param uuid the player's UUID.
     * @return an authenticated instance of {@link CosmeticaAPI} with both a master and limited token.
     * @throws FatalServerErrorException if there is a 5XX error while contacting the servers.
     * @throws IOException if there is an I/O exception while contacting the servers.
     */
    static CosmeticaAPI fromTemporaryToken(String temporaryToken, UUID uuid) throws FatalServerErrorException, IOException {
        return CosmeticaWebAPI.fromTempToken(temporaryToken, uuid, null);
    }

    /**
     * Creates a new authenticated {@link CosmeticaAPI} instance using a cosmetica temporary authentication token: a special token used as an intermediate step between initial authentication and receiving the master and limited token.
     * @param temporaryToken the temporary token.
     * @param uuid the player's UUID.
     * @param client the id of the client to connect as. You can provide null or "" to use no client.
     * @return an authenticated instance of {@link CosmeticaAPI} with both a master and limited token.
     * @throws FatalServerErrorException if there is a 5XX error while contacting the servers.
     * @throws IOException if there is an I/O exception while contacting the servers.
     */
    static CosmeticaAPI fromTemporaryToken(String temporaryToken, UUID uuid, @Nullable String client) throws FatalServerErrorException, IOException {
        return CosmeticaWebAPI.fromTempToken(temporaryToken, uuid, client);
    }

    /**
     * Login to Cosmetica with a minecraft account's token, username, and UUID directly. The resulting {@link CosmeticaAPI} instance will be fully authenticated with a master and limited token, as with using a temporary token in {@link CosmeticaAPI#fromToken(String)}.
     * @param minecraftToken the user's minecraft authentication token.
     * @param username the user's username.
     * @param uuid the user's UUID.
     * @return an instance of the cosmetica web api, configured with the given account.
     * @throws IllegalStateException if an api instance cannot be retrieved.
     * @throws IOException if there is an I/O exception while contacting the minecraft auth servers or cosmetica servers to authenticate the user.
     * @throws FatalServerErrorException if there is a 5XX error while contacting the servers.
     * @apiNote this can take a couple seconds as it has to make 2 POST requests and a GET request to authenticate.
     */
    static CosmeticaAPI fromMinecraftToken(String minecraftToken, String username, UUID uuid) throws IllegalStateException, IOException, FatalServerErrorException {
        return CosmeticaWebAPI.fromMinecraftToken(minecraftToken, username, uuid, null);
    }

    /**
     * Login to Cosmetica with a minecraft account's token, username, and UUID directly. The resulting {@link CosmeticaAPI} instance will be fully authenticated with a master and limited token, as with using a temporary token in {@link CosmeticaAPI#fromToken(String)}.
     * @param minecraftToken the user's minecraft authentication token.
     * @param username the user's username.
     * @param uuid the user's UUID.
     * @param client the id of the client to connect as. You can provide null or "" to use no client.
     * @return an instance of the cosmetica web api, configured with the given account.
     * @throws IllegalStateException if an api instance cannot be retrieved.
     * @throws IOException if there is an I/O exception while contacting the minecraft auth servers or cosmetica servers to authenticate the user.
     * @throws FatalServerErrorException if there is a 5XX error while contacting the servers.
     * @apiNote this can take a couple seconds as it has to make 2 POST requests and a GET request to authenticate.
     */
    static CosmeticaAPI fromMinecraftToken(String minecraftToken, String username, UUID uuid, @Nullable String client) throws IllegalStateException, IOException, FatalServerErrorException {
        return CosmeticaWebAPI.fromMinecraftToken(minecraftToken, username, uuid, client);
    }

    /**
     * @param masterToken the cosmetica master token.
     * @param limitedToken the cosmetica 'limited' or 'get' token, a special token for use over HTTP which only has access to specific "get" endpoints.
     * @return an instance of the cosmetica web api, configured with the given tokens.
     * @throws IllegalStateException if an api instance cannot be retrieved.
     */
    static CosmeticaAPI fromTokens(String masterToken, String limitedToken) throws IllegalStateException {
        return CosmeticaWebAPI.fromTokens(masterToken, limitedToken);
    }

    /**
     * Creates a new instance which is not authenticated. The provided instance will be very limited in what endpoints it can call.
     * @return an instance of the cosmetica web api with no associated token.
     * @throws IllegalStateException if an api instance cannot be retrieved.
     */
    static CosmeticaAPI newUnauthenticatedInstance() throws IllegalStateException {
        return CosmeticaWebAPI.newUnauthenticatedInstance();
    }

    /**
     * Sets the file to cache the API endpoints to, and retrieve from in case of the Github CDN or "getapi" endpoints go offline.
     * @deprecated CosmeticaDotJava skips contacting the Github CDN now and goes straight to /getapi. Use {@link CosmeticaAPI#setAPICache(File)} instead.
     */
    @Deprecated
    static void setAPICaches(File apiCache, File apiGetCache) {
        CosmeticaWebAPI.setAPICache(apiCache);
    }

    /**
     * Sets the file to cache the API endpoints to, and retrieve from in case the node balancer goes offline.
     */
    static void setAPICache(File apiCache) {
        CosmeticaWebAPI.setAPICache(apiCache);
    }

    /**
     * Get the message retrieved once a {@link CosmeticaAPI} instance is retrieved from {@link CosmeticaAPI#fromToken}, {@link CosmeticaAPI#fromMinecraftToken(String, String, UUID)}, {@link CosmeticaAPI#fromTemporaryToken(String, UUID)}, {@link CosmeticaAPI#fromTokens}, or another method that forces initial API data to be fetched is called.
     */
    @Nullable
    static String getMessage() {
        return CosmeticaWebAPI.getMessage();
    }

    /**
     * Get the auth server url. Will force the initial API data to be fetched if it is not.
     */
    static String getAuthServer() {
        return CosmeticaWebAPI.getAuthServerHost(true);
    }

    /**
     * Get the auth-api server url. Will force the initial API data to be fetched if it is not.
     */
    static String getAuthApiServer() {
        return CosmeticaWebAPI.getAuthApiServerHost(true);
    }

    /**
     * Get the cosmetica website url, retrieved once a {@link CosmeticaAPI} instance is retrieved from {@link CosmeticaAPI#fromToken}, {@link CosmeticaAPI#fromMinecraftToken(String, String, UUID)}, {@link CosmeticaAPI#fromTemporaryToken(String, UUID)}, {@link CosmeticaAPI#fromTokens}, or another method that forces initial API data to be fetched is called.
     */
    @Nullable
    static String getWebsite() {
        return CosmeticaWebAPI.getWebsite();
    }

    /**
     * Get the cosmetica api server url being used, retrieved once a {@link CosmeticaAPI} instance is retrieved from {@link CosmeticaAPI#fromToken}, {@link CosmeticaAPI#fromMinecraftToken(String, String, UUID)}, {@link CosmeticaAPI#fromTemporaryToken(String, UUID)}, {@link CosmeticaAPI#fromTokens}, or another method that forces initial API data to be fetched is called.
     */
    @Nullable
    static String getAPIServer() {
        return CosmeticaWebAPI.getApiServerHost(false);
    }

    /**
     * Get the cosmetica api server url being used as an insecure http:// url, retrieved once a {@link CosmeticaAPI} instance is retrieved from {@link CosmeticaAPI#fromToken}, {@link CosmeticaAPI#fromMinecraftToken(String, String, UUID)}, {@link CosmeticaAPI#fromTemporaryToken(String, UUID)}, {@link CosmeticaAPI#fromTokens}, or another method that forces initial API data to be fetched is called.
     * @implNote may not actually give an http url! If global force https is on, will be https.
     */
    @Nullable
    static String getHttpAPIServer() {
        return CosmeticaWebAPI.getFastInsecureApiServerHost(false);
    }

    // Überschreibe alle Methoden, die normalerweise zum Server gehen würden
    UserInfo getUserInfo();
}
