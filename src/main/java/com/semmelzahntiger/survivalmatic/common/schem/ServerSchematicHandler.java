package com.semmelzahntiger.survivalmatic.common.schem;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem.SchematicHeaderPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * Serverside Counterpart to {@link com.semmelzahntiger.survivalmatic.client.schem.ServerPayloadManager ServerPayloadManager}
 */
public class ServerSchematicHandler {
    private static ServerSchematicHandler INSTANCE;
    public static Optional<ServerSchematicHandler> getInstance() {
        if (INSTANCE == null) {
            Survivalmatic.LOGGER.warn("ServerSchematicHandler is not initialized");

        }
        return Optional.of(INSTANCE);
    }
    @ApiStatus.Internal
    public static void createInstance() {
        INSTANCE = new ServerSchematicHandler();
    }
    @ApiStatus.Internal
    public static void voidInstance() {
        INSTANCE = null;
    }

    private final HashMap<UUID, SchematicBuilder> queuedSchematics = new HashMap<>();

    public void addRequest(UUID requestUUID, UUID playerUUID, int paletteSize, int byteCount, String schematicName, short[] dimensions, BlockPos blockEntityPos, ServerLevel level) {
        SchematicBuilder schematicBuilder = new SchematicBuilder(playerUUID, requestUUID, blockEntityPos, dimensions[0], dimensions[1], dimensions[2], byteCount, paletteSize, schematicName, level);
    }

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

        private SchematicBuilder(UUID playerUUID, UUID requestUUID, BlockPos blockEntityPosition,short length, short height, short width, int byteCount, int palleteSize, String schematicName, ServerLevel serverLevel) {
            this.playerUUID = playerUUID;
            level = serverLevel.dimension();
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
                schemBuilderBlockEntity.setSchematicInstance(buildInstance(server));
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
