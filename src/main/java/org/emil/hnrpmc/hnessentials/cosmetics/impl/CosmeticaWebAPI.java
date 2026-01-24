package org.emil.hnrpmc.hnessentials.cosmetics.impl;

import com.google.gson.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.api.*;
//import org.emil.hnrpmc.hnessentials.cosmetics.utils.HostProvider;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.Response;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.Yootil;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticaAPI;
import org.emil.hnrpmc.hnessentials.cosmetics.api.IconSettings;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.SafeURL;
import org.emil.hnrpmc.hnessentials.network.CosmeticRegistry;
import org.emil.hnrpmc.hnessentials.network.RequestSetPayload;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;
import org.emil.hnrpmc.hnessentials.network.responsePlayerData;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CosmeticaWebAPI implements CosmeticaAPI {
    private CosmeticaWebAPI(@Nullable String masterToken, @Nullable String limited) {
        this.masterToken = masterToken;
        this.limitedToken = limited;
        this.loginInfo = Optional.empty();
        //this.apiHostProvider = apiHostProviderTemplate.clone();
    }

    private CosmeticaWebAPI(UUID uuid, String limitedToken, @Nullable String client) throws FatalServerErrorException, IOException {
        //this.apiHostProvider = apiHostProviderTemplate.clone();
        this.loginInfo = Optional.of(this.exchangeTokens(uuid, limitedToken, client));
    }

    private final Optional<LoginInfo> loginInfo;
    //private final HostProvider apiHostProvider;
    private String masterToken;
    private String limitedToken;
    private int timeout = 20 * 1000;
    private Consumer<String> urlLogger = s -> {};

    //private boolean forceHttps() {
        //return this.apiHostProvider.isForceHttps();
    //}

    private LoginInfo exchangeTokens(UUID uuid, String authToken, @Nullable String client) throws IllegalStateException, FatalServerErrorException, IOException {
        return new LoginInfo(false, false);
    }

    @Override
    public Optional<LoginInfo> getLoginInfo() {
        return this.loginInfo;
    }

    @Override
    public ServerResponse<VersionInfo> checkVersion(String minecraftVersion, String cosmeticaVersion) {
        SafeURL versionCheck = new SafeURL("versionCheck", "versionCheck");

        try {
            return new ServerResponse<>(new VersionInfo(
                    false,
                    false,
                    "",
                    "",
                    false
            ), versionCheck);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, versionCheck);
        }
    }

    @Override
    public ServerResponse<UserInfo> getUserInfo(@Nullable UUID uuid, @Nullable String username, boolean noThirdParty, boolean excludeModels, boolean forceShow) throws IllegalArgumentException {
        if (uuid == null && username == null) throw new IllegalArgumentException("Both uuid and username are null!");

        SafeURL target = new SafeURL("userInfo", "userInfo");

        try {
            Gson gson = HNessentials.getInstance().gson;

            PacketDistributor.sendToServer(new requestPlayerData(uuid));

            HNPlayerData playerData = HNessentials.getInstance().HNplayerDataMap.get(uuid);

            String jsonString = gson.toJson(playerData);
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            JsonArray hats = jsonObject.has("hats") ? jsonObject.get("hats").getAsJsonArray() : null;
            JsonObject shoulderBuddies = jsonObject.has("shoulderBuddies") ? jsonObject.get("shoulderBuddies").getAsJsonObject() : null;
            JsonObject backBling = jsonObject.has("backBling") ? jsonObject.get("backBling").getAsJsonObject() : null;
            JsonObject cloak = jsonObject.has("cape") ? jsonObject.get("cape").getAsJsonObject() : null;

            Optional<ShoulderBuddies> sbObj = Optional.empty();

            if (shoulderBuddies != null) {
                sbObj = Optional.of(new ShoulderBuddiesImpl(
                        ModelImpl.parse(shoulderBuddies.has("left") ? shoulderBuddies.get("left").getAsJsonObject() : null),
                        ModelImpl.parse(shoulderBuddies.has("right") ? shoulderBuddies.get("right").getAsJsonObject() : null)
                ));
            }

            JsonObject icon = jsonObject.get("icon").getAsJsonObject();

            return new ServerResponse<>(new UserInfoImpl(
                    Yootil.readNullableJsonString(jsonObject.get("skin")),
                    jsonObject.get("slim").getAsBoolean(),
                    jsonObject.get("lore").getAsString(),
                    jsonObject.get("platform").getAsString(),
                    jsonObject.get("role").getAsString(),
                    jsonObject.get("upsideDown").getAsBoolean(),
                    jsonObject.get("prefix").getAsString(),
                    jsonObject.get("suffix").getAsString(),
                    Yootil.readNullableJsonString(icon.get("client")),
                    icon.get("online").getAsBoolean(),
                    hats == null ? new ArrayList<>() : Yootil.mapObjects(hats, ModelImpl::_parse),
                    sbObj,
                    ModelImpl.parse(backBling),
                    BaseCape.parse(cloak),
                    icon.get("icon").getAsString()
            ), target);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, target);
        }
    }

    @Override
    public ServerResponse<UserSettings> getUserSettings() {
        SafeURL target = new SafeURL("userSettings", "userSettings");

        try {

            Map<String, CapeServer> oCapeServerSettings = new HashMap<>();

            return new ServerResponse<>(new UserSettingsImpl(
                    Yootil.toUUID("18189abd-85b4-47bf-8f78-6f08ae0db066"),
                    // cosmetics
                    true,
                    true,
                    true,
                    false,
                    0x2,
                    // other stuff
                    0L,
                    "default",
                    "de",
                    false,
                    false,
                    0,
                    false,
                    oCapeServerSettings
            ), target);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, target);
        }
    }

    /**
     * Generics hack.
     * @param <T> the class to force it to reference through generics so the darn thing compiles.
     */
    private static class GeneralCosmeticType<T extends CustomCosmetic> {
        private static <T extends CustomCosmetic> GeneralCosmeticType<T> from(CosmeticType<T> type) {
            return new GeneralCosmeticType<>();
        }

        private static GeneralCosmeticType<CustomCosmetic> any() {
            return new GeneralCosmeticType<>();
        }
    }

    private <T extends CustomCosmetic> ServerResponse<CosmeticsPage<T>> getCosmeticsPage(SafeURL url, GeneralCosmeticType<T> cosmeticType) {
        this.urlLogger.accept(url.safeUrl());

        try {
            //JsonObject json = response.getAsJson();
            //checkErrors(url, json);

            //boolean nextPage = json.get("nextPage").getAsBoolean();
            List<T> cosmetics = new ArrayList<>();

            //for (JsonElement element : json.getAsJsonArray("list")) {
                //cosmetics.add((T) parse(element.getAsJsonObject()));
            //}

            //return new ServerResponse<>(new CosmeticsPage<>(cosmetics, nextPage), url);
            return null;
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, url);
        }
    }

    @Override
    public <T extends CustomCosmetic> ServerResponse<CosmeticsPage<T>> getRecentCosmetics(CosmeticType<T> type, int page, int pageSize, Optional<String> query) {
        SafeURL url = new SafeURL("getRecentCosmetics", "getRecentCosmetics");
        return getCosmeticsPage(url, GeneralCosmeticType.from(type));
    }

    @Override
    public ServerResponse<CosmeticsPage<CustomCosmetic>> getPopularCosmetics(int page, int pageSize) {
        SafeURL url = new SafeURL("getPopularCosmetics", "getPopularCosmetics");
        return getCosmeticsPage(url, GeneralCosmeticType.any());
    }

    @Override
    public ServerResponse<CosmeticsPage<CustomCosmetic>> getOfficialCosmetics(int page, int pageSize) {
        SafeURL url = new SafeURL("getOfficialCosmetics", "getOfficialCosmetics");
        return getCosmeticsPage(url, GeneralCosmeticType.any());
    }

    @Override
    public ServerResponse<List<OwnedCosmetic>> getCosmeticsOwnedBy(@Nullable UUID uuid, @Nullable String username) {
        if (uuid == null && username == null) throw new IllegalArgumentException("Both uuid and username are null!");

        SafeURL url = new SafeURL("getCosmeticsOwnedBy", "getCosmeticsOwnedBy");//createMinimalLimited("/get/userownedcosmetics?user=" + Yootil.firstNonNull(uuid, username));

        this.urlLogger.accept(url.safeUrl());

        try {

            // else, it is an array.
            List<OwnedCosmetic> cosmetics = new ArrayList<>();

            return new ServerResponse<>(cosmetics, url);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, url);
        }
    }

    @Override
    public ServerResponse<List<String>> getLoreList(LoreType type) throws IllegalArgumentException {
        if (type == LoreType.DISCORD || type == LoreType.TWITCH || type == LoreType.NONE) throw new IllegalArgumentException("Invalid lore type for getLoreList: " + type);

        SafeURL url = new SafeURL("getLoreList", "getLoreList");
        List<String> lores = List.of("Geiler Man", "Tuff Guy", "Alpha");

        try {
            return new ServerResponse<>(lores, url);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, url);
        }
    }

    @Override
    public <T extends CustomCosmetic> ServerResponse<T> getCosmetic(CosmeticType<T> type, String id) {
        SafeURL url = new SafeURL("getCosmetic", "getCosmetic");

        try {
            // Hol das Original-Objekt aus der Registry
            CustomCosmetic cosmetic = CosmeticRegistry.get(id);

            if (cosmetic == null) {
                throw new RuntimeException("Cosmetic mit ID " + id + " nicht gefunden!");
            }

            // Typ-Sicherheit prüfen und direkt zurückgeben
            return new ServerResponse<>((T) cosmetic, url);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, url);
        }
    }

    @Override
    public ServerResponse<List<Panorama>> getPanoramas() {
        SafeURL url = new SafeURL("getPanoramas", "getPanoramas");
        this.urlLogger.accept(url.safeUrl());

        try {
            List<Panorama> result = new ArrayList<>();


            return new ServerResponse<>(result, url);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, url);
        }
    }

    @Override
    public ServerResponse<CosmeticsUpdates> everyThirtySecondsInAfricaHalfAMinutePasses(InetSocketAddress serverAddress, long timestamp) throws IllegalArgumentException {
        SafeURL awimbawe = new SafeURL("everyThirtySecondsInAfricaHalfAMinutePasses", "everyThirtySecondsInAfricaHalfAMinutePasses");//create("/get/everythirtysecondsinafricahalfaminutepasses?ip=" + Yootil.base64Ip(serverAddress), OptionalLong.of(timestamp));

        this.urlLogger.accept(awimbawe.safeUrl());

        try {

            List<String> notifications = new ArrayList<>();

            List<User> users = new ArrayList<>();

            return new ServerResponse<>(new CosmeticsUpdates(notifications, users, 1L), awimbawe);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, awimbawe);
        }
    }

    // Client/ endpoints

    private ServerResponse<String> requestSet(SafeURL target, String value) {
        this.urlLogger.accept(target.safeUrl());

        try {
            PacketDistributor.sendToServer(new RequestSetPayload(target.url(), value));
            return new ServerResponse<>(value, target);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, target);
        }
    }

    private ServerResponse<Boolean> requestSetZ(SafeURL target, Object value) {

        try {
            PacketDistributor.sendToServer(new RequestSetPayload(target.url(), String.valueOf(value)));
            return new ServerResponse<>(true, target);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, target);
        }
    }

    @Override
    public ServerResponse<Boolean> setCosmetic(CosmeticPosition position, String id, boolean requireOfficial) {
        SafeURL target = new SafeURL("setCosmetic", "setCosmetic");//create("/client/setcosmetic?type=" + position.getUrlString() + "&id=" + id + (requireOfficial ? "&requireofficial" : ""), OptionalLong.empty());
        return requestSetZ(target, id);
    }

    @Override
    public ServerResponse<String> setLore(LoreType type, String lore) {
        if (type == LoreType.DISCORD || type == LoreType.TWITCH) throw new IllegalArgumentException("Invalid lore type for setLore(LoreType, String): " + type);

        SafeURL target = new SafeURL("setlore", "setlore"); //create("setlore"?type=" + type.toString().toLowerCase(Locale.ROOT) + "&value=" + Yootil.base64(Yootil.urlEncode(lore)), OptionalLong.empty());

        return requestSet(target, lore);
    }

    @Override
    public ServerResponse<String> removeLore() {
        return this.setLore(LoreType.NONE, "");
    }

    @Override
    public ServerResponse<Boolean> setPanorama(int id) {
        SafeURL target = new SafeURL("setPanorama", "setPanorama"); //create("/client/setpanorama?panorama=" + id, OptionalLong.empty());
        return requestSetZ(target, String.valueOf(id));
    }

    @Override
    public ServerResponse<Map<String, CapeDisplay>> setCapeServerSettings(Map<String, CapeDisplay> settings) {
        SafeURL target = new SafeURL("setCapeServerSettings", "setCapeServerSettings"); //create("/client/capesettings?" + settings.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue().id).collect(Collectors.joining("&")), OptionalLong.empty());
        this.urlLogger.accept(target.safeUrl());

        try {


            //return new ServerResponse<>(Yootil.mapObject(settings, element -> CapeDisplay.byId(element.getAsInt())), target);
        } catch (RuntimeException e) {
            return new ServerResponse<>(e, target);
        }
        return null;
    }

    @Override
    public ServerResponse<Boolean> updateUserSettings(Map<String, Object> settings) {
        SafeURL target = new SafeURL("updateUserSettings", "updateUserSettings");//create("/v2/client/updatesettings?" + settings.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&")), OptionalLong.empty());
        return requestSetZ(target, settings);
    }

    @Override
    public ServerResponse<Boolean> updateIconSettings(IconSettings iconSettings) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("iconsettings", iconSettings.packToInt());
        return this.updateUserSettings(settings);
    }

    /**@Override
    public ServerResponse<String> uploadModel(CosmeticType<Model> type, String name, String base64Texture, JsonObject model, int flags) {
        SafeURL target = create("/client/upload" + type.getUrlString(), OptionalLong.empty());
        this.urlLogger.accept(target.safeUrl() + " (POST)");

        try (Response response = Response.post(target)
                .set("name", name)
                .set("image", base64Texture)
                .set("model", model.toString())
                .set("extrainfo", flags)
                .submit()) {
            JsonObject obj = response.getAsJson();
            checkErrors(target, obj);

            return new ServerResponse<>(obj.get("success").getAsString(), target);
        }
        catch (IOException ie) {
            return new ServerResponse<>(ie, target);
        }
        catch (RuntimeException e) {
            return new ServerResponse<>(e, target);
        }
    }*/

    @Override
    public void setUrlLogger(Consumer<String> urlLogger) {
        this.urlLogger = urlLogger;
    }

    @Override
    public void setRequestTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setForceHttps(boolean forceHttps) {
        //this.apiHostProvider.setForceHttps(forceHttps);
    }

    @Override
    public boolean isFullyAuthenticated() {
        return true;//return this.masterToken != null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;//this.isFullyAuthenticated() || this.limitedToken != null;
    }

    @Override
    public boolean isHttpsForced() {
        return false;//this.forceHttps();
    }

    /**
     * Use this method if you're cringe.<br/>
     * (exists to stop reflection being necessary for the few times it's justified to manually get the token rather than going through the api)
     * @return the master token on this instance
     */
    public String getMasterToken() {
        return this.masterToken;
    }

    // Global Force Https
    private static boolean enforceHttpsGlobal;

    public static void setDefaultForceHttps(boolean forceHttps) {
        enforceHttpsGlobal = forceHttps;
        // update api host provider too
        //if (apiHostProviderTemplate != null) apiHostProviderTemplate.setForceHttps(forceHttps);
    }

    public static boolean getDefaultForceHttps() {
        return enforceHttpsGlobal;
    }

    // Initialisation Stuff

    //private static HostProvider apiHostProviderTemplate;
    private static String authApiServerHost;

    private static String websiteHost;
    private static String authServerHost;

    private static String message;

    private static File apiCache;

    public static String getMessage() {
        return message;
    }

    public static String getWebsite() {
        return websiteHost;
    }

    @Override
    public UserInfo getUserInfo() {
        return null;
    }

    public static CosmeticaAPI fromTempToken(String tempToken, UUID uuid, @Nullable String client) throws IllegalStateException, IOException, FatalServerErrorException {
        retrieveAPIIfNoneCached();
        return new CosmeticaWebAPI(uuid, tempToken, client);
    }

    public static CosmeticaAPI fromMinecraftToken(String minecraftToken, String username, UUID uuid, @Nullable String client) throws IllegalStateException, IOException, FatalServerErrorException {
        retrieveAPIIfNoneCached();

        byte[] publicKey;

        // https://wiki.vg/Protocol_Encryption
        try (Response response = Response.get(authApiServerHost + "/key")) {
            publicKey = response.getAsByteArray();
        }

        byte[] sharedSecret = Yootil.randomBytes(16);
        String hash = Yootil.hash("".getBytes(StandardCharsets.US_ASCII), sharedSecret, publicKey);

        // authenticate with minecraft
        try (Response response = Response.postJson("https://sessionserver.mojang.com/session/minecraft/join")
                .set("accessToken", minecraftToken)
                .set("selectedProfile", uuid.toString().replaceAll("-", ""))
                .set("serverId", hash)
                .submit()) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Exchange Tokens with Cosmetica
        try (Response response = Response.postJson(authApiServerHost + "/verify")
                .set("secret", new String(Base64.getEncoder().encode(sharedSecret)))
                .set("username", username)
                .submit()) {
            JsonObject data = response.getAsJson();
            checkErrors(SafeURL.direct(authApiServerHost + "/verify"), data);

            return new CosmeticaWebAPI(uuid, data.get("token").getAsString(), client);
        }
    }

    public static CosmeticaAPI fromTokens(String masterToken, @Nullable String limitedToken) throws IllegalStateException {
        retrieveAPIIfNoneCached();
        return new CosmeticaWebAPI(masterToken, limitedToken);
    }

    public static CosmeticaAPI newUnauthenticatedInstance() throws IllegalStateException {
        retrieveAPIIfNoneCached();
        return new CosmeticaWebAPI(null, null);
    }

    @Nullable
    public static String getApiServerHost(boolean requireResult) throws IllegalStateException {
        if (requireResult) retrieveAPIIfNoneCached();
        return "1234";//apiHostProviderTemplate == null ? null : apiHostProviderTemplate.getSecureUrl();
    }

    @Nullable
    public static String getFastInsecureApiServerHost(boolean requireResult) throws IllegalStateException {
        if (requireResult) retrieveAPIIfNoneCached();
        return "1234";//apiHostProviderTemplate == null ? null : apiHostProviderTemplate.getFastInsecureUrl();
    }

    @Nullable
    public static String getAuthServerHost(boolean requireResult) throws IllegalStateException {
        if (requireResult) retrieveAPIIfNoneCached();
        return authServerHost;
    }

    @Nullable
    public static String getAuthApiServerHost(boolean requireResult) throws IllegalStateException {
        if (requireResult) retrieveAPIIfNoneCached();
        return authApiServerHost;
    }

    public static void setAPICache(File api) {
        apiCache = api;
    }

    private static void retrieveAPIIfNoneCached() throws IllegalStateException {
        //if (apiHostProviderTemplate == null) { // if this sequence has not already been initiated
            final String apiGetHost = enforceHttpsGlobal ? "https://cosmetica.cc/getapi" : "http://cosmetica.cc/getapi";

            String apiGetData = """
{
    "api": "",
    "auth-api": "",
    "auth-server": {
        "hostname": "",
        "port": "25596"
    },
    "website": "",
    "message": "unblock us PLEASE"
}
""";


            if (apiCache != null) apiGetData = Yootil.loadOrCache(apiCache, apiGetData);

            if (apiGetData == null) {
                throw new IllegalStateException("Could not receive Cosmetica API host");
            }

            JsonObject data = new JsonParser().parse(apiGetData).getAsJsonObject();
            //apiHostProviderTemplate = new HostProvider(data.get("api").getAsString(), enforceHttpsGlobal);
            authApiServerHost = data.get("auth-api").getAsString();
            websiteHost = data.get("website").getAsString();
            JsonObject auth = data.get("auth-server").getAsJsonObject();
            authServerHost = auth.get("hostname").getAsString() + ":" + auth.get("port").getAsInt();
            message = data.get("message").getAsString();
        //}
    }

    private static void checkErrors(SafeURL url, JsonObject response) {
        if (response.has("error")) {
            throw new CosmeticaAPIException(url, response.get("error").getAsString());
        }
    }

    private static CustomCosmetic parse(JsonObject object) {
        // yes this code is (marginally) better
        // no I will not use it instead of the above
        CosmeticType<?> type = CosmeticType.fromTypeString(object.get("type").getAsString()).get();

        if (type == CosmeticType.CAPE) {
            return (CustomCosmetic) BaseCape.parse(object).get();
        }
        else {
            return ModelImpl.parse(object).get();
        }
    }

    private static JsonArray getAsArray(SafeURL url, JsonElement element) throws CosmeticaAPIException {
        if (element.isJsonObject()) {
            checkErrors(url, element.getAsJsonObject());
        }

        return element.getAsJsonArray();
    }
}
