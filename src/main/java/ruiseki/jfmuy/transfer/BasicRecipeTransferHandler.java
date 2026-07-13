package ruiseki.jfmuy.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.RecipeLayout;
import ruiseki.jfmuy.gui.ingredients.GuiIngredient;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackGroup;
import ruiseki.jfmuy.network.packets.PacketRecipeTransfer;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class BasicRecipeTransferHandler implements IRecipeTransferHandler {

    @Nonnull
    private final IRecipeTransferInfo transferHelper;

    public BasicRecipeTransferHandler(@Nonnull IRecipeTransferInfo transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends Container> getContainerClass() {
        return transferHelper.getContainerClass();
    }

    @Override
    public String getRecipeCategoryUid() {
        return transferHelper.getRecipeCategoryUid();
    }

    @Override
    public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout,
        @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        IRecipeTransferHandlerHelper handlerHelper = Internal.getHelpers()
            .recipeTransferHandlerHelper();

        if (!Config.isJfmuyOnServer()) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.no.server");
            return handlerHelper.createUserErrorWithTooltip(message);
        }

        Map<Integer, Slot> inventorySlots = new HashMap<>();
        for (Slot slot : transferHelper.getInventorySlots(container)) {
            inventorySlots.put(slot.slotNumber, slot);
        }

        Map<Integer, Slot> craftingSlots = new HashMap<>();
        for (Slot slot : transferHelper.getRecipeSlots(container)) {
            craftingSlots.put(slot.slotNumber, slot);
        }

        int inputCount = 0;
        GuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        for (GuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients()
            .values()) {
            if (ingredient.isInput() && !ingredient.getAll()
                .isEmpty()) {
                inputCount++;
            }
        }

        if (inputCount > craftingSlots.size()) {
            Log.error(
                "Recipe Transfer helper {} does not work for container {}",
                transferHelper.getClass(),
                container.getClass());
            return handlerHelper.createInternalError();
        }

        Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
        int filledCraftSlotCount = 0;
        int emptySlotCount = 0;

        for (Slot slot : craftingSlots.values()) {
            final ItemStack stack = slot.getStack();
            if (stack != null && stack.stackSize > 0) {
                if (!slot.canTakeStack(player)) {
                    Log.error(
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
            if (stack != null && stack.stackSize > 0) {
                availableItemStacks.put(slot.slotNumber, stack.copy());
            } else {
                emptySlotCount++;
            }
        }

        if (filledCraftSlotCount - inputCount > emptySlotCount) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.inventory.full");
            return handlerHelper.createUserErrorWithTooltip(message);
        }

        RecipeTransferMatching.MatchingItemsResult matchingItemsResult = RecipeTransferMatching
            .getMatchingItems(availableItemStacks, itemStackGroup.getGuiIngredients());

        if (matchingItemsResult.missingItems.size() > 0) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.missing");
            return handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
        }

        List<Integer> craftingSlotIndexes = new ArrayList<>(craftingSlots.keySet());
        Collections.sort(craftingSlotIndexes);

        List<Integer> inventorySlotIndexes = new ArrayList<>(inventorySlots.keySet());
        Collections.sort(inventorySlotIndexes);

        for (Map.Entry<Integer, Integer> entry : matchingItemsResult.matchingItems.entrySet()) {
            int craftNumber = entry.getKey();
            int slotNumber = craftingSlotIndexes.get(craftNumber);
            if (slotNumber < 0 || slotNumber >= container.inventorySlots.size()) {
                Log.error(
                    "Recipes Transfer Helper {} references slot {} outside of the inventory's size {}",
                    transferHelper.getClass(),
                    slotNumber,
                    container.inventorySlots.size());
                return handlerHelper.createInternalError();
            }
        }

        if (doTransfer) {
            PacketRecipeTransfer packet = new PacketRecipeTransfer(
                matchingItemsResult.matchingItems,
                craftingSlotIndexes,
                inventorySlotIndexes,
                maxTransfer,
                transferHelper.requireCompleteSets());
            JFMUY.getProxy()
                .sendPacketToServer(packet);
        }

        return null;
    }
}
