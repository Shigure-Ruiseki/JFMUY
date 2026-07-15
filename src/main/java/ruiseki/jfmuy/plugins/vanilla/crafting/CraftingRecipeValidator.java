package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IRecipeWrapperFactory;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public final class CraftingRecipeValidator<T extends IRecipe> implements ICraftingRecipeValidator<T> {

    private static final int INVALID_COUNT = -1;
    private final IRecipeWrapperFactory<T> recipeWrapperFactory;

    public CraftingRecipeValidator(IRecipeWrapperFactory<T> recipeWrapperFactory) {
        this.recipeWrapperFactory = recipeWrapperFactory;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(T recipe) {
        return this.recipeWrapperFactory.getRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(T recipe, StackHelper stackHelper) {
        ItemStack recipeOutput = recipe.getRecipeOutput();
        if (recipeOutput == null || recipeOutput.getItem() == null) {
            String recipeInfo = getInfo(recipe);
            Log.get()
                .error("Recipe has no output. {}", recipeInfo);
            return false;
        }

        int inputCount = getInputCount(recipe, stackHelper);
        if (inputCount == INVALID_COUNT) {
            return false;
        } else if (inputCount > 9) {
            String recipeInfo = getInfo(recipe);
            Log.get()
                .error("Recipe has too many inputs. {}", recipeInfo);
            return false;
        } else if (inputCount == 0) {
            String recipeInfo = getInfo(recipe);
            Log.get()
                .error("Recipe has no inputs. {}", recipeInfo);
            return false;
        }
        return true;
    }

    private String getInfo(T recipe) {
        IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe);
        return ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
    }

    private int getInputCount(T recipe, StackHelper stackHelper) {
        Object[] inputs = null;

        if (recipe instanceof ShapedRecipes) {
            inputs = ((ShapedRecipes) recipe).recipeItems;
        } else if (recipe instanceof ShapelessRecipes) {
            List<?> list = ((ShapelessRecipes) recipe).recipeItems;
            if (list != null) {
                inputs = list.toArray();
            }
        } else if (recipe instanceof ShapedOreRecipe) {
            inputs = ((ShapedOreRecipe) recipe).getInput();
        } else if (recipe instanceof ShapelessOreRecipe) {
            List<?> list = ((ShapelessOreRecipe) recipe).getInput();
            if (list != null) {
                inputs = list.toArray();
            }
        }

        if (inputs == null) {
            return INVALID_COUNT;
        }

        int inputCount = 0;
        for (Object input : inputs) {
            if (input != null) {
                if (input instanceof List) {
                    if (((List<?>) input).isEmpty()) {
                        return INVALID_COUNT;
                    }
                }
                inputCount++;
            }
        }
        return inputCount;
    }
}
