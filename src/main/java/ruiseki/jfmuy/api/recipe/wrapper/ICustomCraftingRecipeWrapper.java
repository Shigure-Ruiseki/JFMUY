package ruiseki.jfmuy.api.recipe.wrapper;

import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

/**
 * This interface allows recipes to override the default behavior in the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category.
 *
 * @since JEI 3.13.5
 */
public interface ICustomCraftingRecipeWrapper extends IRecipeWrapper {

    /**
     * This is called to override the vanilla crafting category's
     * {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}
     *
     * Note that when this is called, the {@link IGuiItemStackGroup} has already been init with the crafting grid layout
     * for convenience.
     *
     * Set the {@link IRecipeLayout} properties from this {@link IRecipeWrapper} and {@link IIngredients}.
     *
     * @param recipeLayout the layout that needs its properties set.
     * @param ingredients  the ingredients, already set by the recipeWrapper
     * @since JEI 3.13.5
     */
    void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients);
}
