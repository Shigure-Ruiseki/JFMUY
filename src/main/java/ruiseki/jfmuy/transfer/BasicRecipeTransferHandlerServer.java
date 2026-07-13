package ruiseki.jfmuy.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.util.StackUtil;

public final class BasicRecipeTransferHandlerServer {

    private BasicRecipeTransferHandlerServer() {}

    public static void setItems(EntityPlayer player, Map<Integer, Integer> slotIdMap, List<Integer> craftingSlots,
        List<Integer> inventorySlots, boolean maxTransfer, boolean requireCompleteSets) {
        Container container = player.openContainer;

        Map<Integer, ItemStack> slotMap = new HashMap<>(slotIdMap.size());
        for (Map.Entry<Integer, Integer> entry : slotIdMap.entrySet()) {
            Slot slot = container.getSlot(entry.getValue());
            final ItemStack slotStack = slot.getStack();
            if (slotStack == null || slotStack.stackSize <= 0) {
                return;
            }
            ItemStack stack = slotStack.copy();
            stack.stackSize = 1;
            slotMap.put(entry.getKey(), stack);
        }

        boolean transferAsCompleteSets = requireCompleteSets || !maxTransfer;

        Map<Integer, ItemStack> toTransfer = removeItemsFromInventory(
            player,
            container,
            slotMap,
            craftingSlots,
            inventorySlots,
            transferAsCompleteSets,
            maxTransfer);

        if (toTransfer.isEmpty()) {
            return;
        }

        int minSlotStackLimit = Integer.MAX_VALUE;
        List<ItemStack> clearedCraftingItems = new ArrayList<>();
        for (int craftingSlotNumberIndex = 0; craftingSlotNumberIndex
            < craftingSlots.size(); craftingSlotNumberIndex++) {
            int craftingSlotNumber = craftingSlots.get(craftingSlotNumberIndex);
            Slot craftingSlot = container.getSlot(craftingSlotNumber);
            if (craftingSlot.getHasStack()) {
                ItemStack craftingItem = craftingSlot.decrStackSize(Integer.MAX_VALUE);
                clearedCraftingItems.add(craftingItem);
            }
            if (requireCompleteSets) {
                ItemStack transferItem = toTransfer.get(craftingSlotNumberIndex);
                if (transferItem != null) {
                    int slotStackLimit = craftingSlot.getSlotStackLimit();
                    minSlotStackLimit = Math.min(slotStackLimit, minSlotStackLimit);
                }
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : toTransfer.entrySet()) {
            Integer craftNumber = entry.getKey();
            Integer slotNumber = craftingSlots.get(craftNumber);
            Slot slot = container.getSlot(slotNumber);

            ItemStack stack = entry.getValue();
            if (slot.isItemValid(stack)) {
                if (stack.stackSize > minSlotStackLimit) {
                    ItemStack remainder = stack.splitStack(stack.stackSize - minSlotStackLimit);
                    clearedCraftingItems.add(remainder);
                }
                slot.putStack(stack);
            } else {
                clearedCraftingItems.add(stack);
            }
        }

        for (ItemStack oldCraftingItem : clearedCraftingItems) {
            int added = addStack(container, inventorySlots, oldCraftingItem);
            if (added < oldCraftingItem.stackSize) {
                oldCraftingItem.stackSize -= added;
                if (!player.inventory.addItemStackToInventory(oldCraftingItem)) {
                    player.dropPlayerItemWithRandomChoice(oldCraftingItem, false);
                }
            }
        }

        container.detectAndSendChanges();
    }

    @Nonnull
    private static Map<Integer, ItemStack> removeItemsFromInventory(EntityPlayer player, Container container,
        Map<Integer, ItemStack> required, List<Integer> craftingSlots, List<Integer> inventorySlots,
        boolean transferAsCompleteSets, boolean maxTransfer) {
        final Map<Integer, ItemStack> result = new HashMap<>(required.size());

        loopSets: while (true) {
            Map<Slot, ItemStack> originalSlotContents = null;

            if (transferAsCompleteSets) {
                originalSlotContents = new HashMap<>();
            }

            final Map<Integer, ItemStack> foundItemsInSet = new HashMap<>(required.size());
            boolean noItemsFound = true;

            for (Map.Entry<Integer, ItemStack> entry : required.entrySet()) {
                final ItemStack requiredStack = entry.getValue()
                    .copy();

                final Slot slot = getSlotWithStack(container, requiredStack, craftingSlots, inventorySlots);

                boolean itemFound = (slot != null) && slot.getHasStack() && slot.canTakeStack(player);
                ItemStack resultItemStack = result.get(entry.getKey());
                boolean resultItemStackLimitReached = (resultItemStack != null)
                    && (resultItemStack.stackSize == resultItemStack.getMaxStackSize());

                if (!itemFound || resultItemStackLimitReached) {
                    if (transferAsCompleteSets) {
                        for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
                            ItemStack stack = slotEntry.getValue();
                            slotEntry.getKey()
                                .putStack(stack);
                        }
                        break loopSets;
                    }
                } else {
                    if (originalSlotContents != null && !originalSlotContents.containsKey(slot)) {
                        originalSlotContents.put(
                            slot,
                            slot.getStack()
                                .copy());
                    }

                    ItemStack removedItemStack = slot.decrStackSize(1);
                    foundItemsInSet.put(entry.getKey(), removedItemStack);

                    noItemsFound = false;
                }
            }

            for (Map.Entry<Integer, ItemStack> entry : foundItemsInSet.entrySet()) {
                ItemStack resultItemStack = result.get(entry.getKey());

                if (resultItemStack == null) {
                    result.put(entry.getKey(), entry.getValue());
                } else {
                    resultItemStack.stackSize++;
                }
            }

            if (!maxTransfer || noItemsFound) {
                break;
            }
        }

        return result;
    }

    @Nullable
    private static Slot getSlotWithStack(Container container, ItemStack stack, List<Integer> craftingSlots,
        List<Integer> inventorySlots) {
        Slot slot = getSlotWithStack(container, craftingSlots, stack);
        if (slot == null) {
            slot = getSlotWithStack(container, inventorySlots, stack);
        }
        return slot;
    }

    private static int addStack(Container container, Collection<Integer> slotIndexes, ItemStack stack) {
        int added = 0;
        for (final Integer slotIndex : slotIndexes) {
            if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
                final Slot slot = container.getSlot(slotIndex);
                final ItemStack inventoryStack = slot.getStack();
                if (inventoryStack != null && inventoryStack.isStackable()
                    && StackUtil.isIdentical(inventoryStack, stack)) {

                    final int remain = stack.stackSize - added;
                    final int maxStackSize = Math.min(slot.getSlotStackLimit(), inventoryStack.getMaxStackSize());
                    final int space = maxStackSize - inventoryStack.stackSize;
                    if (space > 0) {
                        if (space >= remain) {
                            inventoryStack.stackSize += remain;
                            return stack.stackSize;
                        }

                        inventoryStack.stackSize = inventoryStack.getMaxStackSize();
                        added += space;
                    }
                }
            }
        }

        if (added >= stack.stackSize) {
            return added;
        }

        for (final Integer slotIndex : slotIndexes) {
            if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
                final Slot slot = container.getSlot(slotIndex);
                final ItemStack inventoryStack = slot.getStack();
                if (inventoryStack == null) {
                    ItemStack stackToAdd = stack.copy();
                    stackToAdd.stackSize = stack.stackSize - added;
                    slot.putStack(stackToAdd);
                    return stack.stackSize;
                }
            }
        }

        return added;
    }

    @Nullable
    private static Slot getSlotWithStack(Container container, Iterable<Integer> slotNumbers, ItemStack itemStack) {
        for (Integer slotNumber : slotNumbers) {
            if (slotNumber >= 0 && slotNumber < container.inventorySlots.size()) {
                Slot slot = container.getSlot(slotNumber);
                ItemStack slotStack = slot.getStack();
                if (StackUtil.isIdentical(itemStack, slotStack)) {
                    return slot;
                }
            }
        }
        return null;
    }
}
