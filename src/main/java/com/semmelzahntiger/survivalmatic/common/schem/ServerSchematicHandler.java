package com.semmelzahntiger.survivalmatic.common.schem;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferHeaderResponsePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferResultPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Serverside Counterpart to {@link ClientPayloadManager ServerPayloadManager}</p>
 * <p>Todo: Documentation</p>
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
    // Todo: Add Config for Size restriction
    private void addRequest(UUID requestUUID,String schematicName, String fileExtension, String uploaderName, int byteCount, GlobalPos pos) {
        SchematicBuilder schematicBuilder = new SchematicBuilder(schematicName, fileExtension, uploaderName, byteCount, pos);
        queuedSchematics.put(requestUUID, schematicBuilder);
    }
    public void incomingInitialRequest(UUID requestUUID,String schematicName,String fileExtension, int byteCount, GlobalPos blockEntityPos, MinecraftServer server, UUID playerUUID) {
        ServerPlayer requester = server.getPlayerList().getPlayer(playerUUID);
        ResourceKey<Level> level = blockEntityPos.dimension();
        BlockEntity blockEntity = server.getLevel(level).getBlockEntity(blockEntityPos.pos());

        Survivalmatic.LOGGER.info("Received new request with UUID {} from Player {}.",requestUUID, playerUUID);
        Survivalmatic.LOGGER.info("Schematic Name: {}; Type : .{}; Size: {}; At {}", schematicName, fileExtension, byteCount, blockEntityPos);

        if (blockEntity instanceof SchemBuilderBlockEntity schemBuilder && schemBuilder.newIncomingRequest()) {
            sendHeaderResponse(requestUUID, requester, true);
            addRequest(requestUUID,schematicName,fileExtension, requester.getName().getString(), byteCount, blockEntityPos);
            Survivalmatic.LOGGER.info("Request {} accepted, responding...", requestUUID);
        }
        else {
            sendHeaderResponse(requestUUID, requester, false);
            Survivalmatic.LOGGER.info("Request {} denied, responding...", requestUUID);
        }
    }
    public void incomingByteBatch(UUID requestUUID, byte[] bytes, boolean finalBytePacket, MinecraftServer server, UUID playerUUID) {
        SchematicBuilder schem = queuedSchematics.get(requestUUID);
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if(schem == null) {
            Survivalmatic.LOGGER.warn("Schem not found");
            sendSchematicResponse(requestUUID, player, false);
            return;
        }
        Survivalmatic.LOGGER.info("Received Payload for Request {} ({} Bytes, Final : {}", requestUUID, bytes.length, finalBytePacket);
        schem.addBytes(bytes);
        if(finalBytePacket) {
            if(schem.validatePayload()) {
                Survivalmatic.LOGGER.info("Validation Successful, Building Schematic...");
                schem.buildAndPassToBlockEntity(server);
                sendSchematicResponse(requestUUID, player, true);
            }
            else {
                Survivalmatic.LOGGER.error("Validation failed. Expected {} Bytes, Received {} Bytes", schem.byteCount, schem.bytes.size());
                terminateRequest(requestUUID, player);
            }
        }
    }
    private void sendHeaderResponse(UUID requestUUID, ServerPlayer player, boolean accepted) {
        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransferHeaderResponsePacket(requestUUID,accepted));
    }
    public void sendSchematicResponse(UUID requestUUID, ServerPlayer player, boolean success) {
        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransferResultPacket(requestUUID,success));
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
        private final String name;
        private final String fileExtension;
        private final String uploader;
        private final int byteCount;
        private final List<Byte> bytes = new ArrayList<>();
        private final GlobalPos blockEntityPos;

        private int ticksSinceLastUpdate = 0;

        public SchematicBuilder(String schematicName, String fileExtension, String uploader, int byteCount, GlobalPos blockEntityPos) {
            this.name = schematicName;
            this.fileExtension = fileExtension;
            this.uploader = uploader;
            this.byteCount = byteCount;
            this.blockEntityPos = blockEntityPos;
        }

        public void addBytes(byte[] newBytes) {
            for (byte newByte : newBytes) {
                bytes.add(newByte);
            }
            ticksSinceLastUpdate = 0;
        }
        public void buildAndPassToBlockEntity(MinecraftServer server) {
            HolderLookup.Provider registry = server.registryAccess();
            CompletableFuture<Optional<SchematicReader.SchematicInstance>> schematicFutureOptional = SchematicReader.getSchematicFromBufferAsync(name,new ByteArrayInputStream(ArrayUtils.toPrimitive(bytes.toArray(new Byte[0]))), fileExtension, registry);
            schematicFutureOptional.thenAccept(optional -> optional.ifPresentOrElse(instance -> {
                ResourceKey<Level> level = this.blockEntityPos.dimension();
                BlockPos pos = blockEntityPos.pos();
                server.execute(() -> {
                    BlockEntity blockEntity = server.getLevel(level).getBlockEntity(pos);
                    if(blockEntity instanceof SchemBuilderBlockEntity schemBuilder && schemBuilder.getState() == SchemBuilderBlockEntity.STATE.RECEIVING_DATA) {
                        schemBuilder.setSchematicInstance(instance);
                    }
                    else {
                        Survivalmatic.LOGGER.info("Could not confirm BlockEntity at {} is of Type SchemBuilderBlockEntity or BlockEntity rejects Data", this.blockEntityPos);
                    }
                });
            }, () -> {
                Survivalmatic.LOGGER.info("No SchematicInstance received, cannot set Instance for BlockEntity at {}", this.blockEntityPos);
            })).exceptionally(exception -> {
                Survivalmatic.LOGGER.info("Couldn't compute SchematicData");
                return null;
            });
        }
        public void tick() {
            ticksSinceLastUpdate++;
        }
        public boolean validatePayload() {
            return bytes.size() == byteCount;
        }
        public int getTicksSinceLastUpdate() {
            return ticksSinceLastUpdate;
        }
    }
}
