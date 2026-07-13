package ruiseki.jfmuy.api;

/**
 * Gives access to JFMUY functions that are available once everything has loaded.
 * The IJFMUYRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJFMUYRuntime)}.
 */
public interface IJFMUYRuntime {

    IRecipeRegistry getRecipeRegistry();

    /**
     * @since JEI 3.2.12
     */
    IRecipesGui getRecipesGui();

    /**
     * @since JEI 4.2.2
     */
    IIngredientFilter getIngredientFilter();

    /**
     * @since JEI 4.2.2
     */
    IIngredientListOverlay getIngredientListOverlay();

    /**
     * @since JEI 4.15.0
     */
    IBookmarkOverlay getBookmarkOverlay();

}
