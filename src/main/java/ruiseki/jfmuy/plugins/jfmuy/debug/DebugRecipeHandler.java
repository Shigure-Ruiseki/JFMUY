package ruiseki.jfmuy.plugins.jfmuy.debug;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public class DebugRecipeHandler implements IRecipeHandler<DebugRecipe> {

    @Override
    public Class<DebugRecipe> getRecipeClass() {
        return DebugRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(DebugRecipe recipe) {
        return "debug";
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(DebugRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(DebugRecipe recipe) {
        return true;
    }
}
