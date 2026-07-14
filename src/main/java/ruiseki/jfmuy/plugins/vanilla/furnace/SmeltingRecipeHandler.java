package ruiseki.jfmuy.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class SmeltingRecipeHandler implements IRecipeHandler<SmeltingRecipe> {

    @Override
    @Nonnull
    public Class<SmeltingRecipe> getRecipeClass() {
        return SmeltingRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull SmeltingRecipe recipe) {
        return VanillaRecipeCategoryUid.SMELTING;
    }

    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull SmeltingRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull SmeltingRecipe recipe) {
        if (recipe.getInputs()
            .isEmpty()) {
            String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
            Log.error("Recipe has no inputs. {}", recipeInfo);
        }
        if (recipe.getOutputs()
            .isEmpty()) {
            String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, this);
            Log.error("Recipe has no outputs. {}", recipeInfo);
        }
        return true;
    }

}
