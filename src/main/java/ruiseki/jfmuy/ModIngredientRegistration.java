package ruiseki.jfmuy;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.util.Log;

public class ModIngredientRegistration implements IModIngredientRegistration {

    private final Map<Class, Collection> allIngredientsMap = new HashMap<>();
    private final Map<Class, IIngredientHelper> ingredientHelperMap = new HashMap<>();
    private final Map<Class, IIngredientRenderer> ingredientRendererMap = new HashMap<>();

    @Override
    public <V> void register(@Nullable Class<V> ingredientClass, @Nullable Collection<V> allIngredients,
        @Nullable IIngredientHelper<V> ingredientHelper, @Nullable IIngredientRenderer<V> ingredientRenderer) {
        if (ingredientClass == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Null ingredientClass", e);
            return;
        }

        if (allIngredients == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Null allIngredients", e);
            return;
        }

        if (ingredientHelper == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Null ingredientHelper", e);
            return;
        }

        if (ingredientRenderer == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Null ingredientRendererFactory", e);
            return;
        }

        allIngredientsMap.put(ingredientClass, allIngredients);
        ingredientHelperMap.put(ingredientClass, ingredientHelper);
        ingredientRendererMap.put(ingredientClass, ingredientRenderer);
    }

    public IngredientRegistry createIngredientRegistry() {
        Map<Class, List> ingredientsMap = new IdentityHashMap<>();
        for (Class ingredientClass : allIngredientsMap.keySet()) {
            Collection ingredients = allIngredientsMap.get(ingredientClass);
            ingredientsMap.put(ingredientClass, Lists.newArrayList(ingredients));
        }

        return new IngredientRegistry(
            ingredientsMap,
            ImmutableMap.copyOf(ingredientHelperMap),
            ImmutableMap.copyOf(ingredientRendererMap));
    }
}
