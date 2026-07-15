package ruiseki.jfmuy.api.recipe;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModPlugin;

/**
 * This is given to your {@link IModPlugin#registerCategories(IRecipeCategoryRegistration)}.
 *
 * @since JEI 4.5.0
 */
public interface IRecipeCategoryRegistration {

    /**
     * Add the recipe categories provided by this plugin.
     */
    void addRecipeCategories(IRecipeCategory... recipeCategories);

    /**
     * Get helpers and tools for implementing JEI plugins.
     */
    IJFMUYHelpers getJFMUYHelpers();
}
