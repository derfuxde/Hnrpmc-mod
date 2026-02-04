package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RequestScoreData(UUID target) implements CustomPacketPayload {
    public static final Type<RequestScoreData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "requestscoredata"));

    public static final StreamCodec<FriendlyByteBuf, RequestScoreData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestScoreData::target,
            RequestScoreData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}