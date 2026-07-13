package ruiseki.jfmuy.api.recipe.wrapper;

import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;

/**
 * This interface allows recipes to override the default behavior in the
 * {@link ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid#CRAFTING} recipe category.
 *
 * @since JEI 3.13.5
 */
public interface ICustomCraftingRecipeWrapper extends ICraftingRecipeWrapper {

    /**
     * This is called to override the vanilla crafting category's
     * {@link ruiseki.jfmuy.api.recipe.IRecipeCategory#setRecipe(IRecipeLayout, ruiseki.jfmuy.api.recipe.IRecipeWrapper, IIngredients)}
     *
     * Note that when this is called, the {@link ruiseki.jfmuy.api.gui.IGuiItemStackGroup} has already been init with
     * the crafting grid layout for convenience.
     *
     * Set the {@link IRecipeLayout} properties from this {@link ruiseki.jfmuy.api.recipe.IRecipeWrapper} and
     * {@link IIngredients}.
     *
     * @param recipeLayout the layout that needs its properties set.
     * @param ingredients  the ingredients, already set by the recipeWrapper
     * @since JEI 3.13.5
     */
    void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients);
}
