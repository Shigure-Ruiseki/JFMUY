package ruiseki.jfmuy.plugins.jfmuy.description;

import java.util.List;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

public class ItemDescriptionRecipeHandler implements IRecipeHandler<ItemDescriptionRecipe> {

    @Nonnull
    @Override
    public Class<ItemDescriptionRecipe> getRecipeClass() {
        return ItemDescriptionRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.DESCRIPTION;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull ItemDescriptionRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull ItemDescriptionRecipe recipe) {
        List<String> description = recipe.getDescription();
        return description.size() > 0;
    }
}
