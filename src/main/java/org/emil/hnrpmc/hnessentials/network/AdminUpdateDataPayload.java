package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record AdminUpdateDataPayload(UUID target, String field, String value) implements CustomPacketPayload {
    public static final Type<AdminUpdateDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "admin_update"));

    public static final StreamCodec<ByteBuf, AdminUpdateDataPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AdminUpdateDataPayload::target,
            ByteBufCodecs.STRING_UTF8, AdminUpdateDataPayload::field,
            ByteBufCodecs.STRING_UTF8, AdminUpdateDataPayload::value,
            AdminUpdateDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}