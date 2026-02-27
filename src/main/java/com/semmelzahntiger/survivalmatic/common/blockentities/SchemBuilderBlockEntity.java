package com.semmelzahntiger.survivalmatic.common.blockentities;

import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SchemBuilderBlockEntity extends BlockEntity {
    public SchematicReader.SchematicInstance instance = null;
    public SchemBuilderBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

}
