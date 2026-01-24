package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestSetPayload(String targetUrl, String value) implements CustomPacketPayload {
    public static final Type<RequestSetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "request_set"));

    // Encoder/Decoder für das Netzwerk
    public static final StreamCodec<ByteBuf, RequestSetPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RequestSetPayload::targetUrl,
            ByteBufCodecs.STRING_UTF8, RequestSetPayload::value,
            RequestSetPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}