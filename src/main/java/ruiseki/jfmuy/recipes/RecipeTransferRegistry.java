package ruiseki.jfmuy.recipes;

import net.minecraft.inventory.Container;

import com.google.common.collect.ImmutableTable;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.collect.Table;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.transfer.BasicRecipeTransferHandler;
import ruiseki.jfmuy.transfer.BasicRecipeTransferInfo;
import ruiseki.jfmuy.util.ErrorUtil;

public class RecipeTransferRegistry implements IRecipeTransferRegistry {

    private final Table<Class, String, IRecipeTransferHandler> recipeTransferHandlers = Table.hashBasedTable();
    private final StackHelper stackHelper;
    private final IRecipeTransferHandlerHelper handlerHelper;

    public RecipeTransferRegistry(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
        this.stackHelper = stackHelper;
        this.handlerHelper = handlerHelper;
    }

    @Override
    public <C extends Container> void addRecipeTransferHandler(Class<C> containerClass, String recipeCategoryUid,
        int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        ErrorUtil.checkNotNull(containerClass, "containerClass");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

        IRecipeTransferInfo<C> recipeTransferHelper = new BasicRecipeTransferInfo<>(
            containerClass,
            recipeCategoryUid,
            recipeSlotStart,
            recipeSlotCount,
            inventorySlotStart,
            inventorySlotCount);
        addRecipeTransferHandler(recipeTransferHelper);
    }

    @Override
    public <C extends Container> void addRecipeTransferHandlerWithOutput(Class<C> containerClass,
        String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart,
        int inventorySlotCount, int outputSlot) {
        ErrorUtil.checkNotNull(containerClass, "containerClass");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

        IRecipeTransferInfo<C> recipeTransferHelper = new BasicRecipeTransferInfo<>(
            containerClass,
            recipeCategoryUid,
            recipeSlotStart,
            recipeSlotCount,
            inventorySlotStart,
            inventorySlotCount).setCraftingSlot(outputSlot);
        addRecipeTransferHandler(recipeTransferHelper);
    }

    @Override
    public <C extends Container> void addRecipeTransferHandler(IRecipeTransferInfo<C> recipeTransferInfo) {
        ErrorUtil.checkNotNull(recipeTransferInfo, "recipeTransferInfo");

        IRecipeTransferHandler<C> recipeTransferHandler = new BasicRecipeTransferHandler<>(
            stackHelper,
            handlerHelper,
            recipeTransferInfo);
        addRecipeTransferHandlerWithOutput(recipeTransferHandler, recipeTransferInfo.getRecipeCategoryUid());
    }

    @Override
    public void addRecipeTransferHandlerWithOutput(IRecipeTransferHandler<?> recipeTransferHandler,
        String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

        Class<?> containerClass = recipeTransferHandler.getContainerClass();
        this.recipeTransferHandlers.put(containerClass, recipeCategoryUid, recipeTransferHandler);
    }

    @Override
    public void addUniversalRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler) {
        ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");

        Class<?> containerClass = recipeTransferHandler.getContainerClass();
        this.recipeTransferHandlers.put(containerClass, Reference.UNIVERSAL_RECIPE_TRANSFER_UID, recipeTransferHandler);
    }

    public ImmutableTable<Class, String, IRecipeTransferHandler> getRecipeTransferHandlers() {
        return recipeTransferHandlers.toImmutable();
    }
}
