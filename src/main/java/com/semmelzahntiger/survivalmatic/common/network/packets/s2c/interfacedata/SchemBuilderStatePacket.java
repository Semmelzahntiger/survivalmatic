package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata;

import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SchemBuilderStatePacket implements IPacketPayload {
    private final int state;
    private final String message;
    public SchemBuilderStatePacket(int state, String message) {
        this.state = state;
        this.message = message;
    }
    public int getState() {
        return state;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(state);
        buf.writeUtf(message);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SchemBuilderInterfaceData.getINSTANCE().setState(state);
        });
        ctx.get().setPacketHandled(true);
    }
    public static SchemBuilderStatePacket decode(FriendlyByteBuf buf) {
        return new SchemBuilderStatePacket(buf.readInt(), buf.readUtf());
    }
}
