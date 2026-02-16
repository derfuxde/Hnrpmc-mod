package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record NotAfkanymoreRequest(UUID playerUUID) implements CustomPacketPayload {
    public static final Type<NotAfkanymoreRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "notafk"));

    public static final StreamCodec<ByteBuf, NotAfkanymoreRequest> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NotAfkanymoreRequest::playerUUID,
            NotAfkanymoreRequest::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
