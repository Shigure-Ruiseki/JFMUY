package ruiseki.jfmuy.api.recipe.wrapper;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

/**
 * Implement this interface instead of just {@link IRecipeWrapper} to have your recipe wrapper work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shaped recipe.
 * <p>
 * For shapeless recipes, just use {@link IRecipeWrapper}.
 */
public interface IShapedCraftingRecipeWrapper extends IRecipeWrapper {

    int getWidth();

    int getHeight();

}
