package com.semmelzahntiger.survivalmatic.common.network;

import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.SchemBuilderGUIButtonInteract;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.SchemBuilderItemBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.SchemBuilderStatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ModNetwork {
    public static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    static {
        INSTANCE.registerMessage(ID++, SchemBuilderStatePacket.class,
                (packet, encodeByteBuf) -> encodeByteBuf.writeInt(packet.getState()),
                decodeByteBuf -> {
                    int state = decodeByteBuf.readInt();
                    return new SchemBuilderStatePacket(state);
                },
                (decodedPacket, ctx) -> {
                    SchemBuilderInterfaceData.getINSTANCE().setState(decodedPacket.getState());
                    ctx.get().setPacketHandled(true);
        });
        INSTANCE.registerMessage(ID++, SchemBuilderGUIButtonInteract.class,(packet, encodeByteBuf) -> {
                    encodeByteBuf.writeInt(packet.getInteractionID());
                },
                decodeByteBuf -> {
                    int id = decodeByteBuf.readInt();
                    return new SchemBuilderGUIButtonInteract(id);
                },
                (decodedPacket, ctx) -> {
            ctx.get().enqueueWork(() -> {

            });
            ctx.get().setPacketHandled(true);
        });
        INSTANCE.registerMessage(ID++, SchemBuilderItemBufferPacket.class,
                SchemBuilderItemBufferPacket::encode,
                SchemBuilderItemBufferPacket::decode,
                SchemBuilderItemBufferPacket::handle);

    }
}
