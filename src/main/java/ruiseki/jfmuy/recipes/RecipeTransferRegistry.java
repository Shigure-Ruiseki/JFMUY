package ruiseki.jfmuy.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.Container;

import org.apache.commons.lang3.tuple.Pair;

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
    private final List<Pair<String, String>> pendingCopies = new ArrayList<>();
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
        addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeCategoryUid());
    }

    @Override
    public void addRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler, String recipeCategoryUid) {
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

    @Override
    public void copyRecipeTransferHandlers(String fromRecipeCategoryUid, String toRecipeCategoryUid) {
        ErrorUtil.checkNotNull(fromRecipeCategoryUid, "fromRecipeCategoryUid");
        ErrorUtil.checkNotNull(toRecipeCategoryUid, "toRecipeCategoryUid");
        this.pendingCopies.add(Pair.of(fromRecipeCategoryUid, toRecipeCategoryUid));
    }

    @SuppressWarnings("rawtypes")
    private void executePendingCopies() {
        ImmutableTable<Class, String, IRecipeTransferHandler> snapshot = this.recipeTransferHandlers.toImmutable();

        for (Pair<String, String> pair : pendingCopies) {
            String fromUid = pair.getLeft();
            String toUid = pair.getRight();

            Map<Class, IRecipeTransferHandler> srcHandlers = snapshot.column(fromUid);
            for (Map.Entry<Class, IRecipeTransferHandler> entry : srcHandlers.entrySet()) {
                Class<?> containerClass = entry.getKey();
                if (Container.class.isAssignableFrom(containerClass)) {
                    this.recipeTransferHandlers.put(containerClass, toUid, entry.getValue());
                }
            }
        }
        pendingCopies.clear();
    }

    @SuppressWarnings("rawtypes")
    public ImmutableTable<Class, String, IRecipeTransferHandler> getRecipeTransferHandlers() {
        if (!pendingCopies.isEmpty()) executePendingCopies();
        return recipeTransferHandlers.toImmutable();
    }
}
