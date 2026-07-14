package ruiseki.jfmuy.plugins.vanilla.furnace;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class SmeltingRecipeHandler implements IRecipeHandler<SmeltingRecipe> {

    @Override
    public Class<SmeltingRecipe> getRecipeClass() {
        return SmeltingRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(SmeltingRecipe recipe) {
        return VanillaRecipeCategoryUid.SMELTING;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(SmeltingRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(SmeltingRecipe recipe) {
        if (recipe.getInputs()
            .isEmpty()) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
            Log.error("Recipe has no inputs. {}", recipeInfo);
        }
        if (recipe.getOutputs()
            .isEmpty()) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
            Log.error("Recipe has no outputs. {}", recipeInfo);
        }
        return true;
    }

}
