package org.emil.hnrpmc.hnessentials.listeners;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PlayerDataRequestPayload(UUID petUUID) implements CustomPacketPayload {
    // Die "Adresse" des Pakets
    public static final Type<PlayerDataRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "player_data_request"));

    // Der Codec sagt Minecraft, wie man die UUID für das Internet verpackt
    public static final StreamCodec<ByteBuf, PlayerDataRequestPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerDataRequestPayload::petUUID,
            PlayerDataRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}