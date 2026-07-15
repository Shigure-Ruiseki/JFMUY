package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.jfmuy.recipes.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.okcore.helper.ItemStackHelpers;

public class ShapedRecipesWrapper implements IShapedCraftingRecipeWrapper {

    private final IJFMUYHelpers jeiHelpers;
    private final ShapedRecipes recipe;

    public ShapedRecipesWrapper(IJFMUYHelpers jeiHelpers, ShapedRecipes recipe) {
        this.jeiHelpers = jeiHelpers;
        this.recipe = recipe;
    }

    @Override
    public int getWidth() {
        return recipe.recipeWidth;
    }

    @Override
    public int getHeight() {
        return recipe.recipeHeight;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack recipeOutput = recipe.getRecipeOutput();
        IStackHelper stackHelper = jeiHelpers.getStackHelper();

        List<Object> rawInputs = new ArrayList<>();
        ItemStack[] inputs = recipe.recipeItems;

        if (inputs != null) {
            for (ItemStack stack : inputs) {
                rawInputs.add(ItemStackHelpers.copyWithSize(stack, 1));
            }
        }

        try {
            List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(rawInputs);
            ingredients.setInputLists(VanillaTypes.ITEM, inputLists);
            ingredients.setOutput(VanillaTypes.ITEM, recipeOutput);
        } catch (RuntimeException e) {
            String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, rawInputs, recipeOutput);
            throw new BrokenCraftingRecipeException(info, e);
        }
    }
}
