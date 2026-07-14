package ruiseki.jfmuy.plugins.vanilla.brewing;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class BrewingRecipeHandler implements IRecipeHandler<BrewingRecipeWrapper> {

    @Override
    public Class<BrewingRecipeWrapper> getRecipeClass() {
        return BrewingRecipeWrapper.class;
    }

    @Override
    public String getRecipeCategoryUid(BrewingRecipeWrapper recipe) {
        return VanillaRecipeCategoryUid.BREWING;
    }

    @Override
    public BrewingRecipeWrapper getRecipeWrapper(BrewingRecipeWrapper recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(BrewingRecipeWrapper recipe) {
        if (recipe.getInputs()
            .size() != 4) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
            Log.error("Recipe has the wrong number of inputs (needs 4). {}", recipeInfo);
            return false;
        }
        if (recipe.getOutputs()
            .size() != 1) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
            Log.error("Recipe has the wrong number of outputs (needs 1). {}", recipeInfo);
            return false;
        }
        return true;
    }
}
