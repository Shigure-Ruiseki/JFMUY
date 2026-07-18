package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IRecipeWrapperFactory;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public final class CraftingRecipeChecker {

    private CraftingRecipeChecker() {}

    @SuppressWarnings("unchecked")
    public static List<IRecipe> getValidRecipes(final IJFMUYHelpers jfmuyHelpers) {
        CraftingRecipeValidator<ShapedOreRecipe> shapedOreRecipeValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapedOreRecipeWrapper(jfmuyHelpers, recipe));
        CraftingRecipeValidator<ShapedRecipes> shapedRecipesValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapedRecipesWrapper(jfmuyHelpers, recipe));

        CraftingRecipeValidator<ShapelessOreRecipe> shapelessOreRecipeValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapelessRecipesWrapper<>(jfmuyHelpers, recipe));
        CraftingRecipeValidator<ShapelessRecipes> shapelessRecipesValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapelessRecipesWrapper<>(jfmuyHelpers, recipe));

        StackHelper stackHelper = Internal.getStackHelper();

        List<IRecipe> recipeList = CraftingManager.getInstance()
            .getRecipeList();
        Iterator<IRecipe> recipeIterator = recipeList.iterator();

        List<IRecipe> validRecipes = new ArrayList<>();

        while (recipeIterator.hasNext()) {
            IRecipe recipe = recipeIterator.next();
            if (recipe == null) {
                continue;
            }

            if (recipe instanceof ShapedOreRecipe) {
                if (shapedOreRecipeValidator.isRecipeValid((ShapedOreRecipe) recipe, stackHelper)) {
                    validRecipes.add(recipe);
                }
            } else if (recipe instanceof ShapedRecipes) {
                if (shapedRecipesValidator.isRecipeValid((ShapedRecipes) recipe, stackHelper)) {
                    validRecipes.add(recipe);
                }
            } else if (recipe instanceof ShapelessOreRecipe) {
                if (shapelessOreRecipeValidator.isRecipeValid((ShapelessOreRecipe) recipe, stackHelper)) {
                    validRecipes.add(recipe);
                }
            } else if (recipe instanceof ShapelessRecipes) {
                if (shapelessRecipesValidator.isRecipeValid((ShapelessRecipes) recipe, stackHelper)) {
                    validRecipes.add(recipe);
                }
            } else {
                validRecipes.add(recipe);
            }
        }
        return validRecipes;
    }

    private static final class CraftingRecipeValidator<T extends IRecipe> {

        private final IRecipeWrapperFactory<T> recipeWrapperFactory;

        public CraftingRecipeValidator(IRecipeWrapperFactory<T> recipeWrapperFactory) {
            this.recipeWrapperFactory = recipeWrapperFactory;
        }

        public boolean isRecipeValid(T recipe, StackHelper stackHelper) {
            try {
                ItemStack recipeOutput = recipe.getRecipeOutput();
                if (recipeOutput == null || recipeOutput.getItem() == null) {
                    String recipeInfo = getInfo(recipe);
                    Log.get()
                        .error("Recipe has no output or output item is null. {}", recipeInfo);
                    return false;
                }
            } catch (RuntimeException | LinkageError e) {
                Log.get()
                    .error(
                        "Recipe crashed while getting output. Class: {}",
                        recipe.getClass()
                            .getName(),
                        e);
                return false;
            }
            return true;
        }

        private String getInfo(T recipe) {
            try {
                IRecipeWrapper recipeWrapper = recipeWrapperFactory.getRecipeWrapper(recipe);
                return ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
            } catch (Exception e) {
                return "Recipe Class: " + recipe.getClass()
                    .getName();
            }
        }
    }
}
