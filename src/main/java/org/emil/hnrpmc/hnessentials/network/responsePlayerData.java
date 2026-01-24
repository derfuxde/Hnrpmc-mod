package org.emil.hnrpmc.hnessentials.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record responsePlayerData(String jsonString) implements CustomPacketPayload {
    public static final Type<responsePlayerData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hnrpmc", "response_player_data"));

    public static final StreamCodec<FriendlyByteBuf, responsePlayerData> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.jsonString()), // Schreiben
            buf -> new responsePlayerData(buf.readUtf())              // Lesen
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public JsonObject getAsJson() {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}