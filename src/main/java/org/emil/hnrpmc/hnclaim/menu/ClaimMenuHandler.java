package org.emil.hnrpmc.hnclaim.menu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.HeavyCoreBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.claimperms;
import org.emil.hnrpmc.hnessentials.ChestLocks.config.LockData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ClaimMenuHandler {

    private static ItemStack createIcon(ItemLike item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack getPlayerHead(ServerPlayer serverPlayer, UUID uuid, String prefix) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        String name = serverPlayer.getServer().getProfileCache().get(uuid)
                .map(p -> p.getName()).orElse("Unbekannt");
        head.set(DataComponents.CUSTOM_NAME, Component.literal(prefix + name));
        head.set(DataComponents.PROFILE, new ResolvableProfile(serverPlayer.getServer().getProfileCache().get(uuid).orElseThrow()));
        return head;
    }

    private static ItemStack getPlayerHead(ServerPlayer serverPlayer, UUID uuid) {
        return getPlayerHead(serverPlayer, uuid, "");
    }

    public static void openMainMenu(ServerPlayer player, Claim claim) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            return new ChestMenu(MenuType.GENERIC_9x3, id, inv, new SimpleContainer(27), 3) {
                {
                    setItem(11, 0,createIcon(Items.PLAYER_HEAD, "§a§lSpieler Berechtigungen verwalten"));
                    setItem(13, 0,createIcon(Items.PAPER, "§eBesitzer: " + player.getServer().getProfileCache().get(claim.getownerUUID()).map(op -> op.getName()).orElse("?")));
                    setItem(15, 0,createIcon(Items.RED_WOOL, "§c§lGenerelle Berechtigungen verwalten"));
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId < 0 || slotId >= 27) { super.clicked(slotId, button, clickType, player); return; }

                    if (slotId == 11) openPlayerPermsMenu((ServerPlayer) player, claim);
                    else if (slotId == 15) {
                        openGeneralPermsManager((ServerPlayer) player, claim);
                    }
                }
            };
        }, Component.literal("Schloss-Verwaltung")));
    }

    private static void openPlayerPermsMenu(ServerPlayer player, Claim claim) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            return new ChestMenu(MenuType.GENERIC_9x5, id, inv, new SimpleContainer(45), 5) {
                {
                    int slot = 0;
                    for (Map.Entry<String, List<claimperms>> entry: claim.getoverridePerms().entrySet()) {
                        if (slot >= 18) break;
                        //if (entry.getKey().startsWith(".")) continue;
                        try {
                            setItem(slot++, 0,getPlayerHead(player, UUID.fromString(entry.getKey())));
                        } catch (Exception e) {
                            ItemStack stack = Items.RED_BANNER.getDefaultInstance();
                            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                                claim.deleteoverridePerms(entry.getKey());
                            }
                            stack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.getKey()));
                            setItem(slot-1, 0, stack);
                        }
                    }

                    setItem(40, 0,createIcon(Items.HOPPER, "§a§lOnline-Spieler hinzufügen"));
                    setItem(44, 0,createIcon(Items.ARROW, "§fZurück"));
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId < 0 || slotId >= 45) return;

                    List<String> names = claim.getoverridePerms().keySet().stream().toList();//.stream().filter(id -> !id.startsWith(".")).toList();
                    // Zurück
                    if (slotId == 44) openMainMenu((ServerPlayer) player, claim);
                    else if (slotId == 40) openAddPlayerMenu((ServerPlayer) player, claim);
                    else if (slotId < names.size()) {
                        String forUUID = names.get(slotId);
                        openPermsManager((ServerPlayer) player, claim, forUUID);
                    }
                }
            };
        }, Component.literal("Vertraute Spieler")));
    }

    private static void openGeneralPermsManager(ServerPlayer sp, Claim claim) {
        Map<Integer, claimperms> permslotids = new HashMap<>();
        Map<Integer, claimperms> allowedpermslotids = new HashMap<>();
        int perpage = 9;
        page = 0;
        allowedpage = 0;
        if (sp == null) return;
        if (claim == null) return;
        sp.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            return new ChestMenu(MenuType.GENERIC_9x5, id, inv, new SimpleContainer(45), 5) {
                {
                    updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId < 0 || slotId >= 45) return;

                    // Zurück
                    if (slotId == 40) openMainMenu((ServerPlayer) player, claim);
                    if (slotId == 44) {
                        if (getPage(claim.getPerms(), allowedpage + 1, perpage).isEmpty()) return;
                        allowedpage++;
                        updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                    } else if (slotId == 42) {
                        if (allowedpage == 0) return;
                        allowedpage--;
                        updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                    } else if (slotId == 38) {
                        if (getPage(Arrays.stream(claimperms.values()).filter(perm -> !claim.getPerms().contains(perm)).toList(), page + 1, perpage).isEmpty())
                            return;
                        page++;
                        updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                    } else if (slotId == 36) {
                        if (page == 0) return;
                        page--;
                        updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                    }
                    //else if (slotId == 22) openAddPlayerMenu((ServerPlayer) player, be);
                    else if (permslotids.containsKey(slotId + 1)) {
                        claimperms claimpermss = permslotids.get(slotId + 1);
                        System.out.println("clicked" + claimpermss.getPermName());
                        setItem(slotId, 0, createIcon(Items.AIR, "§7Zurück"));

                        claim.addPerms(claimpermss);

                        HNClaims.getInstance().getStorageManager().updateClaim(claim);
                        HNClaims.getInstance().getStorageManager().saveModified();


                        if (!allowedpermslotids.isEmpty()) {
                            allowedpermslotids.clear();
                        }


                        if (!permslotids.isEmpty()) {
                            permslotids.clear();
                        }

                        updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                    } else if (allowedpermslotids.containsKey(slotId + 1)) {
                        claimperms claimpermss = allowedpermslotids.get(slotId + 1);
                        System.out.println("clicked " + claimpermss.getPermName());
                        setItem(slotId, 0, createIcon(Items.AIR, "§7Zurück"));

                        claim.removePerms(claimpermss);

                        HNClaims.getInstance().getStorageManager().updateClaim(claim);
                        HNClaims.getInstance().getStorageManager().saveModified();

                        if (!allowedpermslotids.isEmpty()) {
                            allowedpermslotids.clear();
                        }


                        if (!permslotids.isEmpty()) {
                            permslotids.clear();
                        }

                        updateitems(this, permslotids, allowedpermslotids, claim, page, perpage, allowedpage);
                    }
                }
            };
        }, Component.literal("Generelle Berechtigungen für " + claim.getName())));
    }

    static int page = 0;
    static int allowedpage = 0;
    private static void openPermsManager(ServerPlayer sp, Claim claim, @Nullable String target) {
        Map<Integer, claimperms> permslotids = new HashMap<>();
        Map<Integer, claimperms> allowedpermslotids = new HashMap<>();
        int perpage = 9;
        page = 0;
        allowedpage = 0;
        if (sp == null) return;
        if (claim == null) return;
        if (target == null) return;
        try {
            UUID uuid = UUID.fromString(target);
            sp.openMenu(new SimpleMenuProvider((id, inv, p) -> {
                return new ChestMenu(MenuType.GENERIC_9x5, id, inv, new SimpleContainer(45), 5) {
                    {
                        updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                    }

                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, Player player) {
                        if (slotId < 0 || slotId >= 45) return;

                        // Zurück
                        if (slotId == 40) openPlayerPermsMenu((ServerPlayer) player, claim);
                        if (slotId == 44) {
                            if (getPage(claim.getPlayerPerms(uuid), allowedpage + 1, perpage).isEmpty()) return;
                            allowedpage++;
                            updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                        } else if (slotId == 42) {
                            if (allowedpage == 0) return;
                            allowedpage--;
                            updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                        } else if (slotId == 38) {
                            if (getPage(Arrays.stream(claimperms.values()).filter(perm -> !claim.getPlayerPerms(uuid).contains(perm)).toList(), page + 1, perpage).isEmpty())
                                return;
                            page++;
                            updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                        } else if (slotId == 36) {
                            if (page == 0) return;
                            page--;
                            updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                        }
                        //else if (slotId == 22) openAddPlayerMenu((ServerPlayer) player, be);
                        else if (permslotids.containsKey(slotId + 1)) {
                            claimperms claimpermss = permslotids.get(slotId + 1);
                            System.out.println("clicked" + claimpermss.getPermName());
                            setItem(slotId, 0, createIcon(Items.AIR, "§7Zurück"));

                            claim.addoverridePerms(uuid, claimpermss);

                            HNClaims.getInstance().getStorageManager().updateClaim(claim);
                            HNClaims.getInstance().getStorageManager().saveModified();


                            if (!allowedpermslotids.isEmpty()) {
                                allowedpermslotids.clear();
                            }


                            if (!permslotids.isEmpty()) {
                                permslotids.clear();
                            }

                            updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                        } else if (allowedpermslotids.containsKey(slotId + 1)) {
                            claimperms claimpermss = allowedpermslotids.get(slotId + 1);
                            System.out.println("clicked " + claimpermss.getPermName());
                            setItem(slotId, 0, createIcon(Items.AIR, "§7Zurück"));

                            claim.removeoverridePerms(uuid, claimpermss);

                            HNClaims.getInstance().getStorageManager().updateClaim(claim);
                            HNClaims.getInstance().getStorageManager().saveModified();

                            if (!allowedpermslotids.isEmpty()) {
                                allowedpermslotids.clear();
                            }


                            if (!permslotids.isEmpty()) {
                                permslotids.clear();
                            }

                            updateitems(this, permslotids, allowedpermslotids, claim, uuid, page, perpage, allowedpage);
                        }
                    }
                };
            }, Component.literal("Berechtigungen für " + sp.getServer().getProfileCache().get(uuid).get().getName())));
        } catch (Exception ignored) {
            sp.openMenu(new SimpleMenuProvider((id, inv, p) -> {
                return new ChestMenu(MenuType.GENERIC_9x5, id, inv, new SimpleContainer(45), 5) {
                    {
                        updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                    }

                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, Player player) {
                        if (slotId < 0 || slotId >= 45) return;

                        // Zurück
                        if (slotId == 40) openPlayerPermsMenu((ServerPlayer) player, claim);
                        if (slotId == 44) {
                            if (getPage(claim.getClaimPerms(target), allowedpage+1, perpage).isEmpty()) return;
                            allowedpage++;
                            updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                        }else if (slotId == 42) {
                            if (allowedpage == 0) return;
                            allowedpage--;
                            updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                        }else if (slotId == 38) {
                            if (getPage(Arrays.stream(claimperms.values()).filter(perm -> !claim.getClaimPerms(target).contains(perm)).toList(), page+1, perpage).isEmpty()) return;
                            page++;
                            updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                        }else if (slotId == 36) {
                            if (page == 0) return;
                            page--;
                            updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                        }
                        //else if (slotId == 22) openAddPlayerMenu((ServerPlayer) player, be);
                        else if (permslotids.containsKey(slotId+1)) {
                            claimperms claimpermss = permslotids.get(slotId+1);
                            System.out.println("clicked" + claimpermss.getPermName());
                            setItem(slotId, 0,createIcon(Items.AIR, "§7Zurück"));

                            claim.addoverridePerms(target, claimpermss);

                            HNClaims.getInstance().getStorageManager().updateClaim(claim);
                            HNClaims.getInstance().getStorageManager().saveModified();


                            if (!allowedpermslotids.isEmpty()) {
                                allowedpermslotids.clear();
                            }


                            if (!permslotids.isEmpty()) {
                                permslotids.clear();
                            }

                            updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                        } else if (allowedpermslotids.containsKey(slotId+1)) {
                            claimperms claimpermss = allowedpermslotids.get(slotId+1);
                            System.out.println("clicked " + claimpermss.getPermName());
                            setItem(slotId, 0,createIcon(Items.AIR, "§7Zurück"));

                            claim.removeoverridePerms(target, claimpermss);

                            HNClaims.getInstance().getStorageManager().updateClaim(claim);
                            HNClaims.getInstance().getStorageManager().saveModified();

                            if (!allowedpermslotids.isEmpty()) {
                                allowedpermslotids.clear();
                            }


                            if (!permslotids.isEmpty()) {
                                permslotids.clear();
                            }

                            updateitems(this, permslotids, allowedpermslotids, claim, target, page, perpage, allowedpage);
                        }
                    }
                };
            }, Component.literal("Berechtigungen für " + target)));
        }

    }

    private static void updateitems(AbstractContainerMenu container, Map<Integer, claimperms> permslotids, Map<Integer, claimperms> allowedpermslotids, Claim claim, UUID target, int page, int perpage, int allowedpage) {
        if (!allowedpermslotids.isEmpty()) {
            allowedpermslotids.clear();
        }


        if (!permslotids.isEmpty()) {
            permslotids.clear();
        }
        int off = 9;
        int line = 1;
        int slot = 0 + off * line;
        for (int i = 9; i < 44; i++) {
            container.setItem(i, 0, Items.AIR.getDefaultInstance());
        }
        for (claimperms entry: getPage(List.of(claimperms.values()).stream().filter(perm -> !claim.getPlayerPerms(target).contains(perm)).toList(), page, perpage)) {
            if (slot - off * line == 3) {
                line++;
                slot = off * line;
            }
            if (slot >= 36) break;
            if (claim.getPlayerPerms(target).contains(entry)) continue;
            ItemStack stack = entry.getItem().getDefaultInstance();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("§f"+entry.getPermName()));
            container.setItem(slot++, 0, stack);
            permslotids.put(slot, entry);
        }

        List<String> testlist = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            testlist.add("e"+i);
        }
        int ploff = 6;
        int plline = 1;
        int plslot = (9 * plline) + ploff;
        for (claimperms entry : getPage(claim.getPlayerPerms(target), allowedpage, perpage)) {
            ItemStack stack = entry.getItem().getDefaultInstance();
            if (plslot - ((9 * plline) - 0) == 9) {
                plline++;
                plslot = (9 * plline) + ploff;
                //stack = Items.GREEN_WOOL.getDefaultInstance();
            }
            if (plline >= 4) break;
            //entry.getItem().getDefaultInstance();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("§f"+entry.getPermName()));
            container.setItem(plslot++, 0, stack);
            allowedpermslotids.put(plslot, entry);
        }

        ItemStack backhead = createFtbArrowHead(false);
        backhead.set(DataComponents.CUSTOM_NAME, Component.literal("§y§7Nächste Seite"));

        ItemStack nexthead = createFtbArrowHead(true);
        nexthead.set(DataComponents.CUSTOM_NAME, Component.literal("§y§7Letzte Seite"));

        container.setItem(4, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(13, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(22, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(31, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(42, 0, (allowedpage == 0 ? Items.AIR.getDefaultInstance() : nexthead));
        container.setItem(44, 0, (getPage(claim.getPlayerPerms(target), allowedpage+1, perpage).isEmpty() ? Items.AIR.getDefaultInstance() : backhead));
        container.setItem(36, 0, (page == 0 ? Items.AIR.getDefaultInstance() : nexthead));
        container.setItem(38, 0, (getPage(Arrays.stream(claimperms.values()).filter(perm -> !claim.getPlayerPerms(target).contains(perm)).toList(), page+1, perpage).isEmpty() ? Items.AIR.getDefaultInstance() : backhead));
        //container.setItem(40, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(7, 0,createIcon(Items.GREEN_TERRACOTTA, "§aErlaubt"));
        container.setItem(1, 0,createIcon(Items.RED_TERRACOTTA, "§cNicht Erlaubt"));
        container.setItem(40, 0,createIcon(Items.ARROW, "§7Zurück"));
    }

    private static void updateitems(AbstractContainerMenu container, Map<Integer, claimperms> permslotids, Map<Integer, claimperms> allowedpermslotids, Claim claim, String target, int page, int perpage, int allowedpage) {
        if (!allowedpermslotids.isEmpty()) {
            allowedpermslotids.clear();
        }


        if (!permslotids.isEmpty()) {
            permslotids.clear();
        }
        int off = 9;
        int line = 1;
        int slot = off * line;
        for (int i = 9; i < 44; i++) {
            container.setItem(i, 0, Items.AIR.getDefaultInstance());
        }
        List<claimperms> perms = Stream.of(claimperms.values()).filter(perm -> !claim.getClaimPerms(target).contains(perm)).toList();
        for (claimperms entry: getPage(perms, page, perpage)) {
            if (slot - off * line == 3) {
                line++;
                slot = off * line;
            }
            if (slot >= 36) break;
            if (claim.getClaimPerms(target).contains(entry)) continue;
            ItemStack stack = entry.getItem().getDefaultInstance();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("§f"+entry.getPermName()));
            container.setItem(slot++, 0, stack);
            permslotids.put(slot, entry);
        }

        List<String> testlist = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            testlist.add("e"+i);
        }
        int ploff = 6;
        int plline = 1;
        int plslot = (9 * plline) + ploff;
        for (claimperms entry : getPage(claim.getClaimPerms(target), allowedpage, perpage)) {
            ItemStack stack = entry.getItem().getDefaultInstance();
            if (plslot - ((9 * plline) - 0) == 9) {
                plline++;
                plslot = (9 * plline) + ploff;
                //stack = Items.GREEN_WOOL.getDefaultInstance();
            }
            if (plline >= 4) break;
            //entry.getItem().getDefaultInstance();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("§f"+entry.getPermName()));
            container.setItem(plslot++, 0, stack);
            allowedpermslotids.put(plslot, entry);
        }

        ItemStack backhead = createFtbArrowHead(false);
        backhead.set(DataComponents.CUSTOM_NAME, Component.literal("§y§7Nächste Seite"));

        ItemStack nexthead = createFtbArrowHead(true);
        nexthead.set(DataComponents.CUSTOM_NAME, Component.literal("§y§7Letzte Seite"));

        container.setItem(4, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(13, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(22, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(31, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(42, 0, (allowedpage == 0 ? Items.AIR.getDefaultInstance() : nexthead));
        container.setItem(44, 0, (getPage(claim.getClaimPerms(target), allowedpage+1, perpage).isEmpty() ? Items.AIR.getDefaultInstance() : backhead));
        container.setItem(36, 0, (page == 0 ? Items.AIR.getDefaultInstance() : nexthead));
        container.setItem(38, 0, (getPage(Arrays.stream(claimperms.values()).filter(perm -> !claim.getClaimPerms(target).contains(perm)).toList(), page+1, perpage).isEmpty() ? Items.AIR.getDefaultInstance() : backhead));
        //container.setItem(40, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(7, 0,createIcon(Items.GREEN_TERRACOTTA, "§aErlaubt"));
        container.setItem(1, 0,createIcon(Items.RED_TERRACOTTA, "§cNicht Erlaubt"));
        container.setItem(40, 0,createIcon(Items.ARROW, "§7Zurück"));
    }

    private static void updateitems(AbstractContainerMenu container, Map<Integer, claimperms> permslotids, Map<Integer, claimperms> allowedpermslotids, Claim claim, int page, int perpage, int allowedpage) {
        if (!allowedpermslotids.isEmpty()) {
            allowedpermslotids.clear();
        }


        if (!permslotids.isEmpty()) {
            permslotids.clear();
        }
        int off = 9;
        int line = 1;
        int slot = off * line;
        for (int i = 9; i < 44; i++) {
            container.setItem(i, 0, Items.AIR.getDefaultInstance());
        }
        List<claimperms> perms = Stream.of(claimperms.values()).filter(perm -> !claim.getPerms().contains(perm)).toList();
        for (claimperms entry: getPage(perms, page, perpage)) {
            if (slot - off * line == 3) {
                line++;
                slot = off * line;
            }
            if (slot >= 36) break;
            if (claim.getPerms().contains(entry)) continue;
            ItemStack stack = entry.getItem().getDefaultInstance();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("§f"+entry.getPermName()));
            container.setItem(slot++, 0, stack);
            permslotids.put(slot, entry);
        }

        List<String> testlist = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            testlist.add("e"+i);
        }
        int ploff = 6;
        int plline = 1;
        int plslot = (9 * plline) + ploff;
        for (claimperms entry : getPage(claim.getPerms(), allowedpage, perpage)) {
            ItemStack stack = entry.getItem().getDefaultInstance();
            if (plslot - ((9 * plline) - 0) == 9) {
                plline++;
                plslot = (9 * plline) + ploff;
                //stack = Items.GREEN_WOOL.getDefaultInstance();
            }
            if (plline >= 4) break;
            //entry.getItem().getDefaultInstance();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("§f"+entry.getPermName()));
            container.setItem(plslot++, 0, stack);
            allowedpermslotids.put(plslot, entry);
        }

        ItemStack backhead = createFtbArrowHead(false);
        backhead.set(DataComponents.CUSTOM_NAME, Component.literal("§y§7Nächste Seite"));

        ItemStack nexthead = createFtbArrowHead(true);
        nexthead.set(DataComponents.CUSTOM_NAME, Component.literal("§y§7Letzte Seite"));

        container.setItem(4, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(13, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(22, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(31, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(42, 0, (allowedpage == 0 ? Items.AIR.getDefaultInstance() : nexthead));
        container.setItem(44, 0, (getPage(claim.getPerms(), allowedpage+1, perpage).isEmpty() ? Items.AIR.getDefaultInstance() : backhead));
        container.setItem(36, 0, (page == 0 ? Items.AIR.getDefaultInstance() : nexthead));
        container.setItem(38, 0, (getPage(Arrays.stream(claimperms.values()).filter(perm -> !claim.getPerms().contains(perm)).toList(), page+1, perpage).isEmpty() ? Items.AIR.getDefaultInstance() : backhead));
        //container.setItem(40, 0,createIcon(Items.GRAY_STAINED_GLASS_PANE, ""));
        container.setItem(7, 0,createIcon(Items.GREEN_TERRACOTTA, "§aErlaubt"));
        container.setItem(1, 0,createIcon(Items.RED_TERRACOTTA, "§cNicht Erlaubt"));
        container.setItem(40, 0,createIcon(Items.ARROW, "§7Zurück"));
    }

    public static ItemStack createFtbArrowHead(boolean left) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        UUID arrowId = new UUID(
                (long) 1355305227 << 32 | (1587563872L & 0xFFFFFFFFL),
                (long) -1097171628 << 32 | (762106198L & 0xFFFFFFFFL)
        );

        UUID leftarrowId = new UUID(
                (long) -1500574876 << 32 | (-1928052736L & 0xFFFFFFFFL),
                (long) -1453372517 << 32 | (-1588621831L & 0xFFFFFFFFL)
        );

        GameProfile profile = new GameProfile(left ? leftarrowId : arrowId, left ? "MHF_ArrowLeft" : "MHF_ArrowRight");

        String value = "ewogICJ0aW1lc3RhbXAiIDogMTc3MTE2MDA0Njk2NCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQyZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==";
        String signature = "Gfv/pm/4WBiMFunZEPV8yTKVtMX866+DWYYb+rgGu3oHz9wrQHZWXZbZJTwvlTwtYFCi7Jox3IPN5Z8VcZ7sRLct0Tmc1wL10v123v8UxW7NJ9VZR43gBFkodsOWusAf4penyCfFJHkrRSD6+krLtAK4+r9h5ODMU2fsNJy/Hot/Iz5KxijlJuTuyDxhMLIElCh/H7dN/mSnVpHF3+ruuVhmdarGCV1YacOzp7oRfYL0CVoD0258nnQwzHJ6BBZJ8h4BrDyxLo2wXtczOLYQrYL99n+bGYuGdtCQuC1s4eYpPjVJiovZyCedD4d0gs9BENx8FoE5i1ERqDwLsJJ8LMzM4O2sapdVV2vxx0y80MIIegMKmPoBb8otIif4iQdfXOtIX5cjiUXqDZnsFhq1CboCMNiKx1vmAX52hCJGE+CNzGIGkS6T9XIM1QJiwkakvwhBQyZeuMosMdzSlBgwzH34aSiYm0iR7GdF+zBXw4dlVekkLpfKZPELzdjiVPpWLbqodNhlXROyUIGr0wgBcJ64z90R+yCAfulvnFobd7DCsutRgMbBWMlXTzqEdveqnkgACPTmoWkuMsjlJ1huj3ZAWZFQDY3MGWdO6wnplrHLqjDCjzJb2Cfw7MwlsIBiOLXe22ZLYXxHDwfNaGNnKnk7VBkawcluvxJTX8Pg1Qk=";

        String leftvalue = "ewogICJ0aW1lc3RhbXAiIDogMTc3MTE2MTMxNDE3OSwKICAicHJvZmlsZUlkIiA6ICJhNjhmMGI2NDhkMTQ0MDAwYTk1ZjRiOWJhMTRmOGRmOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dMZWZ0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y3YWFjYWQxOTNlMjIyNjk3MWVkOTUzMDJkYmE0MzM0MzhiZTQ2NDRmYmFiNWViZjgxODA1NDA2MTY2N2ZiZTIiCiAgICB9CiAgfQp9";
        String leftsignature = "gJYK/yE1jle0oAXnps91c1QwouWZBpUNfuDtACuBCxT1ReGOW+CPYb9H6UhQBUd6SmZRCPToAK1Lpbvo5Vq0nulpxdm6Cc8+qXlBcVubzJEOehtUoU3HJck8wEiS4KkTr7zm145hP7HDK13J5I02lCah30FG3aKg+MQLUaTxqJMY8Jugp63l8IERh+6DuPtT35M2m9EaOtzKIt/wwTiDCEIp9LgA4UIKit4Vz6QCtBiQ/OI/rbcLFWeNVZ52qIFV9Q0aTarZImx7o1boKHy7ZZl2cLyQH8Si1V/ygDCKhuwiqugKkkQuJgwx//U8BAZJR/pXyA0wVDku51V1lZhMM3qrXwrCx31YvtxUZOYtfIM1tCF9amXxhYGEqQSnDTo5DR2br8I2YdQBw+O87sqg8ROBh17rbjiQqj8dblaxjyXjorwVJqy+dEhJ1xvkHz1R+WtBWAREd+RDCXkLPHXZfMi57QIa0I72pxRsIUJOuJ4jcMPc8EHWJ4XC9XEqZDBboKG9iTwNPzIjcfrzKegKxPbBNyCUOOYYMQq9kyiK7ywLDM+ePHyFm/Yk8eZbia4t7tAseIsXD5YKbG7KEGuyl0wt4LI+TRgnPa511fNw63N6vZLWm1/r3+qin8vWVOCwt26G7Gv6NCBjQJ54ZCU81stLwpoSmcdkMICj26dwt4w=";

        profile.getProperties().put("textures", new Property("textures", left ? leftvalue : value, left ? leftsignature : signature));

        head.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        return head;
    }

    public static List<claimperms> getPage(List<claimperms> allRows, int page, int pageSize) {
        int totalItems = allRows.size();

        int startIndex = page * pageSize;

        if (startIndex >= totalItems || startIndex < 0) {
            return Collections.emptyList();
        }

        int endIndex = Math.min(startIndex + pageSize, totalItems);

        return allRows.subList(startIndex, endIndex);
    }

    private static void openAddPlayerMenu(ServerPlayer player, Claim claim) {
        List<ServerPlayer> onlinePlayers = player.getServer().getPlayerList().getPlayers();

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            Map<Integer, String> slotids = new HashMap<>();
            return new ChestMenu(MenuType.GENERIC_9x6, id, inv, new SimpleContainer(54), 6) {
                {
                    int slot = 0;
                    for (ServerPlayer target : onlinePlayers) {
                        if (target.getUUID().equals(claim.getownerUUID()) || claim.getoverridePerms().containsKey(target.getUUID().toString())) continue;
                        if (slot >= 45) break;

                        setItem(slot++, 0,getPlayerHead(player, target.getUUID(), "§aHinzufügen: §f"));
                        slotids.put(slot, target.getUUID().toString());
                    }
                    for (Clan target : SimpleClans.getInstance().getClanManager().getClans()) {
                        if (claim.getoverridePerms().containsKey("." + target.getTag())) continue;
                        if (slot >= 45) break;

                        ItemStack stack = Items.RED_BANNER.getDefaultInstance();
                        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§aHinzufügen: §f" + target.getStringName()));

                        setItem(slot++, 0, stack);
                        slotids.put(slot, "."+target.getTag());
                    }
                    setItem(49, 0,createIcon(Items.ARROW, "§7Zurück"));
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId == 49) { openPlayerPermsMenu((ServerPlayer) player, claim); return; }

                    ItemStack clickedStack = getSlot(slotId).getItem();
                    List<String> names = claim.getoverridePerms().keySet().stream().toList();
                    //openPermsManager((ServerPlayer) player, claim, slotids.get(slotId));

                    try {
                        UUID uuid = UUID.fromString(slotids.get(slotId));
                        claim.addoverridePerms(uuid, claimperms.DROP_ITEM);
                    } catch (Exception e) {
                        claim.addoverridePerms(slotids.get(slotId), claimperms.DROP_ITEM);
                    }

                    player.displayClientMessage(Component.literal("§aClan hinzugefügt!"), true);
                    openPermsManager((ServerPlayer) player, claim, slotids.get(slotId));
                }
            };
        }, Component.literal("Spieler auswählen")));
    }
}
