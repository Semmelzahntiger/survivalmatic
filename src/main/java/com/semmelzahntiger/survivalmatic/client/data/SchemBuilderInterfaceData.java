package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderItemBufferPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SchemBuilderInterfaceData {
    public Map<ItemStack, Integer> materialBufferItems = new HashMap<>();
    public Map<ItemStack, Integer> dumpBufferItems = new HashMap<>();
    public int tracker = 0;
    private static final SchemBuilderInterfaceData INSTANCE = new SchemBuilderInterfaceData();

    public static SchemBuilderInterfaceData getINSTANCE() {
        return INSTANCE;
    }
    public void computeItemPacket(SchemBuilderItemBufferPacket packet) {
        materialBufferItems.clear();
        packet.materialBufferItems.forEach((item, amount) -> materialBufferItems.put(new ItemStack(item), amount));
        packet.materialBufferItems.forEach((item, amount) -> dumpBufferItems.put(new ItemStack(item), amount));
        tracker++;
        Survivalmatic.LOGGER.info(materialBufferItems.toString());
        Survivalmatic.LOGGER.info(dumpBufferItems.toString());
    }
    public Map<ItemStack, Integer> getMaterialBufferItems() {
        return materialBufferItems;
    }
    public Map<ItemStack, Integer> getDumpBufferItems() {
        return dumpBufferItems;
    }
    public int getTracker() {
        return tracker;
    }
}
