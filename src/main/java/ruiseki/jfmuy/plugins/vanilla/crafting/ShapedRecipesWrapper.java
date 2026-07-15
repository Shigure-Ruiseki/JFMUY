package ruiseki.jfmuy.plugins.vanilla.crafting;

import net.minecraft.item.crafting.ShapedRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

public class ShapedRecipesWrapper extends ShapelessRecipeWrapper<ShapedRecipes>
    implements IShapedCraftingRecipeWrapper {

    public ShapedRecipesWrapper(IJFMUYHelpers jeiHelpers, ShapedRecipes recipe) {
        super(jeiHelpers, recipe);
    }

    @Override
    public int getWidth() {
        return recipe.recipeWidth;
    }

    @Override
    public int getHeight() {
        return recipe.recipeHeight;
    }
}
