package ruiseki.jfmuy.api.recipe.wrapper;

/**
 * Implement this interface instead of just {@link ruiseki.jfmuy.api.recipe.IRecipeWrapper} to have your recipe wrapper
 * work as part of the
 * {@link ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid#CRAFTING} recipe category as a shaped recipe.
 * <p>
 * For shapeless recipes, just use {@link ruiseki.jfmuy.api.recipe.IRecipeWrapper}.
 */
public interface IShapedCraftingRecipeWrapper extends ICraftingRecipeWrapper {

    int getWidth();

    int getHeight();

}
