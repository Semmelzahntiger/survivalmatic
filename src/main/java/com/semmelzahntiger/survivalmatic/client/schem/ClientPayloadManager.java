package com.semmelzahntiger.survivalmatic.client.schem;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.TransferHeaderPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer.TransferPayloadPacket;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.GlobalPos;
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

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientPayloadManager {
    public static final ClientPayloadManager INSTANCE = new ClientPayloadManager();
    private final HashMap<UUID, Tuple<SchematicReader.StreamableSchematicInstance,CompletableFuture<Boolean>>> pendingRequests = new HashMap<>();
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
    public Tuple<CompletableFuture<Boolean>,CompletableFuture<Boolean>> newRequest(SchematicReader.StreamableSchematicInstance instance, GlobalPos blockEntityPos) {
        CompletableFuture<Boolean> requestFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> payloadFuture = new CompletableFuture<>();
        UUID requestUUID = UUID.randomUUID();

        pendingRequests.put(requestUUID,new Tuple<>(instance,requestFuture));
        Survivalmatic.LOGGER.info("Sending Request with UUID '" + requestUUID + "'");
        ModNetwork.INSTANCE.sendToServer(new TransferHeaderPacket(requestUUID, instance.name, instance.dataSize, blockEntityPos));

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

    private void addPayloadAsync(UUID id, SchematicReader.StreamableSchematicInstance instance, CompletableFuture<Boolean> payloadFuture) {
        SchematicServerPayload payload = new SchematicServerPayload(id, instance.dataSize, instance.data);
        payloads.put(id, payload);
        payloadResponses.put(id, payloadFuture);
    }
    private Optional<CompletableFuture<Boolean>> getPendingRequest(UUID id) {
        return Optional.of(pendingRequests.get(id).getB());
    }
    private Optional<CompletableFuture<Boolean>> getPayloadResponse(UUID id) {
        return Optional.ofNullable(payloadResponses.get(id));
    }
    public void computeRequestResponse(UUID id, boolean response) {
        getPendingRequest(id).ifPresent(future -> future.complete(response));
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
            SENDING,
            COMPLETED
        }

        private PAYLOAD_STATE state = PAYLOAD_STATE.SENDING;
        // Max Packet Size of 1MB
        public static final int MAX_BYTES_SENDABLE = 1000000;

        public final UUID requestUUID;
        public final int byteCount;
        public final byte[] data;

        public int lastSentByteIndex = 0;


        public SchematicServerPayload(UUID requestUUID, int byteCount, byte[] data) {
            this.requestUUID = requestUUID;
            this.byteCount = byteCount;
            this.data = data;
        }

        private void tickInternal() {
            this.state = switch (state) {
                case SENDING -> {
                    int bytesToBeSent = Math.min(byteCount - lastSentByteIndex, MAX_BYTES_SENDABLE);
                    boolean isFinalPacket = bytesToBeSent < MAX_BYTES_SENDABLE;
                    ModNetwork.INSTANCE.sendToServer(new TransferPayloadPacket(requestUUID, bytesToBeSent, Arrays.copyOfRange(data, lastSentByteIndex, lastSentByteIndex + bytesToBeSent), isFinalPacket));
                    yield isFinalPacket ? PAYLOAD_STATE.COMPLETED : PAYLOAD_STATE.SENDING;
                }
                case COMPLETED -> PAYLOAD_STATE.COMPLETED;
            };
        }
        public int getCompletedPercentage() {
            return lastSentByteIndex / (byteCount-1) * 100;
        }
    }
}
