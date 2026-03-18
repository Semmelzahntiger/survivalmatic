package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata;


import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class SchemBuilderItemBufferPacket implements IPacketPayload {
    public final Map<Item, Integer> materialBufferItems;
    public final Map<Item, Integer> dumpBufferItems;
    private Registry<Item> registry;

    // Server side constructor
    public SchemBuilderItemBufferPacket(Map<Item, Integer> materialBufferItems, Map<Item, Integer> dumpBufferItems, Registry<Item> registry) {
        this.materialBufferItems = materialBufferItems;
        this.dumpBufferItems = dumpBufferItems;
        this.registry = registry;
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(materialBufferItems.size());
        for (Map.Entry<Item, Integer> itemIntegerEntry : materialBufferItems.entrySet()) {
            buf.writeId(registry, itemIntegerEntry.getKey());
            buf.writeInt(itemIntegerEntry.getValue());
        }
        buf.writeInt(dumpBufferItems.size());
        for (Map.Entry<Item, Integer> itemIntegerEntry : dumpBufferItems.entrySet()) {
            buf.writeId(registry, itemIntegerEntry.getKey());
            buf.writeInt(itemIntegerEntry.getValue());
        }
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
    }
    public static SchemBuilderItemBufferPacket decode(FriendlyByteBuf buf) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if(connection != null) {
            RegistryAccess access = connection.registryAccess();
            Optional<Registry<Item>> itemRegistry = access.registry(ForgeRegistries.ITEMS.getRegistryKey());
            if(itemRegistry.isPresent()) {
                Registry<Item> registry = itemRegistry.get();
                Map<Item, Integer> materialBufferItems = new HashMap<>();
                Map<Item, Integer> dumpBufferItems = new HashMap<>();
                int materialBufferTypes = buf.readInt();
                for (int i = 0; i < materialBufferTypes; i++) {
                    materialBufferItems.put(buf.readById(registry), buf.readInt());
                }
                int dumpBufferTypes = buf.readInt();
                for (int i = 0; i < dumpBufferTypes; i++) {
                    dumpBufferItems.put(buf.readById(registry), buf.readInt());
                }
                return new SchemBuilderItemBufferPacket(materialBufferItems, dumpBufferItems, null);
            }
            Survivalmatic.LOGGER.error("Couldn't find registry for item registry, returning empty SchemBuilderItemBufferPacket");
        }
        else {
            Survivalmatic.LOGGER.error("Couldn't fetch RegistryAccess from Server as Connection is null, returning empty SchemBuilderItemBufferPacket");
        }
        return new SchemBuilderItemBufferPacket(Map.of(), Map.of(),null);
    }

}
