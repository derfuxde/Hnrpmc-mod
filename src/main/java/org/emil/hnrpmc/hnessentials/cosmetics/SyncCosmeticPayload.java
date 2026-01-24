package org.emil.hnrpmc.hnessentials.cosmetics;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;

import java.util.UUID;

/**
 * Dieses Paket synchronisiert ein einzelnes Cosmetic eines Spielers vom Server zum Client.
 */
public record SyncCosmeticPayload(UUID playerUUID, CosmeticSlot slot, String cosmeticId) implements CustomPacketPayload {

    // Die eindeutige ID für das Netzwerk-Paket
    public static final Type<SyncCosmeticPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("hnrpmc", "sync_cosmetic")
    );

    // Der StreamCodec definiert, wie die Daten in den Byte-Buffer geschrieben und gelesen werden
    public static final StreamCodec<ByteBuf, SyncCosmeticPayload> STREAM_CODEC = StreamCodec.composite(
            // 1. UUID des betroffenen Spielers
            UUIDUtil.STREAM_CODEC, SyncCosmeticPayload::playerUUID,

            // 2. Der Slot (idMapper nutzt die Enum-Reihenfolge zur Effizienz)
            ByteBufCodecs.idMapper(id -> CosmeticSlot.values()[id], CosmeticSlot::ordinal), SyncCosmeticPayload::slot,

            // 3. Die ID des Cosmetics als String
            ByteBufCodecs.STRING_UTF8, SyncCosmeticPayload::cosmeticId,

            // Factory Methode (Constructor)
            SyncCosmeticPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}