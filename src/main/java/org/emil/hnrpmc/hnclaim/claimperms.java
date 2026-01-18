package org.emil.hnrpmc.hnclaim;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Objects;

public enum claimperms {
    PLACE_BLOCKS("Place blocks", Items.DIRT, "claim.perms.place_blocks", "claim.place_blocks"),
    EXPLODE_BLOCKS("Explode blocks", Items.TNT, "claim.perms.explode_blocks", "claim.explode_blocks"),
    BREAK_BLOCKS("Break blocks", Items.DIAMOND_PICKAXE, "claim.perms.break_blocks", "claim.break_blocks"),
    BLOCK_INTERACTIONS("Block interactions", Items.CHEST, "claim.perms.block_interactions", "claim.block_interactions"),
    REDSTONE_INTERACTIONS("Redstone interactions", Items.REDSTONE, "claim.perms.redstone_interactions", "claim.redstone_interactions"),
    ITEM_INTERACTIONS("Item interactions", Items.SPYGLASS, "claim.perms.item_interactions", "claim.item_interactions"),
    PICKUP_ITEM("Pickup item", Items.SPYGLASS, "claim.perms.pickup_item", "claim.pickup_item"),
    ENTITY_INTERACTIONS("Entity interactions", Items.ARMOR_STAND, "claim.perms.entity_interactions", "claim.entity_interactions"),
    PVP("PvP", Items.DIAMOND_SWORD, "claim.perms.pvp", "claim.pvp"),
    HITENTITYS("Entitys Schlagen", Items.ZOMBIE_HEAD, "claim.perms.hit_entitys", "claim.hit_entitys"),
    DROP_ITEM("Drop item", Items.BARRIER, "claim.perms.drop_item", "claim.drop_item");

    private final String PermName;
    private final String PermLangkey;
    private final Item item;
    private final String permID;

    claimperms(String PermName, Item item, String PermLangkey, String permID) {
        this.PermName = PermName;
        this.item = item;
        this.PermLangkey = PermLangkey;
        this.permID = permID;
    }

    public String getPermName() {
        return PermName;
    }

    public String getPermLangkey() {
        return PermLangkey;
    }

    public Item getItem() {
        return item;
    }

    public String getPermID() {
        return permID;
    }
}
