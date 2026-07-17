package ruiseki.jfmuy.transfer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;

public class BasicRecipeTransferInfo<C extends Container> implements IRecipeTransferInfo<C> {

    private final Class<C> containerClass;
    private final String recipeCategoryUid;
    private final int recipeSlotStart;
    private final int recipeSlotCount;
    private final int inventorySlotStart;
    private final int inventorySlotCount;
    private int craftingSlot = -1;

    public BasicRecipeTransferInfo(Class<C> containerClass, String recipeCategoryUid, int recipeSlotStart,
        int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        this.containerClass = containerClass;
        this.recipeCategoryUid = recipeCategoryUid;
        this.recipeSlotStart = recipeSlotStart;
        this.recipeSlotCount = recipeSlotCount;
        this.inventorySlotStart = inventorySlotStart;
        this.inventorySlotCount = inventorySlotCount;
    }

    public BasicRecipeTransferInfo<C> setCraftingSlot(int craftingSlot) {
        this.craftingSlot = craftingSlot;
        return this;
    }

    @Override
    public int getOutputSlot() {
        return craftingSlot;
    }

    @Override
    public Class<C> getContainerClass() {
        return containerClass;
    }

    @Override
    public String getRecipeCategoryUid() {
        return recipeCategoryUid;
    }

    @Override
    public boolean canHandle(C container) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(C container) {
        List<Slot> slots = new ArrayList<Slot>();
        int maxSlots = container.inventorySlots.size();

        for (int i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
            if (i >= 0 && i < maxSlots) {
                slots.add(container.getSlot(i));
            }
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(C container) {
        List<Slot> slots = new ArrayList<Slot>();
        int maxSlots = container.inventorySlots.size();

        for (int i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
            if (i >= 0 && i < maxSlots) {
                slots.add(container.getSlot(i));
            }
        }
        return slots;
    }
}
