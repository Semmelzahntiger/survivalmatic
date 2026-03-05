package com.semmelzahntiger.survivalmatic.common.network.packets.s2c;


import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SchemBuilderItemBufferPacket {
    public final Map<Item, Integer> materialBufferItems;
    public final Map<Item, Integer> dumpBufferItems;

    public SchemBuilderItemBufferPacket(Map<Item, Integer> materialBufferItems, Map<Item, Integer> dumpBufferItems) {
        this.materialBufferItems = materialBufferItems;
        this.dumpBufferItems = dumpBufferItems;
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(materialBufferItems.size());
        for (Map.Entry<Item, Integer> itemIntegerEntry : materialBufferItems.entrySet()) {
            buf.writeId(BuiltInRegistries.ITEM, itemIntegerEntry.getKey());
            buf.writeInt(itemIntegerEntry.getValue());
        }
        buf.writeInt(dumpBufferItems.size());
        for (Map.Entry<Item, Integer> itemIntegerEntry : dumpBufferItems.entrySet()) {
            buf.writeId(BuiltInRegistries.ITEM, itemIntegerEntry.getKey());
            buf.writeInt(itemIntegerEntry.getValue());
        }
    }
    public static SchemBuilderItemBufferPacket decode(FriendlyByteBuf buf) {
        Map<Item, Integer> materialBufferItems = new HashMap<>();
        Map<Item, Integer> dumpBufferItems = new HashMap<>();
        int materialBufferTypes = buf.readInt();
        for (int i = 0; i < materialBufferTypes; i++) {
            materialBufferItems.put(buf.readById(BuiltInRegistries.ITEM), buf.readInt());
        }
        int dumpBufferTypes = buf.readInt();
        for (int i = 0; i < dumpBufferTypes; i++) {
            dumpBufferItems.put(buf.readById(BuiltInRegistries.ITEM), buf.readInt());
        }
        return new SchemBuilderItemBufferPacket(materialBufferItems, dumpBufferItems);
    }
    public static void handle(SchemBuilderItemBufferPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> SchemBuilderInterfaceData.getINSTANCE().computeItemPacket(packet));
        ctx.get().setPacketHandled(true);
    }
}
