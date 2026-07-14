package ruiseki.jfmuy.plugins.vanilla.furnace;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class FuelRecipeHandler implements IRecipeHandler<FuelRecipe> {

    @Override
    public Class<FuelRecipe> getRecipeClass() {
        return FuelRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(FuelRecipe recipe) {
        return VanillaRecipeCategoryUid.FUEL;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(FuelRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(FuelRecipe recipe) {
        if (recipe.getInputs()
            .isEmpty()) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
            Log.error("Recipe has no inputs. {}", recipeInfo);
        }
        return true;
    }
}
