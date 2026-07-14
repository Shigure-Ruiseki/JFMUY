package ruiseki.jfmuy.api;

import java.util.List;

import ruiseki.jfmuy.api.recipe.IFocus;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJFMUYRuntime#getRecipesGui()}.
 */
public interface IRecipesGui {

    /**
     * Show recipes for an {@link IFocus}.
     * Opens the {@link IRecipesGui} if it is closed.
     *
     * @see IRecipeRegistry#createFocus(IFocus.Mode, Object)
     */
    <V> void show(IFocus<V> focus);

    /**
     * Show entire categories of recipes.
     *
     * @param recipeCategoryUids a list of categories to display, in order. Must not be empty.
     */
    void showCategories(List<String> recipeCategoryUids);
}
