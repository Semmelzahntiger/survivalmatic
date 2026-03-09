package com.semmelzahntiger.survivalmatic.common.schem;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.SchematicTransferRequestResponsePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.SchematicTransferResponsePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * Serverside Counterpart to {@link ClientPayloadManager ServerPayloadManager}
 * Todo: Add server side saving feature
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

    private void addRequest(UUID requestUUID, UUID playerUUID, int paletteSize, int byteCount, String schematicName, short[] dimensions, BlockPos blockEntityPos, ServerLevel level) {
        SchematicBuilder schematicBuilder = new SchematicBuilder(playerUUID, requestUUID, blockEntityPos, dimensions[0], dimensions[1], dimensions[2], byteCount, paletteSize, schematicName, level);
    }

    /**
     * Called by the {@link com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.SchematicHeaderPacket SchematicHeaderPacket}'s Handler
     * Acts as first handshake before Clients send Schematic Data to Server
     */
    public void incomingInitialRequest(UUID requestUUID, int paletteSize, int byteCount, String schematicName, short[] dimensions, BlockPos blockEntityPos, MinecraftServer server, UUID playerUUID) {
        ServerPlayer requester = server.getPlayerList().getPlayer(requestUUID);
        ServerLevel level = requester.serverLevel();
        BlockEntity blockEntity = level.getBlockEntity(blockEntityPos);
        if (blockEntity instanceof SchemBuilderBlockEntity schemBuilder && schemBuilder.newIncomingRequest()) {
            sendHeaderResponse(requestUUID, requester, true);
            addRequest(requestUUID,playerUUID, paletteSize, byteCount, schematicName, dimensions, blockEntityPos, level);
        }
        else {
            sendHeaderResponse(requestUUID, requester, false);
        }

    }
    private void sendHeaderResponse(UUID requestUUID, ServerPlayer player, boolean accepted) {
        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SchematicTransferRequestResponsePacket(requestUUID,accepted));
    }
    private void sendSchematicResponse(UUID requestUUID, ServerPlayer player, boolean success) {
        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SchematicTransferResponsePacket(requestUUID,success));
    }

    /**
     * Processes {@link com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.SchematicBytePacket SchematicBytePackets} from the Client. If request for does not exist send a failed schematic transfer response. If the BytePacket is marked as the final byte packet but validation fails, terminate and send failed transfer response
     */
    public void incomingByteBatch(UUID requestUUID,  byte[] bytes, boolean finalBytePacket, MinecraftServer server, UUID playerUUID) {
       SchematicBuilder schem =  queuedSchematics.get(requestUUID);
       ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
       if(schem == null) {
           sendSchematicResponse(requestUUID, player, false);
           return;
       }
       schem.addBytes(bytes);
       if(finalBytePacket) {
           if(!schem.validateBytes()) {
               terminateRequest(requestUUID, player);
           }
       }
    }
    /**
     * Processes {@link com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.SchematicPalettePacket SchematicPalettePackets} from the Client. If request for does not exist send a failed schematic transfer response. If final Palette packet arrives and palette cannot be validated, terminate and send failed transfer response.
     * If Validation succeeds, build {@link com.semmelzahntiger.survivalmatic.common.schem.SchematicReader.ServerSchematicInstance ServerSchematicInstance} and pass it to the corresponding BlockEntity
     */
    public void incomingPaletteBatch(UUID requestUUID,int payloadSize, int[] paletteIDs, String[] paletteBlockStates, boolean finalPalettePacket, MinecraftServer server, UUID playerUUID) {
        SchematicBuilder schem =  queuedSchematics.get(requestUUID);
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if(schem == null) {
            sendSchematicResponse(requestUUID, player, false);
            return;
        }
        schem.addPalleteData(payloadSize, paletteIDs, paletteBlockStates);
        if(finalPalettePacket) {
            if(!schem.validatePalleteData()) {
                terminateRequest(requestUUID, player);
            }
            else {
                schem.buildInstanceAndPassToBlockEntity(server);
                sendSchematicResponse(requestUUID, player, true);
                queuedSchematics.remove(requestUUID);
            }
        }
    }
    private void terminateRequest(UUID requestUUID, ServerPlayer player) {
        queuedSchematics.remove(requestUUID);
        sendSchematicResponse(requestUUID, player, false);

    }
    public void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, SchematicBuilder>> iterator = queuedSchematics.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<UUID, SchematicBuilder> entry = iterator.next();
            if(entry.getValue().getTicksSinceLastUpdate() > 600) {
                iterator.remove();
            }
            else {
                entry.getValue().tick();
            }
        }
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

        private int ticksSinceLastUpdate = 0;

        private SchematicBuilder(UUID playerUUID, UUID requestUUID, BlockPos blockEntityPosition, short length, short height, short width, int byteCount, int palleteSize, String schematicName, ServerLevel serverLevel) {
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
            ticksSinceLastUpdate = 0;
        }
        public boolean validateBytes() {
            return bytes.size() == byteCount;
        }
        public void addPalleteData(int size, int[] palleteID, String[] blockState) {
            for (int i = 0; i < size; i++) {
                palette.put(palleteID[i], blockState[i]);
            }
            ticksSinceLastUpdate++;
        }
        public boolean validatePalleteData() {
            return palleteData.size() == palleteSize;
        }
        public SchematicReader.ServerSchematicInstance buildInstance(MinecraftServer server) {
            return SchematicReader.ServerSchematicInstance.createFromBuilder(this, server);
        }
        public void buildInstanceAndPassToBlockEntity(MinecraftServer server) {
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
        public void tick() {
            ticksSinceLastUpdate++;
        }
        public int getTicksSinceLastUpdate() {
            return ticksSinceLastUpdate;
        }
    }
}
