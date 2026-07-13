package ruiseki.jfmuy.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
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

public class PlayerRecipeTransferHandler implements IRecipeTransferHandler {

    private static final Set<Integer> BAD_CRAFT_INDEXES;
    static {
        Set<Integer> bad = new HashSet<>();
        bad.add(2);
        bad.add(5);
        bad.add(6);
        bad.add(7);
        bad.add(8);
        BAD_CRAFT_INDEXES = Collections.unmodifiableSet(bad);
    }

    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferInfo transferHelper;

    public PlayerRecipeTransferHandler(@Nonnull IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
        this.transferHelper = new BasicRecipeTransferInfo(
            ContainerPlayer.class,
            VanillaRecipeCategoryUid.CRAFTING,
            1,
            4,
            9,
            36);
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
        if (!(container instanceof ContainerPlayer)) {
            return handlerHelper.createInternalError();
        }

        if (!Config.isJfmuyOnServer()) {
            String tooltipMessage = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.no.server");
            return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
        }

        Map<Integer, Slot> inventorySlots = new HashMap<>();
        for (Slot slot : transferHelper.getInventorySlots(container)) {
            inventorySlots.put(slot.slotNumber, slot);
        }

        Map<Integer, Slot> craftingSlots = new HashMap<>();
        for (Slot slot : transferHelper.getRecipeSlots(container)) {
            craftingSlots.put(slot.slotNumber, slot);
        }

        GuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        int inputCount = 0;
        {
            int inputIndex = 0;
            for (GuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients()
                .values()) {
                if (ingredient.isInput()) {
                    if (!ingredient.getAll()
                        .isEmpty()) {
                        inputCount++;
                        if (BAD_CRAFT_INDEXES.contains(inputIndex)) {
                            String tooltipMessage = Translator
                                .translateToLocal("jfmuy.tooltip.error.recipe.transfer.too.large.player.inventory");
                            return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
                        }
                    }
                    inputIndex++;
                }
            }
        }

        List<GuiIngredient<ItemStack>> guiIngredients = new ArrayList<>();
        for (GuiIngredient<ItemStack> guiIngredient : itemStackGroup.getGuiIngredients()
            .values()) {
            if (guiIngredient.isInput()) {
                guiIngredients.add(guiIngredient);
            }
        }
        GuiItemStackGroup playerInvItemStackGroup = new GuiItemStackGroup();
        int[] playerGridIndexes = { 0, 1, 3, 4 };
        for (int i = 0; i < 4; i++) {
            int index = playerGridIndexes[i];
            if (index < guiIngredients.size()) {
                GuiIngredient<ItemStack> ingredient = guiIngredients.get(index);
                playerInvItemStackGroup.init(i, true, 0, 0);
                playerInvItemStackGroup.set(i, ingredient.getAll());
            }
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
            .getMatchingItems(availableItemStacks, playerInvItemStackGroup.getGuiIngredients());

        if (matchingItemsResult.missingItems.size() > 0) {
            String message = Translator.translateToLocal("jfmuy.tooltip.error.recipe.transfer.missing");
            matchingItemsResult = RecipeTransferMatching
                .getMatchingItems(availableItemStacks, itemStackGroup.getGuiIngredients());
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
                false);
            JFMUY.getProxy()
                .sendPacketToServer(packet);
        }

        return null;
    }
}
