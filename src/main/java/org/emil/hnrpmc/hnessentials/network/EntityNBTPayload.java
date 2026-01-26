package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EntityNBTPayload(int entityId, CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<EntityNBTPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("meinmod", "entity_nbt"));

    public static final StreamCodec<ByteBuf, EntityNBTPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EntityNBTPayload::entityId,
            ByteBufCodecs.COMPOUND_TAG, EntityNBTPayload::nbt,
            EntityNBTPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}