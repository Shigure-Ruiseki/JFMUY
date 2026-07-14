package ruiseki.jfmuy.plugins.jfmuy.description;

import java.util.List;

import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class ItemDescriptionRecipeHandler implements IRecipeHandler<ItemDescriptionRecipe> {

    @Override
    public Class<ItemDescriptionRecipe> getRecipeClass() {
        return ItemDescriptionRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(ItemDescriptionRecipe recipe) {
        return VanillaRecipeCategoryUid.DESCRIPTION;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(ItemDescriptionRecipe recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(ItemDescriptionRecipe recipe) {
        List<String> description = recipe.getDescription();
        if (description.isEmpty()) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, this);
            Log.error("Recipe has no description text. {}", recipeInfo);
        }
        return true;
    }
}
