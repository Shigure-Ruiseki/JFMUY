package ruiseki.jfmuy.api;

import ruiseki.jfmuy.api.recipe.transfer.IAutocraftingHandler;

/**
 * Gives access to JFMUY functions that are available once everything has loaded.
 * The IJFMUYRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJFMUYRuntime)} .
 */
public interface IJFMUYRuntime {

    IRecipeRegistry getRecipeRegistry();

    IRecipesGui getRecipesGui();

    IIngredientFilter getIngredientFilter();

    IIngredientListOverlay getIngredientListOverlay();

    IBookmarkOverlay getBookmarkOverlay();

    IAutocraftingHandler getAutocraftingHandler();
}
