package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.commons.lang3.tuple.Pair;

import ruiseki.jfmuy.api.IJFMUYHelpers;

public final class CraftingRecipeChecker {

    private CraftingRecipeChecker() {}

    public static Pair<List<IRecipe>, Set<Class<? extends IRecipe>>> getValidRecipes(final IJFMUYHelpers jeiHelpers) {
        CraftingRecipeValidator validator = new CraftingRecipeValidator();

        Set<Class<? extends IRecipe>> recipeTypes = new HashSet<>();

        List<?> recipeList = CraftingManager.getInstance()
            .getRecipeList();
        Iterator<?> recipeIterator = recipeList.iterator();
        List<IRecipe> validRecipes = new ArrayList<>();

        while (recipeIterator.hasNext()) {
            Object obj = recipeIterator.next();
            if (obj instanceof IRecipe) {
                IRecipe recipe = (IRecipe) obj;
                recipeTypes.add(recipe.getClass());

                if (validator.isRecipeValid(recipe)) {
                    validRecipes.add(recipe);
                }
            }
        }

        return Pair.of(validRecipes, recipeTypes);
    }

    private static final class CraftingRecipeValidator {

        public boolean isRecipeValid(IRecipe recipe) {
            ItemStack recipeOutput = recipe.getRecipeOutput();
            if (recipeOutput == null || recipeOutput.getItem() == null) {
                return false;
            }

            int inputCount = 0;

            if (recipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes) recipe;
                if (shaped.recipeItems == null) return false;
                for (ItemStack stack : shaped.recipeItems) {
                    if (stack != null) inputCount++;
                }
            } else if (recipe instanceof ShapelessRecipes) {
                ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
                if (shapeless.recipeItems == null) return false;
                for (Object obj : shapeless.recipeItems) {
                    if (obj instanceof ItemStack) {
                        inputCount++;
                    }
                }
            } else if (recipe instanceof ShapedOreRecipe) {
                ShapedOreRecipe shapedOre = (ShapedOreRecipe) recipe;
                Object[] inputObjects = shapedOre.getInput();
                if (inputObjects == null) return false;
                for (Object obj : inputObjects) {
                    if (obj != null) {
                        if (obj instanceof List && ((List<?>) obj).isEmpty()) {
                            return false;
                        }
                        inputCount++;
                    }
                }
            } else if (recipe instanceof ShapelessOreRecipe) {
                ShapelessOreRecipe shapelessOre = (ShapelessOreRecipe) recipe;
                List<?> inputList = shapelessOre.getInput();
                if (inputList == null) return false;
                for (Object obj : inputList) {
                    if (obj != null) {
                        if (obj instanceof List && ((List<?>) obj).isEmpty()) {
                            return false;
                        }
                        inputCount++;
                    }
                }
            } else {
                return recipe.getRecipeSize() > 0;
            }

            if (inputCount > 9 || inputCount == 0) {
                return false;
            }

            return true;
        }
    }
}
