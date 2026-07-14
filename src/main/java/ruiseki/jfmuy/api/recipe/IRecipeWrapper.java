package ruiseki.jfmuy.api.recipe;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiIngredientGroup;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.ingredients.IIngredients;

/**
 * A wrapper around a normal recipe with methods that allow JFMUY can make sense of it.
 * Plugins implement these to wrap each type of recipe they have.
 * <p>
 * Normal recipes are converted to wrapped recipes by {@link IRecipeHandler#getRecipeWrapper(Object)}.
 *
 * @see BlankRecipeWrapper
 */
public interface IRecipeWrapper {

    /**
     * Gets all the recipe's ingredients by filling out an instance of {@link IIngredients}.
     *
     * @since JFMUY 3.11.0
     */
    void getIngredients(IIngredients ingredients);

    /**
     * Draw additional info about the recipe.
     * Use the mouse position for things like button highlights.
     * Tooltips are handled by {@link IRecipeWrapper#getTooltipStrings(int, int)}
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @see IDrawable for a simple class for drawing things.
     * @see IGuiHelper for useful functions.
     * @since JFMUY 2.19.0
     */
    void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY);

    /**
     * Get the tooltip for whatever's under the mouse.
     * ItemStack and fluid tooltips are already handled by JFMUY, this is for anything else.
     *
     * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
     * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltipStrings(int, int)}
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @return tooltip strings. If there is no tooltip at this position, return null or an empty list.
     */
    @Nullable
    List<String> getTooltipStrings(int mouseX, int mouseY);

    /**
     * Called when a player clicks the recipe.
     * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
     *
     * @param mouseX      the X position of the mouse, relative to the recipe.
     * @param mouseY      the Y position of the mouse, relative to the recipe.
     * @param mouseButton the current mouse event button.
     * @return true if the click was handled, false otherwise
     * @since JEI 2.19.0
     */
    boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton);
}
