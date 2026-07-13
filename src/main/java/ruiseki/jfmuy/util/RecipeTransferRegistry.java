package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.inventory.Container;

import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.transfer.BasicRecipeTransferHandler;
import ruiseki.jfmuy.transfer.BasicRecipeTransferInfo;

public class RecipeTransferRegistry implements IRecipeTransferRegistry {

    private final List<IRecipeTransferHandler> recipeTransferHandlers = new ArrayList<>();

    @Override
    public void addRecipeTransferHandler(@Nullable Class<? extends Container> containerClass,
        @Nullable String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart,
        int inventorySlotCount) {
        if (containerClass == null) {
            Log.error("Null containerClass", new NullPointerException());
            return;
        }
        if (recipeCategoryUid == null) {
            Log.error("Null recipeCategoryUid", new NullPointerException());
            return;
        }

        IRecipeTransferInfo recipeTransferHelper = new BasicRecipeTransferInfo(
            containerClass,
            recipeCategoryUid,
            recipeSlotStart,
            recipeSlotCount,
            inventorySlotStart,
            inventorySlotCount);
        addRecipeTransferHandler(recipeTransferHelper);
    }

    @Override
    public void addRecipeTransferHandler(@Nullable IRecipeTransferInfo recipeTransferInfo) {
        if (recipeTransferInfo == null) {
            Log.error("Null recipeTransferInfo", new NullPointerException());
            return;
        }
        IRecipeTransferHandler recipeTransferHandler = new BasicRecipeTransferHandler(recipeTransferInfo);
        addRecipeTransferHandler(recipeTransferHandler);
    }

    @Override
    public void addRecipeTransferHandler(@Nullable IRecipeTransferHandler recipeTransferHandler) {
        if (recipeTransferHandler == null) {
            Log.error("Null recipeTransferHandler", new NullPointerException());
            return;
        }
        this.recipeTransferHandlers.add(recipeTransferHandler);
    }

    public List<IRecipeTransferHandler> getRecipeTransferHandlers() {
        return recipeTransferHandlers;
    }
}
