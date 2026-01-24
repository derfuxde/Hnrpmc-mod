package org.emil.hnrpmc.hnessentials.cosmetics.api;

import org.emil.hnrpmc.hnessentials.cosmetics.impl.DummyUserInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * An object containing the response of a user info get request.
 */
public interface UserInfo {
    /**
     * @return the skin of this user, in base64 format. Returns null if there is no string.
     */
    @Nullable
    String getSkin();

    /**
     * @return whether the player model is slim.
     */
    boolean isSlim();

    /**
     * @return the user's lore, i.e. the text that should be displayed below their name. This may start with a minecraft colour code.
     */
    String getLore();

    /**
     * @return the platform this user is registered on. Can be "java" or "bedrock".
     */
    String getPlatform();

    /**
     * @return the role of the user on the Cosmetica platform. For example, "admin" or "default". If the user has not used cosmetica before, their role will be "none".
     */
    String getRole();

    /**
     * @return whether the user should be rendered upside down in game due to region specific effects.
     */
    boolean isUpsideDown();

    /**
     * @return the client the user last connected with. Returns {@linkplain Optional#empty()} if the user has not recently used cosmetica or an associated client.
     */
    Optional<String> getClient();

    /**
     * @return whether the player is currently online and using cosmetica or another service that integrates with cosmetica. If the user chooses not to share this status, they will appear online regardless.
     * If the user is not registered with cosmetica, they will appear offline.
     */
    boolean isOnline();

    /**
     * @return A prefix to the user's name.
     */
    String getPrefix();

    /**
     * @return a suffix to the user's name.
     */
    String getSuffix();

    /**
     * @return a list of the hats worn by this user.
     */
    List<Model> getHats();

    /**
     * @return the shoulder buddies worn by this user.
     */
    Optional<ShoulderBuddies> getShoulderBuddies();

    /**
     * @return the back bling worn by this user.
     */
    Optional<Model> getBackBling();

    /**
     * @return the cape worn by this user.
     */
    Optional<Cape> getCape();

    /**
     * Get the icon's base64 image string. Each frame of an icon will be square. If the icon is of a different aspect ratio,
     * assume it is composed of multiple square frames stacked vertically. No icon is represented with an empty string.
     * @return the icon, in base64 image format. If the user has no icon, the string will be blank.
     * @apiNote it is recommended not to cache the icon image by the associated client, as one client (especially cosmetica itself) may have different icons for different people!
     *  If you wish to cache, it is recommended to instead depend on the image data itself. For example: using the hash of the base64 icon string.
     */
    String getIcon();

    /**
     * A {@link UserInfo} instance with dummy values for everything which can be used as a placeholder.
     */
    UserInfo DUMMY = new DummyUserInfo();
}
