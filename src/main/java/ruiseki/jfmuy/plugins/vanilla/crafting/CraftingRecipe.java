package ruiseki.jfmuy.plugins.vanilla.crafting;

import net.minecraft.item.crafting.IRecipe;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.wrapper.ICraftingRecipeWrapper;

public abstract class CraftingRecipe<T extends IRecipe> implements ICraftingRecipeWrapper {

    protected final IJFMUYHelpers jeiHelpers;
    protected final T recipe;

    public CraftingRecipe(IJFMUYHelpers jeiHelpers, T recipe) {
        this.jeiHelpers = jeiHelpers;
        this.recipe = recipe;
    }
}
