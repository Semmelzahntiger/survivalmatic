package com.semmelzahntiger.survivalmatic.client.schem;

import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem.SchematicBytePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem.SchematicHeaderPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem.SchematicPalletePacket;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.print.DocFlavor;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class ServerPayloadManager {
    public static final ServerPayloadManager INSTANCE = new ServerPayloadManager();
    private final HashMap<UUID, SchematicServerPayload> payloads = new HashMap<>();
    private final HashMap<UUID, CompletableFuture<Boolean>> payloadResponses = new HashMap<>();

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
    public Tuple<UUID, CompletableFuture<Boolean>> addPayloadAsync(SchematicReader.ClientInstance instance) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        UUID id = UUID.randomUUID();
        HashMap<Integer, String> palette = instance.paletteMap;
        int[] paletteIDs = palette.keySet().stream().mapToInt(i -> i).toArray();
        String[] paletteBlockStates = palette.values().toArray(new String[0]);
        SchematicServerPayload payload = new SchematicServerPayload(id, instance.name, instance.byteCount, instance.blockData, instance.paletteSize, paletteIDs, paletteBlockStates);
        payloads.put(id, payload);
        payloadResponses.put(id, future);
        return new Tuple<>(id, future);
    }
    private Optional<CompletableFuture<Boolean>> getPayloadResponse(UUID id) {
        return Optional.ofNullable(payloadResponses.get(id));
    }
    public void computeResponse(UUID requestID, boolean response) {
        getPayloadResponse(requestID).ifPresent(future -> future.complete(response));
    }
    public static class SchematicServerPayload {
        public enum PAYLOAD_STATE {
            SENDING_HEADER,
            SENDING_BYTES,
            SENDING_PALETTE,
            COMPLETED
        }
        private PAYLOAD_STATE state = PAYLOAD_STATE.SENDING_BYTES;

        // Max Packet Size of 1MB
        public static final int MAX_BYTES_SENDABLE = 500000;
        public static final int MAX_PALETTE_BYTES_SENDABLE = 500000;

        public final UUID uuid;
        public final String name;
        public final int byteCount;
        public final byte[] data;
        public final int paletteSize;
        public final int[] paletteIDs;
        public final String[] paletteBlockStates;

        public int lastSentByteIndex = 0;
        public int lastSentPaletteIndex = 0;


        public SchematicServerPayload(UUID uuid, String name, int byteCount, byte[] data, int paletteSize, int[] paletteIDs, String[] paletteBlockStates) {
            this.uuid = uuid;
            this.name = name;
            this.byteCount = byteCount;
            this.data = data;
            this.paletteSize = paletteSize;
            this.paletteIDs = paletteIDs;
            this.paletteBlockStates = paletteBlockStates;
        }
        private void tickInternal() {
            this.state = switch (state) {
                case SENDING_HEADER ->  {
                    ModNetwork.INSTANCE.sendToServer(new SchematicHeaderPacket(uuid, paletteSize, byteCount, name, ));
                    yield PAYLOAD_STATE.SENDING_BYTES;
                }
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
                    ModNetwork.INSTANCE.sendToServer(new SchematicPalletePacket(this.uuid, lastPacket, entriesToBeSent,
                            Arrays.copyOfRange(paletteIDs, lastSentPaletteIndex, shiftedIndex), Arrays.copyOfRange(paletteBlockStates, lastSentPaletteIndex, shiftedIndex)));
                    lastSentPaletteIndex = shiftedIndex;
                    yield lastPacket ? PAYLOAD_STATE.COMPLETED : PAYLOAD_STATE.SENDING_PALETTE;
                }
                case COMPLETED -> PAYLOAD_STATE.COMPLETED;
            };
        }
    }
}
