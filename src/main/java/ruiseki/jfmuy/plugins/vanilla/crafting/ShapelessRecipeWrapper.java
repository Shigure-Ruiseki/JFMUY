package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.wrapper.ICraftingRecipeWrapper;
import ruiseki.jfmuy.recipes.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;

public class ShapelessRecipeWrapper<T extends IRecipe> implements ICraftingRecipeWrapper {

    private final IJFMUYHelpers jeiHelpers;
    protected final T recipe;

    public ShapelessRecipeWrapper(IJFMUYHelpers jeiHelpers, T recipe) {
        this.jeiHelpers = jeiHelpers;
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack recipeOutput = recipe.getRecipeOutput();
        IStackHelper stackHelper = jeiHelpers.getStackHelper();

        List<Object> rawInputs = new ArrayList<>();
        if (recipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            if (shapeless.recipeItems != null) {
                rawInputs.addAll(shapeless.recipeItems);
            }
        } else if (recipe instanceof ShapelessOreRecipe) {
            ShapelessOreRecipe shapelessOre = (ShapelessOreRecipe) recipe;
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

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return null;
    }
}
