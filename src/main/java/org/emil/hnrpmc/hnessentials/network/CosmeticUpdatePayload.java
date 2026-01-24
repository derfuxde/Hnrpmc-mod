package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public record CosmeticUpdatePayload(UUID playerUUID, String cosmeticId) implements CustomPacketPayload {
    public static final Type<CosmeticUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetic_update"));

    public static final StreamCodec<ByteBuf, CosmeticUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CosmeticUpdatePayload::playerUUID,
            ByteBufCodecs.STRING_UTF8, CosmeticUpdatePayload::cosmeticId,
            CosmeticUpdatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}