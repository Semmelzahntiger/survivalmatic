package com.example.examplemod.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.io.File;
import java.io.IOException;

public class Importer extends HorizontalDirectionalBlock {
    public Importer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) pLevel;
            MinecraftServer server = serverLevel.getServer();
            File schematic = server.getFile();
            try {
                CompoundTag schematicData = NbtIo.readCompressed(schematic);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
