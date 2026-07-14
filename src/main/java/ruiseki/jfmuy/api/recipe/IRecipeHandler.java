package ruiseki.jfmuy.api.recipe;

import org.jetbrains.annotations.NotNull;

/**
 * An IRecipeHandler provides information about one Recipe Class.
 */
public interface IRecipeHandler<T> {

    /** Returns the class of the Recipe handled by this IRecipeHandler. */
    @NotNull
    Class<T> getRecipeClass();

    /**
     * Returns this recipe's unique category id.
     */
    @NotNull
    String getRecipeCategoryUid(@NotNull T recipe);

    /** Returns a recipe wrapper for the given recipe. */
    @NotNull
    IRecipeWrapper getRecipeWrapper(@NotNull T recipe);

    /** Returns true if a recipe is valid and can be used. */
    boolean isRecipeValid(@NotNull T recipe);
}
