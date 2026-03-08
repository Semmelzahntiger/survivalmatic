package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SchematicPalettePacket implements IPacketPayload {
    public final UUID requestUUID;
    public final boolean finalPalettePacket;
    public final int payloadSize;
    public final int[] paletteIDs;
    public final String[] paletteBlockStates;

    public SchematicPalettePacket(UUID requestUUID, boolean finalPalettePacket , int payloadSize, int[] paletteIDs, String[] paletteBlockStates) {
        this.requestUUID = requestUUID;
        this.finalPalettePacket = finalPalettePacket;
        this.payloadSize = payloadSize;
        this.paletteIDs = paletteIDs;
        this.paletteBlockStates = paletteBlockStates;
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.requestUUID);
        buffer.writeBoolean(this.finalPalettePacket);
        buffer.writeInt(this.payloadSize);
        for (int i = 0; i < this.payloadSize; i++) {
            buffer.writeInt(this.paletteIDs[i]);
            buffer.writeUtf(this.paletteBlockStates[i]);
        }
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }

    public static SchematicPalettePacket decode(FriendlyByteBuf buffer) {
        UUID requestUUID = buffer.readUUID();
        boolean finalPalettePacket = buffer.readBoolean();
        int payloadSize = buffer.readInt();
        int[] paletteIDs = new int[payloadSize];
        String[] paletteBlockStates = new String[payloadSize];
        for (int i = 0; i < payloadSize; i++) {
            paletteIDs[i] = buffer.readInt();
            paletteBlockStates[i] = buffer.readUtf();
        }
        return new SchematicPalettePacket(requestUUID, finalPalettePacket , payloadSize, paletteIDs, paletteBlockStates);
    }
}
