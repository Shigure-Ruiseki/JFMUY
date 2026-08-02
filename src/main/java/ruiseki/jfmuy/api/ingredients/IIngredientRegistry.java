package ruiseki.jfmuy.api.ingredients;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.recipe.IIngredientType;

/**
 * The IIngredientRegistry is provided by JFMUY and has some useful functions related to recipe ingredients.
 * Get the instance from {@link IModRegistry#getIngredientRegistry()}.
 */
public interface IIngredientRegistry {

    /**
     * Returns an unmodifiable collection of all the ingredients known to JFMUY, of the specified type.
     */
    <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType);

    <V> V getIngredientByUid(IIngredientType<V> ingredientType, String uid);

    /**
     * Returns the appropriate ingredient helper for this ingredient.
     */
    <V> IIngredientHelper<V> getIngredientHelper(V ingredient);

    /**
     * Returns the appropriate ingredient helper for this ingredient type.
     */
    <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType);

    /**
     * Returns the ingredient renderer for this ingredient.
     */
    <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient);

    /**
     * Returns the ingredient renderer for this ingredient class.
     */
    <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType);

    /**
     * Returns an unmodifiable collection of all registered ingredient types.
     * Without addons, there are {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}.
     */
    Collection<IIngredientType> getRegisteredIngredientTypes();

    /**
     * Returns an unmodifiable list of all the ItemStacks that can be used as fuel in a vanilla furnace.
     */
    List<ItemStack> getFuels();

    /**
     * Returns an unmodifiable list of all the ItemStacks that return true to isPotionIngredient.
     */
    List<ItemStack> getPotionIngredients();

    /**
     * Add new ingredients to JFMUY at runtime.
     * Used by mods that have items created while the game is running, or use the server to define items.
     */
    <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

    /**
     * Remove ingredients from JFMUY at runtime.
     * Used by mods that have items created while the game is running, or use the server to define items.
     */
    <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients);

    /**
     * Helper method to get ingredient type for an ingredient.
     */
    <V> IIngredientType<V> getIngredientType(V ingredient);

    /**
     * Helper method to get ingredient type from a legacy ingredient class.
     */
    <V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass);

    /**
     * Returns the ingredient types that plugins have marked as craftable.
     * <p>
     * Types opt in with {@link IModIngredientRegistration#markAsCraftable(IIngredientType)}.
     * Only these types can be hashed, favourited, and autocrafted.
     */
    default Set<IIngredientType<?>> getCraftableIngredientTypes() {
        return Collections.emptySet();
    }

    /**
     * Returns true if the given ingredient's type has been marked craftable.
     */
    default boolean isIngredientCraftable(Object ingredient) {
        return false;
    }
}
