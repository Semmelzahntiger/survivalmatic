package com.semmelzahntiger.survivalmatic.common.registries;

import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.blocks.SchemBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class Registry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<Block> SCHEM_BUILDER = BLOCKS.register("schematic_builder", () -> new SchemBuilder(
            BlockBehaviour.Properties.of()
    ));
    public static final RegistryObject<BlockEntityType<SchemBuilderBlockEntity>> SCHEM_BUILDER_BE = BLOCK_ENTITIES.register(
            "schematic_builder_block_entity", () -> BlockEntityType.Builder.of(SchemBuilderBlockEntity::new, SCHEM_BUILDER.get()).build(null)
    );
}
