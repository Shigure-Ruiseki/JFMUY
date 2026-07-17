package ruiseki.jfmuy.api;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.recipe.IFocus;

/**
 * JFMUY's gui for displaying recipes. Use this interface to open recipes.
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

    /**
     * @return the ingredient that's currently under the mouse in this gui, or null if there is none.
     */
    @Nullable
    Object getIngredientUnderMouse();

    /**
     * @return the text of the search filter
     */
    String getSearchFilter();

    /**
     * Set the text of the search filter
     *
     * @return if the search filter was changed as a result
     */
    boolean setSearchFilter(String searchFilter);

    /**
     * @return the search mode being used
     */
    RecipeSearchMode getSearchMode();

    /**
     * Set the search mode being used
     *
     * @return if the search mode was changed as a result
     */
    boolean setSearchMode(RecipeSearchMode searchMode);

    /**
     * What ingredients are being searched by the search filter.
     */
    enum RecipeSearchMode {
        NONE,
        INPUT,
        OUTPUT,
        BOTH;
    }
}
