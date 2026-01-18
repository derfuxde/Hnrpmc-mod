package org.emil.hnrpmc.hnessentials.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

public record PlayerDataResponsePayload(String PetUUID, int selected) implements CustomPacketPayload {
    public static final Type<PlayerDataResponsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "player_data_response"));

    public static final StreamCodec<ByteBuf, PlayerDataResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerDataResponsePayload::PetUUID,
            ByteBufCodecs.INT, PlayerDataResponsePayload::selected,
            PlayerDataResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}