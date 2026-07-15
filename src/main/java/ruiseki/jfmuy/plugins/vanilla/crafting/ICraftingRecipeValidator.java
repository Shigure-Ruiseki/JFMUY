package ruiseki.jfmuy.plugins.vanilla.crafting;

import net.minecraft.item.crafting.IRecipe;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.startup.StackHelper;

public interface ICraftingRecipeValidator<T extends IRecipe> {

    boolean isRecipeValid(T recipe, StackHelper stackHelper);

    IRecipeWrapper getRecipeWrapper(T recipe);
}
