package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record requestPlayerData(UUID target) implements CustomPacketPayload {
    public static final Type<requestPlayerData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "requestplayerdata"));

    public static final StreamCodec<FriendlyByteBuf, requestPlayerData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, requestPlayerData::target,
            requestPlayerData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}