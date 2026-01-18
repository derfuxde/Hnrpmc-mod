package org.emil.hnrpmc.hnessentials.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SaveSkinPayload(String PetUUID, int skinint) implements CustomPacketPayload {
    public static final Type<SaveSkinPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "save_skin"));

    public static final StreamCodec<ByteBuf, SaveSkinPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SaveSkinPayload::PetUUID,
            ByteBufCodecs.INT, SaveSkinPayload::skinint,
            SaveSkinPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}