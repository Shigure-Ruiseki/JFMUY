package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.startup.StackHelper;

public final class CraftingRecipeMarker {

    private CraftingRecipeMarker() {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<IRecipeWrapper> getValidRecipes(final IJFMUYHelpers jfmuyHelpers) {
        StackHelper stackHelper = Internal.getStackHelper();
        List<IRecipe> recipeList = CraftingManager.getInstance()
            .getRecipeList();
        List<IRecipeWrapper> validRecipeWrappers = new ArrayList<>();

        for (IRecipe recipe : recipeList) {

            ICraftingRecipeValidator validator = CraftingRecipeValidatorRegistry.getValidatorFor(recipe);
            if (validator != null && validator.isRecipeValid(recipe, stackHelper)) {
                validRecipeWrappers.add(validator.getRecipeWrapper(recipe));
            }
        }
        return validRecipeWrappers;
    }
}
