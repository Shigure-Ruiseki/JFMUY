package ruiseki.jfmuy.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

public class SmeltingRecipeHandler implements IRecipeHandler<SmeltingRecipe> {

    @Override
    @Nonnull
    public Class<SmeltingRecipe> getRecipeClass() {
        return SmeltingRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.SMELTING;
    }

    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull SmeltingRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull SmeltingRecipe recipe) {
        return recipe.getInputs()
            .size() != 0
            && recipe.getOutputs()
                .size() > 0;
    }

}
