package com.semmelzahntiger.survivalmatic.common.network;

import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.TransferHeaderPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.TransferPayloadPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderDumpBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderMaterialBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata.HologramUpdatePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata.InvalidateHologramPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata.NewHologramPacket;
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

        INSTANCE.registerMessage(ID++, SchemBuilderMaterialBufferPacket.class, SchemBuilderMaterialBufferPacket::encode, SchemBuilderMaterialBufferPacket::decode, SchemBuilderMaterialBufferPacket::handle);
        INSTANCE.registerMessage(ID++, SchemBuilderDumpBufferPacket.class, SchemBuilderDumpBufferPacket::encode, SchemBuilderDumpBufferPacket::decode, SchemBuilderDumpBufferPacket::handle);

        INSTANCE.registerMessage(ID++, NewHologramPacket.class, NewHologramPacket::encode, NewHologramPacket::decode, NewHologramPacket::handle);
        INSTANCE.registerMessage(ID++, HologramUpdatePacket.class, HologramUpdatePacket::encode, HologramUpdatePacket::decode, HologramUpdatePacket::handle);
        INSTANCE.registerMessage(ID++, InvalidateHologramPacket.class, InvalidateHologramPacket::encode, InvalidateHologramPacket::decode, InvalidateHologramPacket::handle);
    }
    public static void init() {
        clientMessages();
        serverMessages();
    }
}
