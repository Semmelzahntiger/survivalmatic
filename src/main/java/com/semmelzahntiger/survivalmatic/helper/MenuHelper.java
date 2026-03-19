package com.semmelzahntiger.survivalmatic.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class MenuHelper {
    public static <E extends BlockEntity> Optional<E> getBlockEntity(Level level, BlockPos blockPos, Class<E> blockEntityClass) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity != null && blockEntity.getClass().isInstance(blockEntityClass)) {
            return Optional.of((E) blockEntity);
        }
        return Optional.empty();
    }

}
