package com.semmelzahntiger.survivalmatic.client.vfx;

import com.mojang.blaze3d.vertex.*;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HologramProjector {
    public static final HologramProjector INSTANCE = new HologramProjector();
    public HashMap<UUID, Hologram> holograms = new HashMap<>();

    @SubscribeEvent
    public static void renderEvent(RenderLevelStageEvent event) {
        if(event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_LEVEL)) {

            HologramProjector projector = INSTANCE;
            projector.projectHolograms(event.getPoseStack(), event.getProjectionMatrix());
        }
    }
    public void addHologram(UUID hologramUUID, Hologram hologram) {
        holograms.put(hologramUUID, hologram);
    }
    public void invalidateHologram(UUID hologramUUID) {
        if(holograms.containsKey(hologramUUID)) {
            holograms.get(hologramUUID).invalidate();
        }
        holograms.remove(hologramUUID);
    }
    public void updateHologram(UUID hologramUUID, GlobalPos globalPos) {
        if(holograms.containsKey(hologramUUID)) {
            holograms.get(hologramUUID).updatePosition(globalPos);
        }
    }

    public void projectHolograms(PoseStack poseStack, Matrix4f proj) {
        for (Hologram value : this.holograms.values()) {
            Level level = Minecraft.getInstance().level;
            if(level == null || level.dimension() != value.worldPos.dimension()) {
                continue;
            }
            ShaderInstance shaderInstance = GameRenderer.getPositionColorTexShader();
            if(shaderInstance == null) {
                continue;
            }
            poseStack.pushPose();
            BlockPos worldPos = value.worldPos.pos();
            poseStack.translate(worldPos.getX(), worldPos.getY(), worldPos.getZ());
            Matrix4f modelView = poseStack.last().pose();
            value.buffer.drawWithShader(modelView, proj, shaderInstance);
            poseStack.popPose();
        }
    }
    public static class Hologram {
        public VertexBuffer buffer;
        public GlobalPos worldPos;
        public final int width;
        public final int height;
        public final int length;
        public final HashMap<Integer, BlockState> blockStates;


        public Hologram(GlobalPos pos, int width, int height, int length, HashMap<Integer, BlockState> blockStates) {
            this.worldPos = pos;
            this.width = width;
            this.height = height;
            this.length = length;
            this.blockStates = blockStates;
        }
        public void load() {
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
            this.buffer.bind();
            this.buffer.upload(bufferBuilder.end());
            VertexBuffer.unbind();
        }
        public void invalidate() {
            this.buffer.close();
            this.buffer = null;
        }
        public void updatePosition(GlobalPos offset) {
            this.worldPos = offset;
        }

    }
}
