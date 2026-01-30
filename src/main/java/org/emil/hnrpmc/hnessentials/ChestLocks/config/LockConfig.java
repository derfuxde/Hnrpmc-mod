package org.emil.hnrpmc.hnessentials.ChestLocks.config;

import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class LockConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACK_LIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITE_LIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("General");

        BLACK_LIST = builder.comment("Bloecke, die NIEMALS gesperrt werden duerfen (Priority).")
                .defineList("black_list", List.of("minecraft:bedrock"), entry -> true);

        WHITE_LIST = builder.comment("Erlaubte Bloecke oder Tags (mit #). Beispiel: '#minecraft:containers'")
                .defineList("white_list", List.of("#minecraft:containers", "minecraft:barrel"), entry -> true);

        builder.pop();
        SPEC = builder.build();
    }

    public static boolean isLockable(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String idString = id.toString();

        if (BLACK_LIST.get().contains(idString)) {
            return false;
        }

        if (WHITE_LIST.get().contains(idString)) {
            return true;
        }

        return block.builtInRegistryHolder().tags().anyMatch(tag ->
                WHITE_LIST.get().contains("#" + tag.location().toString())
        );
    }

    public static boolean isLockableEntity(EntityType<?> block) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(block);
        String idString = id.toString();

        if (BLACK_LIST.get().contains(idString)) {
            return false;
        }

        if (WHITE_LIST.get().contains(idString)) {
            return true;
        }

        return block.builtInRegistryHolder().tags().anyMatch(tag ->
                WHITE_LIST.get().contains("#" + tag.location().toString())
        );
    }
}