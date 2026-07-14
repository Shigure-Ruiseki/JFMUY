package ruiseki.jfmuy.api;

/**
 * Gives access to JFMUY functions that are available once everything has loaded.
 * The IJFMUYRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJFMUYRuntime)} .
 */
public interface IJFMUYRuntime {

    IRecipeRegistry getRecipeRegistry();

    IItemListOverlay getItemListOverlay();

    IRecipesGui getRecipesGui();
}
