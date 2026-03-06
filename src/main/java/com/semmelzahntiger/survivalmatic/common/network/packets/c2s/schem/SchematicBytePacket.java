package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem;

public class SchematicBytePacket {
    private final byte[] data;
    public SchematicBytePacket(byte[] data) {
        this.data = data;
    }
    public byte[] getData() {
        return data;
    }
}
