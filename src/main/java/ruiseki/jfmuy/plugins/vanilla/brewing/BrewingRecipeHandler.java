package ruiseki.jfmuy.plugins.vanilla.brewing;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class BrewingRecipeHandler implements IRecipeHandler<BrewingRecipeWrapper> {

    @Nonnull
    @Override
    public Class<BrewingRecipeWrapper> getRecipeClass() {
        return BrewingRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull BrewingRecipeWrapper recipe) {
        return VanillaRecipeCategoryUid.BREWING;
    }

    @Nonnull
    @Override
    public BrewingRecipeWrapper getRecipeWrapper(@Nonnull BrewingRecipeWrapper recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull BrewingRecipeWrapper recipe) {
        if (recipe.getInputs()
            .size() != 4) {
            String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
            Log.error("Recipe has the wrong number of inputs (needs 4). {}", recipeInfo);
            return false;
        }
        if (recipe.getOutputs()
            .size() != 1) {
            String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
            Log.error("Recipe has the wrong number of outputs (needs 1). {}", recipeInfo);
            return false;
        }
        return true;
    }
}
