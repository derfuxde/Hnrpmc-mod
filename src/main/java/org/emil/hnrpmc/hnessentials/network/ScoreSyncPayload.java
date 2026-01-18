package org.emil.hnrpmc.hnessentials.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

public record ScoreSyncPayload(int scoreValue) implements CustomPacketPayload {
    public static final Type<ScoreSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "score_sync"));

    public static final StreamCodec<ByteBuf, ScoreSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ScoreSyncPayload::scoreValue,
            ScoreSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}