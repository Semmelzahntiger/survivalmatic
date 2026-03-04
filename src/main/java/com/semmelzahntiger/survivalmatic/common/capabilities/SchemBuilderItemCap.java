package com.semmelzahntiger.survivalmatic.common.capabilities;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemBuilderItemCap implements IItemHandler {
    private final SchemBuilderBlockEntity schemBuilder;
    private List<Item> cachedDumpItems;

    public SchemBuilderItemCap(SchemBuilderBlockEntity schemBuilder) {
        this.schemBuilder = schemBuilder;
    }
    private void validateDumpCache() {
            if(cachedDumpItems == null || schemBuilder.hasDumpBufferChanged()) {
                cachedDumpItems = schemBuilder.getDumpBuffer().keySet().stream().toList();
                schemBuilder.confirmDumpBufferChanged();
            }
    }

    @Override
    public int getSlots() {
        validateDumpCache();
        return cachedDumpItems.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        validateDumpCache();
        try {
            Item item = cachedDumpItems.get(i);
            int amount = schemBuilder.getDumpBuffer().get(item);
            return new ItemStack(item, amount);
        } catch (IndexOutOfBoundsException e) {
            Survivalmatic.LOGGER.error("Tried to read an Item from a Out of Bounds Slot");
            return ItemStack.EMPTY;
        }
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack itemStack, boolean b) {
        int insertAmount = itemStack.getCount();
        Map<Item, Integer> requiredItems = schemBuilder.getSchematicInstance().getRequiredItems();
        int requiredAmount = requiredItems.getOrDefault(itemStack.getItem(), -1);
        // Return in full if not required
        if(requiredAmount < 0) {
            return itemStack;
        }
        HashMap<Item, Integer> currentMaterialBuffer = schemBuilder.getMaterialBuffer();
        int getStoredAmount = currentMaterialBuffer.getOrDefault(itemStack.getItem(), 0);

        if(getStoredAmount >= requiredAmount) {
            return ItemStack.EMPTY;
        }
        int missingAmount = requiredAmount - getStoredAmount;
        if(missingAmount >= insertAmount) {
            return ItemStack.EMPTY;
        }
        itemStack.shrink(missingAmount);
        return itemStack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateDumpCache();
        HashMap<Item, Integer> currentDumpBuffer = schemBuilder.getDumpBuffer();
        try {
            Item item = cachedDumpItems.get(slot);
            if(currentDumpBuffer.containsKey(item)) {
                int amountInDumpBuffer = currentDumpBuffer.get(cachedDumpItems.get(slot));
                int maxStackSizeOfItem = item.getMaxStackSize(new ItemStack(item, 1));
                if(amountInDumpBuffer <= maxStackSizeOfItem) {
                    currentDumpBuffer.remove(item);
                    return new ItemStack(item, amountInDumpBuffer);
                }
                else {
                    currentDumpBuffer.put(item, amountInDumpBuffer - maxStackSizeOfItem);
                    return new ItemStack(item, maxStackSizeOfItem);
                }
            }
            else {
                return ItemStack.EMPTY;
            }
        }
        catch (IndexOutOfBoundsException e) {
            Survivalmatic.LOGGER.error("Tried to extract an Item from a Out of Bounds Slot");
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotLimit(int i) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        return schemBuilder.getSchematicInstance().getRequiredItems().containsKey(itemStack.getItem());
    }

}
