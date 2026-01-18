package org.emil.hnrpmc.simpleclans.proxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SDLinkPayload(byte[] data) implements CustomPacketPayload {

    // Dies entspricht deinem Kanalnamen "sdlink:main"
    public static final Type<SDLinkPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("sdlink", "main"));

    // Der Codec schreibt und liest die rohen Bytes
    public static final StreamCodec<FriendlyByteBuf, SDLinkPayload> CODEC = CustomPacketPayload.codec(
            (payload, buffer) -> buffer.writeByteArray(payload.data()),
            buffer -> new SDLinkPayload(buffer.readByteArray())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}