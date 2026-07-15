package ruiseki.jfmuy.api;

import ruiseki.jfmuy.api.ingredients.IIngredientBlacklist;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * IJFMUYHelpers provides helpers and tools for addon mods.
 * Get the instance from {@link IModRegistry#getJFMUYHelpers()}.
 */
public interface IJFMUYHelpers {

    /**
     * Helps with the implementation of GUIs.
     */
    IGuiHelper getGuiHelper();

    /**
     * Helps with getting itemStacks from recipes.
     */
    IStackHelper getStackHelper();

    /**
     * Used to stop JFMUY from displaying a specific ingredient in the ingredient list
     */
    IIngredientBlacklist getIngredientBlacklist();

    /**
     * Helps with the implementation of Recipe Transfer Handlers
     */
    IRecipeTransferHandlerHelper recipeTransferHandlerHelper();

    /**
     * Allows manual creation of vanilla recipes.
     */
    IVanillaRecipeFactory getVanillaRecipeFactory();
}
