package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderDumpBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderMaterialBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderSchematicDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientSchemBuilderInterfaceData {
    public Map<ItemStack, Tuple<Integer, Integer>> materials = new LinkedHashMap<>();
    public Map<ItemStack, Integer> dumpBufferItems = new LinkedHashMap<>();
    public int materialBufferTracker = 0;
    public int dumpBufferTracker = 0;

    public String schematicName = "";
    public int width = 0;
    public int height = 0;
    public int length = 0;
    public int schematicDataTracker = 0;


    private static final ClientSchemBuilderInterfaceData INSTANCE = new ClientSchemBuilderInterfaceData();

    public static ClientSchemBuilderInterfaceData getInstance() {
        return INSTANCE;
    }
    // Todo: Abstract away into handlers
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
    public void computeSchematicDataPacket(SchemBuilderSchematicDataPacket packet) {
        schematicName = packet.schematicName;
        width = packet.width;
        height = packet.height;
        length = packet.length;
        schematicDataTracker++;
    }
    public Map<ItemStack, Tuple<Integer, Integer>> getMaterials() {
        return materials;
    }
    public Map<ItemStack, Integer> getDumpBufferItems() {
        return dumpBufferItems;
    }
    public String getSchematicName() {
        return schematicName;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int getLength() {
        return length;
    }
    public int getMaterialBufferTracker() {
        return materialBufferTracker;
    }
    public int getDumpBufferTracker() {
        return dumpBufferTracker;
    }
    public int getSchematicDataTracker() {
        return schematicDataTracker;
    }
}
