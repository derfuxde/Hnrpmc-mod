package org.emil.hnrpmc.hnclaim;

import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.lang.reflect.Type;
import java.util.*;

public final class Claim {
    private String name;
    private UUID ownerUUID;
    private String corner1;
    private String corner2;
    private String corner3;
    private String corner4;
    private List<claimperms> perms = new ArrayList<>();
    private Map<String, List<claimperms>> permsoverride;
    private String center;
    private String dimension;
    private List<UUID> banPlayers = new ArrayList<>();
    private long createDate;
    private long last_used;

    public Claim() {
        this.name = "";
    }

    public Claim(String name, ServerPlayer player, String pos1, String pos2, String pos3, String pos4, String center) {
        this.name = name;
        this.ownerUUID = player.getUUID();
        this.corner1 = pos1;
        this.corner2 = pos2;
        this.corner3 = pos3;
        this.corner4 = pos4;
        this.banPlayers = new ArrayList<>();
        this.perms = new ArrayList<>();
        this.createDate = (new Date()).getTime();
        this.last_used = (new Date()).getTime();
        this.center = center;
        this.dimension = player.level().dimension().location().toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getownerUUID() {
        return this.ownerUUID;
    }

    public void setCorner1(String pos) {
        this.corner1 = pos;
    }

    public String getCorner1() {
        return this.corner1;
    }

    public void setCorner2(String pos) {
        this.corner2 = pos;
    }

    public String getCorner2() {
        return this.corner2;
    }

    public void setCorner3(String pos) {
        this.corner3 = pos;
    }

    public String getCorner3() {
        return this.corner3;
    }

    public void setCorner4(String pos) {
        this.corner4 = pos;
    }

    public String getCorner4() {
        return this.corner4;
    }

    public void setPerms(String perms) {
        try {
            List<String> permsslist = List.of(perms.replace("[", "").replace("]", "").split(", "));
            if (permsslist.isEmpty() || perms.isEmpty()) {
                this.perms = new ArrayList<>();
                return;
            }
            List<claimperms> clanperms = new ArrayList<>();
            for (String permis : permsslist) {
                claimperms perm = claimperms.valueOf(permis.trim().toUpperCase());
                clanperms.add(perm);
            }
            this.perms = clanperms;
        } catch (IllegalArgumentException e) {
            HNClaims.getInstance().getLogger().debug("fehler " + e);
        }

    }

    public void addPerms(String perms) {
        List<claimperms> clanperms = new ArrayList<>();
        if (!getPerms().isEmpty()) {
            clanperms = getPerms();
        }
        for (claimperms permis : claimperms.values()) {
            if (Objects.equals(permis.getPermName(), perms)) {
                clanperms.add(permis);
                break;
            }
        }
        this.perms = clanperms;
    }

    public void addPerms(claimperms perms) {
        //List<claimperms> clanperms = new ArrayList<>();
        //if (!getPerms().isEmpty()) {
            //clanperms = getPerms();
        //}
        //clanperms.add(perms);
        this.perms.add(perms);
    }

    public void removePerms(String perms) {
        List<claimperms> clanperms = new ArrayList<>();
        if (!getPerms().isEmpty()) {
            clanperms = getPerms();
        }
        for (claimperms permis : claimperms.values()) {
            if (Objects.equals(permis.getPermName(), perms)) {
                clanperms.remove(permis);
                break;
            }
        }
        this.perms = clanperms;
    }

    public void removePerms(claimperms perms) {
        this.perms.remove(perms);
    }

    public List<claimperms> getPerms() {
        return this.perms;
    }

    public List<String> getStringPerms() {
        List<String> permslist = new ArrayList<>();
        if (perms == null) return permslist;
        for (claimperms perm : perms) {
            permslist.add(perm.name());
        }
        return permslist;
    }

    public void setoverridePerms(String perms) {
        if (Objects.equals(perms, "")){
            this.permsoverride = new HashMap<>();
            return;
        }
        Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
        Map<String, List<String>> restoredMap = HNClaims.getInstance().getGSON().fromJson(perms, type);
        Map<String, List<claimperms>> endemap = new HashMap<>();
        for (Map.Entry<String, List<String>> plperms : restoredMap.entrySet()) {
            List<claimperms> permslis = new ArrayList<>();
            for (String permname : plperms.getValue()) {
                for (claimperms claimperms : claimperms.values()) {
                    if (Objects.equals(claimperms.getPermName(), permname)) {
                        permslis.add(claimperms);
                    }
                }
            }
            endemap.put(plperms.getKey(), permslis);
        }
        this.permsoverride = endemap;
    }

    public void addoverridePerms(UUID uuid, claimperms perm) {
        Map<String, List<claimperms>> map = getoverridePerms();

        if (map.containsKey(uuid.toString())) {
            List<claimperms> loadperms = map.get(uuid.toString());

            loadperms.add(perm);

            map.replace(uuid.toString(), loadperms);
        } else {
            List<claimperms> listperms = new ArrayList<>();

            listperms.add(perm);

            map.put(uuid.toString(), listperms);
        }
    }

    public void addoverridePerms(String clanname, claimperms perm) {
        Map<String, List<claimperms>> map = getoverridePerms();

        if (map.containsKey(clanname)) {
            List<claimperms> loadperms = map.get(clanname);

            loadperms.add(perm);

            map.replace(clanname, loadperms);
        } else {
            List<claimperms> listperms = new ArrayList<>();

            listperms.add(perm);

            map.put(clanname, listperms);
        }
    }

    public void removeoverridePerms(UUID uuid, claimperms perm) {
        Map<String, List<claimperms>> map = getoverridePerms();

        if (map.containsKey(uuid.toString())) {
            List<claimperms> loadperms = map.get(uuid.toString());

            loadperms.remove(perm);

            map.replace(uuid.toString(), loadperms);
        } else {
            List<claimperms> listperms = new ArrayList<>();

            map.put(uuid.toString(), listperms);
        }
    }

    public void removeoverridePerms(String name, claimperms perm) {
        Map<String, List<claimperms>> map = getoverridePerms();

        if (map.containsKey(name)) {
            List<claimperms> loadperms = map.get(name);

            loadperms.remove(perm);

            map.replace(name, loadperms);
        } else {
            List<claimperms> listperms = new ArrayList<>();

            map.put(name, listperms);
        }
    }

    public Map<String, List<claimperms>> getoverridePerms() {
        if (permsoverride == null) return new HashMap<>();
        return this.permsoverride;
    }

    public String getoverrideStringPerms() {
        if (permsoverride == null || permsoverride.isEmpty()) {
            return "";
        }

        String json = HNClaims.getInstance().getGSON().toJson(this.permsoverride);

        return json;
    }

    public List<claimperms> getPlayerPerms(ServerPlayer player) {
        return this.permsoverride.get(player.getUUID().toString());
    }

    public List<claimperms> getPlayerPerms(UUID uuid) {
        if (permsoverride == null) return new ArrayList<>();
        return this.permsoverride.get(uuid.toString());
    }

    public List<claimperms> getPlayerPerms(String playerName, HNClaims plugin) {
        if (permsoverride == null) return new ArrayList<>();
        return this.permsoverride.get(plugin.getServer().getProfileCache().get(playerName).get().getId().toString());
    }

    public List<claimperms> getClaimPerms(String playerName) {
        if (permsoverride == null) return new ArrayList<>();
        return this.permsoverride.get("." + playerName);
    }

    public void setCenter(String pos) {
        this.center = pos;
    }

    public String getCenter() {
        return this.center;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getDimension() {
        return this.dimension;
    }

    public void setCreateDate(long date) {
        this.createDate = date;
    }

    public long getCreateDate() {
        return this.createDate;
    }

    public void setLast_used(long date) {
        this.last_used = date;
    }

    public long getLast_used() {
        return this.last_used;
    }

    public void setBanPlayers(List<UUID> uuids) {
        this.banPlayers = uuids;
    }

    public void addBanPlayers(UUID uuid) {
        if (this.banPlayers == null) {
            this.banPlayers = new ArrayList<>();
        }
        if (this.banPlayers.isEmpty()) {
            this.banPlayers = new ArrayList<>();
        }
        if (uuid == null || banPlayers.contains(uuid)) return;
        this.banPlayers.add(uuid);
    }

    public void removeBanPlayers(UUID uuid) {
        if (this.banPlayers == null) {
            this.banPlayers = new ArrayList<>();
            return;
        }

        if (!(this.banPlayers instanceof ArrayList)) {
            this.banPlayers = new ArrayList<>(this.banPlayers);
        }

        if (uuid == null || !banPlayers.contains(uuid)) return;

        this.banPlayers.remove(uuid);
    }

    public void setStringBanPlayers(String uuids) {
        if (uuids.isEmpty()) {
            this.banPlayers = new ArrayList<>();
            return;
        }
        String[] splitteduuids = uuids.split(", ");
        this.banPlayers = Arrays.stream(splitteduuids).map(st -> UUID.fromString(st)).toList();
    }

    public List<UUID> getBanPlayers() {
        return banPlayers;
    }

    public List<String> getStringBanPlayers() {
        if (this.banPlayers.isEmpty()) {
            return Collections.singletonList("");
        }
        return banPlayers.stream().map(bp -> bp.toString()).toList();
    }

}
