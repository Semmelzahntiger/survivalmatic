package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderDumpBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderMaterialBufferPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SchemBuilderInterfaceData {
    public Map<ItemStack, Tuple<Integer, Integer>> materials = new LinkedHashMap<>();
    public Map<ItemStack, Integer> dumpBufferItems = new LinkedHashMap<>();
    public int materialBufferTracker = 0;
    public int dumpBufferTracker = 0;
    private static final SchemBuilderInterfaceData INSTANCE = new SchemBuilderInterfaceData();

    public static SchemBuilderInterfaceData getINSTANCE() {
        return INSTANCE;
    }
    public void computeMaterialBufferPacket(SchemBuilderMaterialBufferPacket packet) {
        materials.clear();
        packet.items.forEach((item, amount) -> materials.put(new ItemStack(item), amount));
        materialBufferTracker++;
    }
    public void computeDumpBufferPacket(SchemBuilderDumpBufferPacket packet) {
        dumpBufferItems.clear();
        packet.items.forEach((item, amount) -> dumpBufferItems.put(new ItemStack(item), amount));
        dumpBufferTracker++;
    }
    public Map<ItemStack, Tuple<Integer, Integer>> getMaterials() {
        return materials;
    }
    public Map<ItemStack, Integer> getDumpBufferItems() {
        return dumpBufferItems;
    }
    public int getMaterialBufferTracker() {
        return materialBufferTracker;
    }
    public int getDumpBufferTracker() {
        return dumpBufferTracker;
    }
}
