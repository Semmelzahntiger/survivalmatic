package com.semmelzahntiger.survivalmatic.common.capabilities;

import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

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
        if(i == 0) return schemBuilder.getInputSlot();
        else if (i == 1) return schemBuilder.getOutputSlot();
        else return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack itemStack, boolean b) {
        if(i != 0) return itemStack;
        ItemStack stack = schemBuilder.getInputSlot().copy();
        if(stack.getItem().equals(itemStack.getItem())) {
            int insertAmount = itemStack.getCount();
            int storedAmount = stack.getCount();
            int maxAmount = stack.getMaxStackSize();
            int insertable = maxAmount - storedAmount;
            if(insertable >= insertAmount) {
                stack.setCount(insertAmount);
                return ItemStack.EMPTY;
            }
            else {
                int sub = insertAmount - insertable;
                stack.setCount(maxAmount);
                ItemStack returned = itemStack.copy();
                returned.setCount(sub);
                return returned;
            }
        }
        else {
            schemBuilder.setInputSlot(itemStack);
            return stack;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(slot == 0) {
            ItemStack inputSlot = schemBuilder.getInputSlot();
            return getReturnedItem(amount, inputSlot);
        }
        else if (slot == 1) {
            ItemStack outputSlot = schemBuilder.getOutputSlot();
            return getReturnedItem(amount, outputSlot);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    private ItemStack getReturnedItem(int amount, ItemStack slotItemStack) {
        if(slotItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int storedAmount = slotItemStack.getCount();
        ItemStack returned = slotItemStack.copy();
        if(amount >= storedAmount) {
            schemBuilder.setInputSlot(ItemStack.EMPTY);
        }
        else {
            returned.setCount(amount);
            slotItemStack.setCount(storedAmount-amount);
        }
        return returned;
    }

    @Override
    public int getSlotLimit(int i) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        if(i != 0) return false;
        SchematicReader.SchematicInstance instance = schemBuilder.getSchematicInstance();
        return instance != null && schemBuilder.getSchematicInstance().requiredItems().containsKey(itemStack.getItem());
    }

}
