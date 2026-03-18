package com.semmelzahntiger.survivalmatic.common.blocks;

import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchemBuilder extends HorizontalDirectionalBlock implements EntityBlock {
    public SchemBuilder(Properties pProperties) {
        super(pProperties);
    }
    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            ServerLevel level = (ServerLevel) pLevel;
            GlobalPos pos = GlobalPos.of(pLevel.dimension(), pPos);
            ServerPlayer player = (ServerPlayer) pPlayer;
            SchemBuilderBlockEntity be = (SchemBuilderBlockEntity) level.getBlockEntity(pPos);
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (containerID, playerInventory, serverplayer) -> new SchemBuilderMenu(containerID, playerInventory, be.itemHandler,pos), Component.empty()
            ), buf -> buf.writeGlobalPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SchemBuilderBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> type) {
        return type == ModRegistry.SCHEM_BUILDER_BE.get() ? SchemBuilderBlockEntity::tick : null;
    }

}
