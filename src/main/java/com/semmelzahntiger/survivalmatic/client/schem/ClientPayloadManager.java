package com.semmelzahntiger.survivalmatic.client.schem;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.SchematicBytePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.SchematicHeaderPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.SchematicPalettePacket;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientPayloadManager {
    public static final ClientPayloadManager INSTANCE = new ClientPayloadManager();
    private final HashMap<UUID, Tuple<SchematicReader.ClientInstance,CompletableFuture<Boolean>>> pendingRequests = new HashMap<>();
    private final HashMap<UUID, SchematicServerPayload> payloads = new HashMap<>();
    private final HashMap<UUID, CompletableFuture<Boolean>> payloadResponses = new HashMap<>();


    @SubscribeEvent
    public static void tickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientPayloadManager.INSTANCE.tick();
        }
    }

    public void tick() {
        Iterator<Map.Entry<UUID, SchematicServerPayload>> iter = payloads.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, SchematicServerPayload> entry = iter.next();
            SchematicServerPayload payload = entry.getValue();
            if(payload.state != SchematicServerPayload.PAYLOAD_STATE.COMPLETED) {
                payload.tickInternal();
            }
            else {
                iter.remove();
            }
        }
    }

    /**
     *
     * @param instance currently loaded instance
     * @param blockEntityPos Position of blockEntity
     * @return {@link CompletableFuture<Boolean>} containing the Result if the Server accepted the Request and {@link CompletableFuture<Boolean>} containing if the Schematic Transfer was successful
     */
    public Tuple<CompletableFuture<Boolean>,CompletableFuture<Boolean>> newRequest(SchematicReader.ClientInstance instance, BlockPos blockEntityPos) {
        CompletableFuture<Boolean> requestFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> payloadFuture = new CompletableFuture<>();
        UUID requestUUID = UUID.randomUUID();
        pendingRequests.put(requestUUID,new Tuple<>(instance,requestFuture));
        Survivalmatic.LOGGER.info("Sending Request with UUID '" + requestUUID + "'");
        ModNetwork.INSTANCE.sendToServer(new SchematicHeaderPacket(requestUUID, instance.paletteSize, instance.byteCount,instance.name, new short[] {instance.length, instance.height, instance.width }, blockEntityPos));
        requestFuture.orTimeout(20, TimeUnit.SECONDS).thenAccept(request -> {
            pendingRequests.remove(requestUUID);
            if(request) {
                Survivalmatic.LOGGER.info("Request with UUID '" + requestUUID + "' accepted, creating Payload...");
                addPayloadAsync(requestUUID, instance, payloadFuture);
            }
            else {
                Survivalmatic.LOGGER.warn("Request with UUID '" + requestUUID + "' rejected, terminating...");
                payloadFuture.completeExceptionally(new CancellationException());
            }
        }).exceptionally(exception -> {
            if(exception instanceof CancellationException) {
                Survivalmatic.LOGGER.warn("Request with UUID '" + requestUUID + "' cancelled, terminating...");
            }
            else if (exception instanceof TimeoutException) {
                Survivalmatic.LOGGER.warn("Request with UUID '" + requestUUID + "' timed out, terminating...");
            }
            pendingRequests.remove(requestUUID);
            payloadFuture.completeExceptionally(exception);
            return null;
        });
        payloadFuture.thenAccept(value -> {
            Survivalmatic.LOGGER.info("Transfer with UUID '" + requestUUID + "' successful");
            removePayload(requestUUID);

        }).exceptionally(exception -> {
            removePayload(requestUUID);
            return null;
        });
        return new Tuple<>(requestFuture,payloadFuture);
    }

    private void removePayload(UUID requestUUID) {
        pendingRequests.remove(requestUUID);
        payloads.remove(requestUUID);
        payloadResponses.remove(requestUUID);
    }

    private void addPayloadAsync(UUID id, SchematicReader.ClientInstance instance, CompletableFuture<Boolean> payloadFuture) {
        HashMap<Integer, String> palette = instance.paletteMap;
        int[] paletteIDs = palette.keySet().stream().mapToInt(i -> i).toArray();
        String[] paletteBlockStates = palette.values().toArray(new String[0]);
        SchematicServerPayload payload = new SchematicServerPayload(id, instance.byteCount, instance.blockData, instance.paletteSize, paletteIDs, paletteBlockStates);
        payloads.put(id, payload);
        payloadResponses.put(id, payloadFuture);
    }
    private Optional<CompletableFuture<Boolean>> getPendingRequest(UUID id) {
        return Optional.of(pendingRequests.get(id).getB());
    }
    private Optional<CompletableFuture<Boolean>> getPayloadResponse(UUID id) {
        return Optional.ofNullable(payloadResponses.get(id));
    }
    public void computeRequestResponse(UUID id) {
        getPendingRequest(id).ifPresent(future -> future.complete(true));
    }
    public void computePayloadResponse(UUID requestID, boolean response) {
        getPayloadResponse(requestID).ifPresent(future -> future.complete(response));
    }
    public void cancelRequest(UUID requestID) {
        payloads.remove(requestID);
        getPendingRequest(requestID).ifPresent(future -> future.completeExceptionally(new CancellationException()));
        getPayloadResponse(requestID).ifPresent(future -> future.completeExceptionally(new CancellationException()));
    }
    public static class SchematicServerPayload {
        public enum PAYLOAD_STATE {
            SENDING_BYTES,
            SENDING_PALETTE,
            COMPLETED
        }
        private PAYLOAD_STATE state = PAYLOAD_STATE.SENDING_BYTES;

        // Max Packet Size of 1MB
        public static final int MAX_BYTES_SENDABLE = 500000;
        public static final int MAX_PALETTE_BYTES_SENDABLE = 500000;

        public final UUID uuid;
        public final int byteCount;
        public final byte[] data;
        public final int paletteSize;
        public final int[] paletteIDs;
        public final String[] paletteBlockStates;

        public int lastSentByteIndex = 0;
        public int lastSentPaletteIndex = 0;


        public SchematicServerPayload(UUID uuid, int byteCount, byte[] data, int paletteSize, int[] paletteIDs, String[] paletteBlockStates) {
            this.uuid = uuid;
            this.byteCount = byteCount;
            this.data = data;
            this.paletteSize = paletteSize;
            this.paletteIDs = paletteIDs;
            this.paletteBlockStates = paletteBlockStates;
        }
        private void tickInternal() {
            this.state = switch (state) {
                case SENDING_BYTES -> {
                    int indexDifference = byteCount-1 - lastSentByteIndex;
                    if(indexDifference > MAX_BYTES_SENDABLE) {
                        byte[] toBeSent = Arrays.copyOfRange(data, lastSentByteIndex, lastSentByteIndex + MAX_BYTES_SENDABLE);
                        lastSentByteIndex = byteCount + MAX_BYTES_SENDABLE;

                        ModNetwork.INSTANCE.sendToServer(new SchematicBytePacket(this.uuid, toBeSent, false));
                        yield PAYLOAD_STATE.SENDING_BYTES;
                    }
                    else {
                        byte[] toBeSent = Arrays.copyOfRange(data, lastSentByteIndex, lastSentByteIndex + indexDifference);
                        lastSentByteIndex = byteCount;
                        ModNetwork.INSTANCE.sendToServer(new SchematicBytePacket(this.uuid, toBeSent, true));
                        yield PAYLOAD_STATE.SENDING_PALETTE;
                    }
                }
                case SENDING_PALETTE -> {
                    // Init remaining bytes
                    int entriesToBeSent = 0;
                    int bytesLeft = MAX_PALETTE_BYTES_SENDABLE;
                    int shiftedIndex = lastSentPaletteIndex;
                    for (int i = lastSentPaletteIndex; i < paletteSize; i++) {
                        // Calculate byteSize of entry. paletteID fixed at 4 Bytes
                        String blockState = paletteBlockStates[i];
                        int entryTotalAllocatedBytes = blockState.getBytes().length + 4;
                        if(bytesLeft < entryTotalAllocatedBytes) {
                            break;
                        }
                        else {
                            bytesLeft -= entryTotalAllocatedBytes;
                            entriesToBeSent++;
                        }
                        shiftedIndex = i;
                    }
                    boolean lastPacket = lastSentPaletteIndex >= paletteSize-1;
                    ModNetwork.INSTANCE.sendToServer(new SchematicPalettePacket(this.uuid, lastPacket, entriesToBeSent,
                            Arrays.copyOfRange(paletteIDs, lastSentPaletteIndex, shiftedIndex), Arrays.copyOfRange(paletteBlockStates, lastSentPaletteIndex, shiftedIndex)));
                    lastSentPaletteIndex = shiftedIndex;
                    yield lastPacket ? PAYLOAD_STATE.COMPLETED : PAYLOAD_STATE.SENDING_PALETTE;
                }
                case COMPLETED -> PAYLOAD_STATE.COMPLETED;
            };
        }
    }
}
