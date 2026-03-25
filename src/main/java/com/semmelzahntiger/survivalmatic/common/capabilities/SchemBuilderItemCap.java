package com.semmelzahntiger.survivalmatic.common.capabilities;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchemBuilderItemCap implements IItemHandler {
    private final SchemBuilderBlockEntity schemBuilder;

    public SchemBuilderItemCap(SchemBuilderBlockEntity schemBuilder) {
        this.schemBuilder = schemBuilder;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        Iterator<Map.Entry<Item, Integer>> iter = schemBuilder.getDumpBuffer().entrySet().iterator();
        if(!iter.hasNext()) return ItemStack.EMPTY;
        Map.Entry<Item, Integer> entry = iter.next();
        return new ItemStack(entry.getKey(), Math.min(entry.getKey().getMaxStackSize(), entry.getValue()));
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack itemStack, boolean b) {
        int insertAmount = itemStack.getCount();
        Map<Item, Integer> requiredItems = schemBuilder.getSchematicInstance().requiredItems();
        Map<Item, Integer> currentMaterialBuffer = schemBuilder.getMaterialBuffer();
        int getStoredAmount = currentMaterialBuffer.getOrDefault(itemStack.getItem(), 0);
        int requiredAmount = requiredItems.getOrDefault(itemStack.getItem(), -1);
        int missingAmount = requiredAmount - getStoredAmount;
        // Return in full if not required
        if(requiredAmount < 0) {
            return itemStack;
        }
        if(missingAmount >= insertAmount) {
            currentMaterialBuffer.merge(itemStack.getItem(), insertAmount, Integer::sum);
            schemBuilder.incrementMaterialBufferVersionCounter();
            return ItemStack.EMPTY;
        }
        else {
            int remainder = insertAmount - missingAmount;
            currentMaterialBuffer.merge(itemStack.getItem(), missingAmount, Integer::sum);
            schemBuilder.incrementMaterialBufferVersionCounter();
            return new ItemStack(itemStack.getItem(), remainder);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        Iterator<Map.Entry<Item, Integer>> iter = schemBuilder.getDumpBuffer().entrySet().iterator();
        if(!iter.hasNext()) return ItemStack.EMPTY;
        Map.Entry<Item, Integer> entry = iter.next();
        Item item = entry.getKey();
        Integer storedAmount = entry.getValue();
        int amountToExtract = Math.min(amount, entry.getKey().getMaxStackSize());
        int actualExtract;
        if(storedAmount <= amountToExtract) {
            actualExtract = storedAmount;
            iter.remove();
        }
        else {
            actualExtract = amountToExtract;
            entry.setValue(storedAmount - amountToExtract);
        }
        schemBuilder.incrementDumpBufferVersionCounter();
        return new ItemStack(entry.getKey(), actualExtract);
    }

    @Override
    public int getSlotLimit(int i) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        return schemBuilder.getSchematicInstance().requiredItems().containsKey(itemStack.getItem());
    }

}
