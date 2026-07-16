package ruiseki.jfmuy.transfer;

import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.api.gui.IGuiIngredient;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.jfmuy.config.ServerInfo;
import ruiseki.jfmuy.network.PacketRecipeTransfer;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class BasicRecipeTransferHandler<C extends Container> implements IRecipeTransferHandler<C> {

    private final StackHelper stackHelper;
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferInfo<C> transferHelper;

    public BasicRecipeTransferHandler(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper,
        IRecipeTransferInfo<C> transferHelper) {
        this.stackHelper = stackHelper;
        this.handlerHelper = handlerHelper;
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<C> getContainerClass() {
        return transferHelper.getContainerClass();
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(C container, IRecipeLayout recipeLayout, EntityPlayer player,
        boolean maxTransfer, boolean doTransfer) {
        if (!ServerInfo.isJFMUYOnServer()) {
            String tooltipMessage = Translator.translateToLocal("jfmuy.y.tooltip.error.recipe.transfer.no.server");
            return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
        }

        if (!transferHelper.canHandle(container)) {
            return handlerHelper.createInternalError();
        }

        Int2ObjectMap<Slot> inventorySlots = new Int2ObjectArrayMap<>();
        for (Slot slot : transferHelper.getInventorySlots(container)) {
            inventorySlots.put(slot.slotNumber, slot);
        }

        Int2ObjectMap<Slot> craftingSlots = new Int2ObjectArrayMap<>();
        for (Slot slot : transferHelper.getRecipeSlots(container)) {
            craftingSlots.put(slot.slotNumber, slot);
        }

        int inputCount = 0;
        boolean stackCrafting = false;
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients()
            .values()) {
            if (ingredient.isInput() && !ingredient.getAllIngredients()
                .isEmpty()) {
                for (ItemStack stack : ingredient.getAllIngredients()) {
                    stackCrafting |= stack.stackSize > 1;
                }
                inputCount++;
            }
        }

        if (inputCount > craftingSlots.size()) {
            Log.get()
                .error(
                    "Recipe Transfer helper {} does not work for container {}. "
                        + "{} ingredients are marked as inputs in IRecipeCategory#setRecipe, but there are only {} crafting slots defined for the recipe transfer helper.",
                    transferHelper.getClass(),
                    container.getClass(),
                    inputCount,
                    craftingSlots.size());
            return handlerHelper.createInternalError();
        }

        Int2ObjectMap<ItemStack> availableItemStacks = new Int2ObjectArrayMap<>();
        int filledCraftSlotCount = 0;
        int emptySlotCount = 0;

        for (Slot slot : craftingSlots.values()) {
            final ItemStack stack = slot.getStack();
            if (stack != null) {
                if (!slot.canTakeStack(player)) {
                    Log.get()
                        .error(
                            "Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}",
                            transferHelper.getClass(),
                            container.getClass(),
                            slot.slotNumber);
                    return handlerHelper.createInternalError();
                }
                filledCraftSlotCount++;
                availableItemStacks.put(slot.slotNumber, stack.copy());
            }
        }

        for (Slot slot : inventorySlots.values()) {
            final ItemStack stack = slot.getStack();
            if (stack != null) {
                availableItemStacks.put(slot.slotNumber, stack.copy());
            } else {
                emptySlotCount++;
            }
        }

        // check if we have enough inventory space to shuffle items around to their final locations
        if (filledCraftSlotCount - inputCount > emptySlotCount) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.inventory.full");
            return handlerHelper.createUserErrorWithTooltip(message);
        }

        StackHelper.MatchingItemsResult matchingItemsResult = stackCrafting
            ? stackHelper.getMatchingItemsWithSensitiveCount(availableItemStacks, itemStackGroup.getGuiIngredients())
            : stackHelper.getMatchingItems(availableItemStacks, itemStackGroup.getGuiIngredients());

        if (!matchingItemsResult.missingItems.isEmpty()) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.missing");
            return handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
        }

        IntList craftingSlotIndexes = new IntArrayList(craftingSlots.keySet());
        Collections.sort(craftingSlotIndexes);

        IntList inventorySlotIndexes = new IntArrayList(inventorySlots.keySet());
        Collections.sort(inventorySlotIndexes);

        // check that the slots exist and can be altered
        for (Int2IntMap.Entry entry : matchingItemsResult.matchingItemsCasted.int2IntEntrySet()) {
            int slotNumber = craftingSlotIndexes.get(entry.getIntKey());
            if (slotNumber < 0 || slotNumber >= container.inventorySlots.size()) {
                Log.get()
                    .error(
                        "Recipes Transfer Helper {} references slot {} outside of the inventory's size {}",
                        transferHelper.getClass(),
                        slotNumber,
                        container.inventorySlots.size());
                return handlerHelper.createInternalError();
            }
        }

        if (doTransfer) {
            PacketRecipeTransfer packet;
            if (stackCrafting) {
                packet = new PacketRecipeTransfer(
                    matchingItemsResult.matchingItemsCasted,
                    craftingSlotIndexes,
                    inventorySlotIndexes,
                    maxTransfer,
                    transferHelper.requireCompleteSets(),
                    ((StackHelper.SensitiveCountMatchingItemsResult) matchingItemsResult).matchingItemsCounts);
            } else {
                packet = new PacketRecipeTransfer(
                    matchingItemsResult.matchingItemsCasted,
                    craftingSlotIndexes,
                    inventorySlotIndexes,
                    maxTransfer,
                    transferHelper.requireCompleteSets());
            }
            JFMUY.instance.getPacketHandler()
                .sendToServer(packet);
        }

        return null;
    }
}
