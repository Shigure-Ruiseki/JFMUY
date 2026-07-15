package ruiseki.jfmuy.runtime;

import ruiseki.jfmuy.api.IBookmarkOverlay;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.recipes.RecipeRegistry;

public class JFMUYRuntime implements IJFMUYRuntime {

    private final RecipeRegistry recipeRegistry;
    private final IngredientListOverlay ingredientListOverlay;
    private final IBookmarkOverlay bookmarkOverlay;
    private final RecipesGui recipesGui;
    private final IngredientFilter ingredientFilter;

    public JFMUYRuntime(RecipeRegistry recipeRegistry, IngredientListOverlay ingredientListOverlay,
        IBookmarkOverlay bookmarkOverlay, RecipesGui recipesGui, IngredientFilter ingredientFilter) {
        this.recipeRegistry = recipeRegistry;
        this.ingredientListOverlay = ingredientListOverlay;
        this.bookmarkOverlay = bookmarkOverlay;
        this.recipesGui = recipesGui;
        this.ingredientFilter = ingredientFilter;
    }

    public void close() {
        this.recipesGui.close();
    }

    @Override
    public RecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    @Override
    public IngredientFilter getIngredientFilter() {
        return ingredientFilter;
    }

    @Override
    public IngredientListOverlay getIngredientListOverlay() {
        return ingredientListOverlay;
    }

    @Override
    public IBookmarkOverlay getBookmarkOverlay() {
        return bookmarkOverlay;
    }

    @Override
    public RecipesGui getRecipesGui() {
        return recipesGui;
    }
}
