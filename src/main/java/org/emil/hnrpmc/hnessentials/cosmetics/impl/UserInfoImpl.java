package org.emil.hnrpmc.hnessentials.cosmetics.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.emil.hnrpmc.hnessentials.cosmetics.api.*;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserInfoImpl implements UserInfo {
    public UserInfoImpl(@Nullable String skin, boolean slim, String lore, String platform, String role, boolean upsideDown, String prefix, String suffix, @Nullable String client, boolean online,
                        List<Model> hats, Optional<ShoulderBuddies> shoulderBuddiess, Optional<Model> backBlings, Optional<Cape> capes, String icon) {
        this.skin = skin;
        this.slim = slim;
        this.lore = lore;
        this.platform = platform;
        this.role = role;
        this.upsideDown = upsideDown;
        this.prefix = prefix;
        this.suffix = suffix;
        this.client = client;
        this.online = online;

        ShoulderBuddies shoulderBuddies = shoulderBuddiess.orElse(null);
        Model backBling = backBlings.orElse(null);
        Cape cape = capes.orElse(null);
        this.hats = hats;
        this.shoulderBuddies = shoulderBuddies;
        this.backBling = backBling;
        this.cape = cape;
        this.icon = icon;
    }

    public UserInfoImpl(@Nullable String skin, boolean slim, String lore, String platform, String role, boolean upsideDown, String prefix, String suffix, @Nullable String client, boolean online,
                        List<String> hats, String rightshoulderBuddie, String leftshoulderBuddie, String backBlings, String capes, String icon) {
        this.skin = skin;
        this.slim = slim;
        this.lore = lore;
        this.platform = platform;
        this.role = role;
        this.upsideDown = upsideDown;
        this.prefix = prefix;
        this.suffix = suffix;
        this.client = client;
        this.online = online;


        List<Model> hatbakableModels = new ArrayList<>();
        if (hats != null && !hats.isEmpty()) {
            for (String hatid : hats) {
                Model hatmodel = CosmeticFetcher.getModel(CosmeticType.HAT, hatid);
                if (hatmodel != null) {
                    hatbakableModels.add(hatmodel);
                }
            }
        }

        Optional<ShoulderBuddies> sbObj = Optional.empty();

        JsonObject right = null;
        try {
            Model sholderbudy = CosmeticFetcher.getModel(CosmeticType.SHOULDER_BUDDY, rightshoulderBuddie);
            if (sholderbudy != null) {
                right = JsonParser.parseString(sholderbudy.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Parsen der JSON-Datei: " + e.getMessage());
        }

        JsonObject left = null;
        try {
            Model sholderbudy = CosmeticFetcher.getModel(CosmeticType.SHOULDER_BUDDY, leftshoulderBuddie);
            if (sholderbudy != null) {
                left = JsonParser.parseString(sholderbudy.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Parsen der JSON-Datei: " + e.getMessage());
        }

        if (leftshoulderBuddie != null  && !leftshoulderBuddie.isEmpty() || rightshoulderBuddie != null && !rightshoulderBuddie.isEmpty() ) {
            sbObj = Optional.of(new ShoulderBuddiesImpl(
                    ModelImpl.parse(left != null?  left.getAsJsonObject() : null),
                    ModelImpl.parse(right != null ? right.getAsJsonObject() : null)
            ));
        }

        Optional<Model> BBlingbm = Optional.empty();
        if (backBlings != null && !backBlings.isEmpty() ) {
            BBlingbm = Optional.ofNullable(CosmeticFetcher.getModel(CosmeticType.BACK_BLING, backBlings));

        }

        Optional<Cape> CCO = Optional.empty();
        if (capes != null && !capes.isEmpty()) {
            CCO = Optional.ofNullable(CosmeticFetcher.getCape(capes));
        }

        ShoulderBuddies shoulderBuddies = sbObj.orElse(null);
        Model backBling = BBlingbm.orElse(null);
        Cape cape = CCO.orElse(null);
        this.hats = hatbakableModels;
        this.shoulderBuddies = shoulderBuddies;
        this.backBling = backBling;
        this.cape = cape;
        this.icon = icon;
        //new UserInfoImpl(skin, slim, lore, platform, role,upsideDown, prefix, suffix, client, online, hatbakableModels, sbObj, Optional.ofNullable(BBlingbm), capes, icon);
    }

    private final @Nullable String skin;
    private final boolean slim;
    private final String lore;
    private final String platform;
    private final String role;
    private final boolean upsideDown;
    private final String prefix;
    private final String suffix;
    private final @Nullable String client;
    private final boolean online;
    private final List<Model> hats;
    private final ShoulderBuddies shoulderBuddies;
    private final Model backBling;
    private final Cape cape;
    private final String icon;

    @Override
    @Nullable
    public String getSkin() {
        return this.skin;
    }

    @Override
    public boolean isSlim() {
        return this.slim;
    }

    @Override
    public String getLore() {
        return lore;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public boolean isUpsideDown() {
        return upsideDown;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public Optional<String> getClient() {
        return Optional.ofNullable(this.client);
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public List<Model> getHats() {
        return hats;
    }

    @Override
    public Optional<ShoulderBuddies> getShoulderBuddies() {
        return Optional.ofNullable(shoulderBuddies);
    }

    @Override
    public Optional<Model> getBackBling() {
        return Optional.ofNullable(backBling);
    }

    @Override
    public Optional<Cape> getCape() {
        return Optional.ofNullable(cape);
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "UserInfo[" +
                "lore=" + lore + ", " +
                "upsideDown=" + upsideDown + ", " +
                "prefix=" + prefix + ", " +
                "suffix=" + suffix + ", " +
                "hats=" + hats + ", " +
                "shoulderBuddies=" + shoulderBuddies + ", " +
                "backBling=" + backBling + ", " +
                "cape=" + cape + ']';
    }
}
