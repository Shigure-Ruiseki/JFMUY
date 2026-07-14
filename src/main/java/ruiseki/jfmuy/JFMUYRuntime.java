package ruiseki.jfmuy;

import org.jetbrains.annotations.NotNull;

import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IRecipesGui;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.gui.RecipesGui;

public class JFMUYRuntime implements IJFMUYRuntime {

    private final RecipeRegistry recipeRegistry;
    private final ItemListOverlay itemListOverlay;
    private final RecipesGui recipesGui;

    public JFMUYRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay, RecipesGui recipesGui) {
        this.recipeRegistry = recipeRegistry;
        this.itemListOverlay = itemListOverlay;
        this.recipesGui = recipesGui;
    }

    public void close() {
        if (itemListOverlay.isOpen()) {
            itemListOverlay.close();
        }
        if (recipesGui.isOpen()) {
            recipesGui.close();
        }
    }

    @Override
    public @NotNull RecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    @Override
    public @NotNull ItemListOverlay getItemListOverlay() {
        return itemListOverlay;
    }

    @Override
    public @NotNull IRecipesGui getRecipesGui() {
        return recipesGui;
    }
}
