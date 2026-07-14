package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.gui.recipes.RecipesGui;

public class JFMUYRuntime implements IJFMUYRuntime {

    private final RecipeRegistry recipeRegistry;
    private final ItemListOverlay itemListOverlay;
    private final RecipesGui recipesGui;
    private final IngredientRegistry ingredientRegistry;
    private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;

    public JFMUYRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay, RecipesGui recipesGui,
        IngredientRegistry ingredientRegistry, List<IAdvancedGuiHandler<?>> advancedGuiHandlers) {
        this.recipeRegistry = recipeRegistry;
        this.itemListOverlay = itemListOverlay;
        this.recipesGui = recipesGui;
        this.ingredientRegistry = ingredientRegistry;
        this.advancedGuiHandlers = advancedGuiHandlers;
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
    public RecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    @Override
    public ItemListOverlay getItemListOverlay() {
        return itemListOverlay;
    }

    @Override
    public RecipesGui getRecipesGui() {
        return recipesGui;
    }

    public IngredientRegistry getIngredientRegistry() {
        return ingredientRegistry;
    }

    public List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(GuiScreen guiScreen) {
        List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<?>>();
        if (guiScreen instanceof GuiContainer) {
            for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
                Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
                if (guiContainerClass.isInstance(guiScreen)) {
                    activeAdvancedGuiHandler.add(advancedGuiHandler);
                }
            }
        }
        return activeAdvancedGuiHandler;
    }
}
