package com.semmelzahntiger.survivalmatic.common.schem;

import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class SchematicReconstructor {
    private final HashMap<UUID, SchematicBuilder> queuedSchematics = new HashMap<>();

    public static class SchematicBuilder {
        private final UUID playerUUID;
        private final ResourceKey<Level> level;
        private final UUID requestUUID;

        private final BlockPos blockEntityPosition;
        private final short length;
        private final short height;
        private final short width;
        private final int byteCount;
        private final int palleteSize;
        private final String schematicName;
        private final HashMap<Integer, String> palette = new HashMap<>();
        private List<Byte> bytes = new ArrayList<>();
        private HashMap<Integer, String> palleteData = new HashMap<>();
        private List<Byte> schematicBytes = new ArrayList<>();

        private SchematicBuilder(UUID playerUUID, UUID requestUUID, BlockPos blockEntityPosition,short length, short height, short width, int byteCount, int palleteSize, String schematicName, MinecraftServer server) {
            this.playerUUID = playerUUID;
            level = server.getPlayerList().getPlayer(playerUUID).level().dimension();
            this.requestUUID = requestUUID;
            this.blockEntityPosition = blockEntityPosition;
            this.length = length;
            this.height = height;
            this.width = width;
            this.byteCount = byteCount;
            this.palleteSize = palleteSize;
            this.schematicName = schematicName;
        }
        public void addBytes(byte[] newBytes) {
            for (byte newByte : newBytes) {
                bytes.add(newByte);
            }
        }
        public boolean validateBytes() {
            return bytes.size() == byteCount;
        }
        public void addPalleteData(Integer palleteID, String blockState) {
            palleteData.put(palleteID, blockState);
        }
        public boolean validatePalleteData() {
            return palleteData.size() == palleteSize;
        }
        public SchematicReader.ServerSchematicInstance buildInstance(MinecraftServer server) {
            return SchematicReader.ServerSchematicInstance.createFromBuilder(this, server);
        }
        public void passInstance(MinecraftServer server) {
            BlockEntity be = Objects.requireNonNull(server.getLevel(level)).getBlockEntity(blockEntityPosition);
            if(be instanceof SchemBuilderBlockEntity schemBuilderBlockEntity) {
                schemBuilderBlockEntity.setSchematicInstance(buildInstance());
            }
        }
        public short getLength() {
            return length;
        }
        public short getHeight() {
            return height;
        }
        public short getWidth() {
            return width;
        }
        public int getByteCount() {
            return byteCount;
        }
        public List<Byte> getBytes() {
            return bytes;
        }
        public int getPalleteSize() {
            return palleteSize;
        }
        public String getSchematicName() {
            return schematicName;
        }
        public HashMap<Integer, String> getPalette() {
            return palette;
        }
    }
}
