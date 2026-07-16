package ruiseki.jfmuy.ingredients;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.ProgressManager;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.startup.IModIdHelper;
import ruiseki.okcore.datastructure.NonNullList;

public final class IngredientListElementFactory {

    private static final IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

    private IngredientListElementFactory() {}

    public static NonNullList<IIngredientListElement> createBaseList(IIngredientRegistry ingredientRegistry,
        IModIdHelper modIdHelper) {
        NonNullList<IIngredientListElement> ingredientListElements = NonNullList.create();

        for (IIngredientType<?> ingredientType : ingredientRegistry.getRegisteredIngredientTypes()) {
            addToBaseList(ingredientListElements, ingredientRegistry, ingredientType, modIdHelper);
        }

        ingredientListElements.sort(IngredientListElementComparator.INSTANCE);
        return ingredientListElements;
    }

    public static <V> NonNullList<IIngredientListElement<V>> createList(IIngredientRegistry ingredientRegistry,
        IIngredientType<V> ingredientType, Collection<V> ingredients, IModIdHelper modIdHelper) {
        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
        IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

        NonNullList<IIngredientListElement<V>> list = NonNullList.create();
        for (V ingredient : ingredients) {
            if (ingredient != null) {
                int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
                IngredientListElement<V> ingredientListElement = IngredientListElement
                    .create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
                if (ingredientListElement != null) {
                    list.add(ingredientListElement);
                }
            }
        }
        return list;
    }

    @Nullable
    public static <V> IIngredientListElement<V> createUnorderedElement(IIngredientRegistry ingredientRegistry,
        IIngredientType<V> ingredientType, V ingredient, IModIdHelper modIdHelper) {
        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
        IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);
        return IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, 0);
    }

    private static <V> void addToBaseList(NonNullList<IIngredientListElement> baseList,
        IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, IModIdHelper modIdHelper) {
        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
        IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

        Collection<V> ingredients = ingredientRegistry.getAllIngredients(ingredientType);
        if (Config.skipShowingProgressBar()) {
            for (V ingredient : ingredients) {
                if (ingredient != null) {
                    int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
                    IngredientListElement<V> ingredientListElement = IngredientListElement
                        .create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
                    if (ingredientListElement != null) {
                        baseList.add(ingredientListElement);
                    }
                }
            }
        } else {
            ProgressManager.ProgressBar progressBar = ProgressManager.push(
                "Registering ingredients: " + ingredientType.getIngredientClass()
                    .getSimpleName(),
                ingredients.size());
            for (V ingredient : ingredients) {
                progressBar.step(ingredientHelper.getDisplayName(ingredient));
                if (ingredient != null) {
                    int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
                    IngredientListElement<V> ingredientListElement = IngredientListElement
                        .create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
                    if (ingredientListElement != null) {
                        baseList.add(ingredientListElement);
                    }
                }
            }
            ProgressManager.pop(progressBar);
        }
    }

}
