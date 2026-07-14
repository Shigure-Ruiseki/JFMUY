package ruiseki.jfmuy.plugins.jfmuy.debug;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public class DebugRecipeHandler implements IRecipeHandler<DebugRecipe> {

    @Nonnull
    @Override
    public Class<DebugRecipe> getRecipeClass() {
        return DebugRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull DebugRecipe recipe) {
        return "debug";
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull DebugRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull DebugRecipe recipe) {
        return true;
    }
}
