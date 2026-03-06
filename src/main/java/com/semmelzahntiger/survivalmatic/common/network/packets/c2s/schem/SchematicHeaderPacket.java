package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class SchematicHeaderPacket {

    private final int paletteSize;
    private final int byteCount;
    private final String schematicName;
    private final Vec3i dimensions;

    public SchematicHeaderPacket(int paletteSize, int byteCount, String schematicName, Vec3i dimensions) {
        this.paletteSize = paletteSize;
        this.byteCount = byteCount;
        this.schematicName = schematicName;
        this.dimensions = dimensions;
    }
    public int getPaletteSize() {
        return paletteSize;
    }
    public int getByteCount() {
        return byteCount;
    }
    public String getSchematicName() {
        return schematicName;
    }
    public Vec3i getDimensions() {
        return dimensions;
    }
}
