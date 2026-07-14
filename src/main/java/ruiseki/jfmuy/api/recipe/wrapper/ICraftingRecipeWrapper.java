package ruiseki.jfmuy.api.recipe.wrapper;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public interface ICraftingRecipeWrapper extends IRecipeWrapper {

    @Nonnull
    @Override
    List getInputs();

    @Nonnull
    @Override
    List<ItemStack> getOutputs();

}
