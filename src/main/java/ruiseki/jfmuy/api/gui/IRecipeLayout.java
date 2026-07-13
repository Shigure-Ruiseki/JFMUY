package ruiseki.jfmuy.api.gui;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;

/**
 * Represents the layout of one recipe on-screen.
 * Plugins interpret a recipe wrapper to set the properties here.
 * It is passed to plugins in
 * {@link IRecipeCategory#setRecipe(IRecipeLayout, ruiseki.jfmuy.api.recipe.IRecipeWrapper, ruiseki.jfmuy.api.ingredients.IIngredients)}.
 *
 * @see IRecipeLayoutDrawable
 */
public interface IRecipeLayout {

    /**
     * Contains all the itemStacks displayed on this recipe layout.
     * Init and set them in your recipe category.
     */
    IGuiItemStackGroup getItemStacks();

    /**
     * Contains all the fluidStacks displayed on this recipe layout.
     * Init and set them in your recipe category.
     */
    IGuiFluidStackGroup getFluidStacks();

    /**
     * Get all the ingredients of one type that are displayed on this recipe layout.
     * Init and set them in your recipe category.
     * <p>
     * This method is for handling custom item types, registered with
     * {@link ruiseki.jfmuy.api.ingredients.IModIngredientRegistration}.
     *
     * @see #getItemStacks()
     * @see #getFluidStacks()
     */
    <T> IGuiIngredientGroup<T> getIngredientsGroup(IIngredientType<T> ingredientType);

    /**
     * The current search focus. Set by the player when they look up the recipe. The object being looked up is the
     * focus.
     */
    @Nullable
    IFocus<?> getFocus();

    /**
     * The current recipe category.
     */
    IRecipeCategory<?> getRecipeCategory();

    /**
     * Moves the recipe transfer button's position relative to the recipe layout.
     * By default the recipe transfer button is at the bottom, to the right of the recipe.
     * If it doesn't fit there, you can use this to move it when you init the recipe layout.
     */
    void setRecipeTransferButton(int posX, int posY);

    /**
     * Adds a shapeless icon to the top right of the recipe, that shows a tooltip saying "shapeless" when hovered over.
     */
    void setShapeless();
}
