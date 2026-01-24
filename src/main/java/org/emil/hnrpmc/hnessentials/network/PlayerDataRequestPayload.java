package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Dieses Paket wird vom Client an den Server gesendet,
 * um die Daten eines bestimmten Spielers anzufordern.
 */
public record PlayerDataRequestPayload(UUID target) implements CustomPacketPayload {
    public static final Type<PlayerDataRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("hnrpmc", "player_data_request")
    );

    public static final StreamCodec<ByteBuf, PlayerDataRequestPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerDataRequestPayload::target,
            PlayerDataRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}