package ruiseki.jfmuy.plugins.vanilla.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IRecipeWrapperFactory;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public final class CraftingRecipeValidator<T extends IRecipe> implements ICraftingRecipeValidator<T> {

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
        if (recipeOutput == null || recipeOutput.getItem() == null || recipeOutput.stackSize <= 0) {
            String recipeInfo = getInfo(recipe);
            Log.get()
                .error("Recipe has no valid output (null, empty, or size <= 0). {}", recipeInfo);
            return false;
        }

        return true;
    }

    private String getInfo(T recipe) {
        IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe);
        return ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
    }
}
