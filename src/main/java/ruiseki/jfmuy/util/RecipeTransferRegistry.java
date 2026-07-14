package ruiseki.jfmuy.util;

import javax.annotation.Nullable;

import net.minecraft.inventory.Container;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.transfer.BasicRecipeTransferHandler;
import ruiseki.jfmuy.transfer.BasicRecipeTransferInfo;

public class RecipeTransferRegistry implements IRecipeTransferRegistry {

    private final Table<Class, String, IRecipeTransferHandler> recipeTransferHandlers = HashBasedTable.create();
    private final StackHelper stackHelper;
    private final IRecipeTransferHandlerHelper handlerHelper;

    public RecipeTransferRegistry(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
        this.stackHelper = stackHelper;
        this.handlerHelper = handlerHelper;
    }

    @Override
    public <C extends Container> void addRecipeTransferHandler(@Nullable Class<C> containerClass,
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

        IRecipeTransferInfo<C> recipeTransferHelper = new BasicRecipeTransferInfo<C>(
            containerClass,
            recipeCategoryUid,
            recipeSlotStart,
            recipeSlotCount,
            inventorySlotStart,
            inventorySlotCount);
        addRecipeTransferHandler(recipeTransferHelper);
    }

    @Override
    public <C extends Container> void addRecipeTransferHandler(@Nullable IRecipeTransferInfo<C> recipeTransferInfo) {
        if (recipeTransferInfo == null) {
            Log.error("Null recipeTransferInfo", new NullPointerException());
            return;
        }
        IRecipeTransferHandler<C> recipeTransferHandler = new BasicRecipeTransferHandler<C>(
            stackHelper,
            handlerHelper,
            recipeTransferInfo);
        addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeCategoryUid());
    }

    @Override
    public void addRecipeTransferHandler(@Nullable IRecipeTransferHandler<?> recipeTransferHandler,
        @Nullable String recipeCategoryUid) {
        if (recipeTransferHandler == null) {
            Log.error("Null recipeTransferHandler", new NullPointerException());
            return;
        }
        if (recipeCategoryUid == null) {
            Log.error("Null recipeCategoryUid", new NullPointerException());
            return;
        }
        Class<?> containerClass = recipeTransferHandler.getContainerClass();
        this.recipeTransferHandlers.put(containerClass, recipeCategoryUid, recipeTransferHandler);
    }

    @Override
    public void addUniversalRecipeTransferHandler(@Nullable IRecipeTransferHandler<?> recipeTransferHandler) {
        if (recipeTransferHandler == null) {
            Log.error("Null recipeTransferHandler", new NullPointerException());
            return;
        }
        Class<?> containerClass = recipeTransferHandler.getContainerClass();
        this.recipeTransferHandlers.put(containerClass, Reference.UNIVERSAL_RECIPE_TRANSFER_UID, recipeTransferHandler);
    }

    public ImmutableTable<Class, String, IRecipeTransferHandler> getRecipeTransferHandlers() {
        return ImmutableTable.copyOf(recipeTransferHandlers);
    }
}
