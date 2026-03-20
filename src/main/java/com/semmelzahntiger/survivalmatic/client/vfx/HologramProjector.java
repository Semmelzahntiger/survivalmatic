package com.semmelzahntiger.survivalmatic.client.vfx;

import com.mojang.blaze3d.vertex.*;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class HologramProjector {
    public static HologramProjector INSTANCE = null;
    public static Optional<HologramProjector> getInstance() {
        return Optional.of(INSTANCE);
    }
    public static void createInstance() {
        INSTANCE = new HologramProjector();
    }
    public static void invalidateInstance() {
        INSTANCE = null;
    }
    public HashMap<UUID, Hologram> holograms = new HashMap<>();

    public static void renderEvent(RenderLevelStageEvent event) {
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder builder = new BufferBuilder(2000000);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    public void projectHolograms() {

    }
    public static class Hologram {
        public VertexBuffer buffer;
        public BlockPos offset;
        public final int width;
        public final int height;
        public final int length;
        public final HashMap<Integer, BlockState> blockStates;


        public Hologram(BlockPos pos, int width, int height, int length, HashMap<Integer, BlockState> blockStates) {
            this.offset = pos;
            this.width = width;
            this.height = height;
            this.length = length;
            this.blockStates = blockStates;
        }
        public void calculateHologramMesh() {
            Minecraft mc = Minecraft.getInstance();
            BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
            this.buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            PoseStack poseStack = new PoseStack();
            RandomSource random = RandomSource.create();
            BufferBuilder bufferBuilder = new BufferBuilder(2000000);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState blockState = blockStates.get(SchematicReader.to1D(x,y,z, width, length));
                        dispatcher.renderBatched(blockState, pos, mc.level, poseStack, bufferBuilder, true, random, ModelData.EMPTY, RenderType.translucent());
                    }
                }
            }

        }
        public void updateOffset(BlockPos offset) {
            this.offset = offset;
        }

    }
}
