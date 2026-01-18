package org.emil.hnrpmc.simpleclans.proxy.dto;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BungeePayload(byte[] data) implements CustomPacketPayload {
    // Verwende exakt diesen ResourceLocation, da BungeeCord darauf hört
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bungeecord", "main");
    public static final Type<BungeePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, BungeePayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.data),
            buf -> {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                return new BungeePayload(bytes);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}