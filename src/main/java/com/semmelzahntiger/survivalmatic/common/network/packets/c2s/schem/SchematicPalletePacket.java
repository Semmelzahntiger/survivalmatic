package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;
import java.util.UUID;

public class SchematicPalletePacket {
    public final UUID requestUUID;
    public final boolean finalPalettePacket;
    public final int payloadSize;
    public final int[] paletteIDs;
    public final String[] paletteBlockStates;

    public SchematicPalletePacket(UUID requestUUID, boolean finalPalettePacket , int payloadSize, int[] paletteIDs, String[] paletteBlockStates) {
        this.requestUUID = requestUUID;
        this.finalPalettePacket = finalPalettePacket;
        this.payloadSize = payloadSize;
        this.paletteIDs = paletteIDs;
        this.paletteBlockStates = paletteBlockStates;
    }
    public static void encode(SchematicPalletePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.requestUUID);
        buffer.writeBoolean(packet.finalPalettePacket);

    }
}
