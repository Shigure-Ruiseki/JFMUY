package ruiseki.jfmuy.api.recipe;

/**
 * Converts recipes to {@link IRecipeWrapper}.
 * Using Java 8 you can define this using syntax like this: MyRecipeWrapper::new
 *
 * @param <T> the recipe type
 */
@FunctionalInterface
public interface IRecipeWrapperFactory<T> {

    /**
     * Returns a recipe wrapper for the given recipe.
     */
    IRecipeWrapper getRecipeWrapper(T recipe);
}
