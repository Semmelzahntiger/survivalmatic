package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SchematicResponsePacket implements IPacketPayload {
    private final boolean success;

    public SchematicResponsePacket(boolean success) {
        this.success = success;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(success);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }
    public static SchematicResponsePacket decode(FriendlyByteBuf buf) {
        return new SchematicResponsePacket(buf.readBoolean());
    }
}
