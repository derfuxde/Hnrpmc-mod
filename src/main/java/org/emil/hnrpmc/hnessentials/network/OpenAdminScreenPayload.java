package org.emil.hnrpmc.hnessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.emil.hnrpmc.hnessentials.HNPlayerData;

import java.util.UUID;

public record OpenAdminScreenPayload(UUID targetUUID, String targetName, String jsonData, boolean vip) implements CustomPacketPayload {
    public static final Type<OpenAdminScreenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "open_admin_gui"));

    public static final StreamCodec<ByteBuf, OpenAdminScreenPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, OpenAdminScreenPayload::targetUUID,
            ByteBufCodecs.STRING_UTF8, OpenAdminScreenPayload::targetName,
            ByteBufCodecs.STRING_UTF8, OpenAdminScreenPayload::jsonData,
            ByteBufCodecs.BOOL, OpenAdminScreenPayload::vip,
            OpenAdminScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}