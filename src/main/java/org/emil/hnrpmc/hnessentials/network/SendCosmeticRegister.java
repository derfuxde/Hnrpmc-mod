package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SendCosmeticRegister(String cosmetics) implements CustomPacketPayload {
    public static final Type<SendCosmeticRegister> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetic_regist"));

    public static final StreamCodec<ByteBuf, SendCosmeticRegister> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SendCosmeticRegister::cosmetics,
            SendCosmeticRegister::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}