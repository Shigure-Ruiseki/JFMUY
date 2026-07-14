package ruiseki.jfmuy.plugins.vanilla.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.util.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;

public class ShapelessRecipesWrapper extends AbstractShapelessRecipeWrapper {

    private final ShapelessRecipes recipe;

    public ShapelessRecipesWrapper(IGuiHelper guiHelper, ShapelessRecipes recipe) {
        super(guiHelper);
        this.recipe = recipe;
        for (Object input : this.recipe.recipeItems) {
            if (input instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) input;
                if (itemStack.stackSize != 1) {
                    itemStack.stackSize = 1;
                }
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack recipeOutput = recipe.getRecipeOutput();

        try {
            ingredients.setInputs(ItemStack.class, recipe.recipeItems);
            if (recipeOutput != null) {
                ingredients.setOutput(ItemStack.class, recipeOutput);
            }
        } catch (RuntimeException e) {
            String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.recipeItems, recipeOutput);
            throw new BrokenCraftingRecipeException(info, e);
        }
    }
}
