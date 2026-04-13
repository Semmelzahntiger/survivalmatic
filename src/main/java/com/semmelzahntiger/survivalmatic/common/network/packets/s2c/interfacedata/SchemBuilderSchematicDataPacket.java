package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata;

import com.semmelzahntiger.survivalmatic.client.data.ClientSchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SchemBuilderSchematicDataPacket implements IPacketPayload {
    public final String schematicName;
    public final int width;
    public final int height;
    public final int length;
    public SchemBuilderSchematicDataPacket(String schematicName, int width, int height, int length) {
        this.schematicName = schematicName;
        this.width = width;
        this.height = height;
        this.length = length;
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(schematicName);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(length);
    }
    public static SchemBuilderSchematicDataPacket decode(FriendlyByteBuf buf) {
        return new SchemBuilderSchematicDataPacket(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {
        ClientSchemBuilderInterfaceData.getInstance().computeSchematicDataPacket(this);
    }
}
