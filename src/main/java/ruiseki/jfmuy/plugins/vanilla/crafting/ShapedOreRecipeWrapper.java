package ruiseki.jfmuy.plugins.vanilla.crafting;

import net.minecraftforge.oredict.ShapedOreRecipe;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

public class ShapedOreRecipeWrapper extends ShapelessRecipeWrapper<ShapedOreRecipe>
    implements IShapedCraftingRecipeWrapper {

    public ShapedOreRecipeWrapper(IJFMUYHelpers jeiHelpers, ShapedOreRecipe recipe) {
        super(jeiHelpers, recipe);
    }

    @Override
    public int getWidth() {
        return recipe.width;
    }

    @Override
    public int getHeight() {
        return recipe.height;
    }

}
