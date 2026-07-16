package ruiseki.jfmuy.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.api.gui.IGuiIngredient;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.jfmuy.config.ServerInfo;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackGroup;
import ruiseki.jfmuy.network.PacketRecipeTransfer;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class PlayerRecipeTransferHandler implements IRecipeTransferHandler<ContainerPlayer> {

    private final StackHelper stackHelper;
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferInfo<ContainerPlayer> transferHelper;

    public PlayerRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.stackHelper = Internal.getStackHelper();
        this.handlerHelper = handlerHelper;
        this.transferHelper = new BasicRecipeTransferInfo<>(
            ContainerPlayer.class,
            VanillaRecipeCategoryUid.CRAFTING,
            1,
            4,
            9,
            36);
    }

    @Override
    public Class<ContainerPlayer> getContainerClass() {
        return transferHelper.getContainerClass();
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerPlayer container, IRecipeLayout recipeLayout,
        EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (!ServerInfo.isJFMUYOnServer()) {
            String tooltipMessage = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.no.server");
            return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
        }

        if (!transferHelper.canHandle(container)) {
            return handlerHelper.createInternalError();
        }

        Map<Integer, Slot> inventorySlots = new HashMap<>();
        for (Slot slot : transferHelper.getInventorySlots(container)) {
            inventorySlots.put(slot.slotNumber, slot);
        }

        Map<Integer, Slot> craftingSlots = new HashMap<>();
        for (Slot slot : transferHelper.getRecipeSlots(container)) {
            craftingSlots.put(slot.slotNumber, slot);
        }

        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        int inputCount = 0;
        {
            // indexes that do not fit into the player crafting grid
            Set<Integer> badIndexes = ImmutableSet.of(2, 5, 6, 7, 8);

            int inputIndex = 0;
            for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients()
                .values()) {
                if (ingredient.isInput()) {
                    if (!ingredient.getAllIngredients()
                        .isEmpty()) {
                        inputCount++;
                        if (badIndexes.contains(inputIndex)) {
                            String tooltipMessage = Translator
                                .translateToLocal("jfmuy.tooltip.error.recipe.transfer.too.large.player.inventory");
                            return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
                        }
                    }
                    inputIndex++;
                }
            }
        }

        // compact the crafting grid into a 2x2 area
        List<IGuiIngredient<ItemStack>> guiIngredients = new ArrayList<>();
        for (IGuiIngredient<ItemStack> guiIngredient : itemStackGroup.getGuiIngredients()
            .values()) {
            if (guiIngredient.isInput()) {
                guiIngredients.add(guiIngredient);
            }
        }
        IGuiItemStackGroup playerInvItemStackGroup = new GuiItemStackGroup(null, 0);
        int[] playerGridIndexes = { 0, 1, 3, 4 };
        for (int i = 0; i < 4; i++) {
            int index = playerGridIndexes[i];
            if (index < guiIngredients.size()) {
                IGuiIngredient<ItemStack> ingredient = guiIngredients.get(index);
                playerInvItemStackGroup.init(i, true, 0, 0);
                playerInvItemStackGroup.set(i, ingredient.getAllIngredients());
            }
        }

        Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
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

        StackHelper.MatchingItemsResult matchingItemsResult = stackHelper
            .getMatchingItems(availableItemStacks, playerInvItemStackGroup.getGuiIngredients());

        if (matchingItemsResult.missingItems.size() > 0) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.missing");
            matchingItemsResult = stackHelper.getMatchingItems(availableItemStacks, itemStackGroup.getGuiIngredients());
            return handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
        }

        List<Integer> craftingSlotIndexes = new ArrayList<>(craftingSlots.keySet());
        Collections.sort(craftingSlotIndexes);

        List<Integer> inventorySlotIndexes = new ArrayList<>(inventorySlots.keySet());
        Collections.sort(inventorySlotIndexes);

        // check that the slots exist and can be altered
        for (Map.Entry<Integer, Integer> entry : matchingItemsResult.matchingItems.entrySet()) {
            int craftNumber = entry.getKey();
            int slotNumber = craftingSlotIndexes.get(craftNumber);
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
            PacketRecipeTransfer packet = new PacketRecipeTransfer(
                matchingItemsResult.matchingItems,
                matchingItemsResult.matchingItemsCasted,
                craftingSlotIndexes,
                inventorySlotIndexes,
                maxTransfer,
                false);
            JFMUY.instance.getPacketHandler()
                .sendToServer(packet);
        }

        return null;
    }
}
