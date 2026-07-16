package ruiseki.jfmuy.api.gui;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

/**
 * Represents the layout of one recipe on-screen.
 * Plugins interpret a recipe wrapper to set the properties here.
 * It is passed to plugins in {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}.
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
     * This method is for handling custom item types, registered with {@link IModIngredientRegistration}.
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
     * By default, the recipe transfer button is at the bottom, to the right of the recipe.
     * If it doesn't fit there, you can use this to move it when you init the recipe layout.
     * This calls {@link IRecipeLayout#setRecipeTransferButton(int, int, boolean)} with moveAll = true.
     */
    void setRecipeTransferButton(int posX, int posY);

    /**
     * Moves the recipe transfer button's position relative to the recipe layout.
     * If moveAll is true, it also moves the recipe favorite button and recipe bookmark button to be to the right of the
     * recipe transfer button.
     */
    void setRecipeTransferButton(int posX, int posY, boolean moveAll);

    /**
     * Sets the recipe favourite button's position
     */
    void setRecipeFavoriteButton(int posX, int posY);

    /**
     * Sets the recipe bookmark button's position
     */
    void setRecipeBookmarkButton(int posX, int posY);

    /**
     * Adds a shapeless icon to the top right of the recipe, that shows a tooltip saying "shapeless" when hovered over.
     */
    void setShapeless();
}
