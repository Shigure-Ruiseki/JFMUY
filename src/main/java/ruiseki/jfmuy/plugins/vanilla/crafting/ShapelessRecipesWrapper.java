package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.wrapper.ICraftingRecipeWrapper;
import ruiseki.jfmuy.recipes.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;

public class ShapelessRecipesWrapper<T extends IRecipe> implements ICraftingRecipeWrapper {

    protected final IJFMUYHelpers jfmuyHelpers;
    protected final T recipe;

    public ShapelessRecipesWrapper(IJFMUYHelpers jfmuyHelpers, T recipe) {
        this.jfmuyHelpers = jfmuyHelpers;
        this.recipe = recipe;
    }

    public T getRawRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack recipeOutput = recipe.getRecipeOutput();
        IStackHelper stackHelper = jfmuyHelpers.getStackHelper();

        List<Object> rawInputs = new ArrayList<>();
        if (recipe instanceof ShapelessRecipes shapeless) {
            if (shapeless.recipeItems != null) {
                rawInputs.addAll(shapeless.recipeItems);
            }
        } else if (recipe instanceof ShapelessOreRecipe shapelessOre) {
            if (shapelessOre.getInput() != null) {
                rawInputs.addAll(shapelessOre.getInput());
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
