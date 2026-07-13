package ruiseki.jfmuy.api;

import ruiseki.jfmuy.api.ingredients.IIngredientBlacklist;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;

public interface IJFMUYHelpers {

    /**
     * Helps with the implementation of GUIs.
     */
    IGuiHelper getGuiHelper();

    /**
     * Helps with getting itemStacks from recipes.
     */
    IStackHelper getStackHelper();

    /*
     * Used to stop JEI from displaying a specific ingredient in the ingredient list
     * @since JEI 4.2.1
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
