package ruiseki.jfmuy.api.recipe;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiIngredientGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.ingredients.IIngredients;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Handles setting up the GUI for its recipe category in {@link #setRecipe(IRecipeLayout, IRecipeWrapper)}.
 * Also draws elements that are common to all recipes in the category like the background.
 *
 * @see BlankRecipeCategory
 */
public interface IRecipeCategory<T extends IRecipeWrapper> {

    /**
     * Returns a unique ID for this recipe category.
     * Referenced from recipes to identify which recipe category they belong to.
     *
     * @see IRecipeHandler#getRecipeCategoryUid(Object)
     * @see VanillaRecipeCategoryUid
     */
    String getUid();

    /**
     * Returns the localized name for this recipe type.
     * Drawn at the top of the recipe GUI pages for this category.
     */
    String getTitle();

    /**
     * Returns the drawable background for a single recipe in this category.
     */
    IDrawable getBackground();

    /**
     * Optional icon for the category tab.
     * If no icon is defined here, JFMUY will use first item registered with
     * {@link IModRegistry#addRecipeCategoryCraftingItem(ItemStack, String...)}
     *
     * @return icon to draw on the category tab, max size is 16x16 pixels.
     * @since 3.13.1
     */
    @Nullable
    IDrawable getIcon();

    /**
     * Draw any extra elements that might be necessary, icons or extra slots.
     *
     * @see IDrawable for a simple class for drawing things.
     * @see IGuiHelper for useful functions.
     */
    void drawExtras(Minecraft minecraft);

    /**
     * Set the {@link IRecipeLayout} properties from the {@link IRecipeWrapper} and {@link IIngredients}.
     *
     * @param recipeLayout  the layout that needs its properties set.
     * @param recipeWrapper the recipeWrapper, for extra information.
     * @param ingredients   the ingredients, already set by the recipeWrapper
     */
    void setRecipe(IRecipeLayout recipeLayout, T recipeWrapper, IIngredients ingredients);

    /**
     * Get the tooltip for whatever's under the mouse.
     * ItemStack and fluid tooltips are already handled by JFMUY, this is for anything else.
     *
     * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
     * To add tooltips for a recipe wrapper, see {@link IRecipeWrapper#getTooltipStrings(int, int)}
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @return tooltip strings. If there is no tooltip at this position, return an empty list.
     * @since JEI 4.2.5, backported to JEI 3.14.6
     */
    List<String> getTooltipStrings(int mouseX, int mouseY);
}
