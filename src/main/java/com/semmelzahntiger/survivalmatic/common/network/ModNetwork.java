package com.semmelzahntiger.survivalmatic.common.network;

import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.TransferHeaderPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.TransferPayloadPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.SchemBuilderItemBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.SchemBuilderStatePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferHeaderResponsePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferResultPacket;
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
    public static void clientMessages() {
        INSTANCE.registerMessage(ID++, TransferHeaderPacket.class, TransferHeaderPacket::encode, TransferHeaderPacket::decode, TransferHeaderPacket::handle);
        INSTANCE.registerMessage(ID++, TransferPayloadPacket.class, TransferPayloadPacket::encode, TransferPayloadPacket::decode, TransferPayloadPacket::handle);
    }
    public static void serverMessages() {
        INSTANCE.registerMessage(ID++, TransferHeaderResponsePacket.class, TransferHeaderResponsePacket::encode, TransferHeaderResponsePacket::decode, TransferHeaderResponsePacket::handle);
        INSTANCE.registerMessage(ID++, TransferResultPacket.class, TransferResultPacket::encode, TransferResultPacket::decode, TransferResultPacket::handle);

        INSTANCE.registerMessage(ID++, SchemBuilderStatePacket.class, SchemBuilderStatePacket::encode, SchemBuilderStatePacket::decode, SchemBuilderStatePacket::handle);
        INSTANCE.registerMessage(ID++, SchemBuilderItemBufferPacket.class, SchemBuilderItemBufferPacket::encode, SchemBuilderItemBufferPacket::decode, SchemBuilderItemBufferPacket::handle);
    }
    static {
        clientMessages();
        serverMessages();
    }
}
