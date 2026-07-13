package ruiseki.jfmuy.api.recipe.wrapper;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public interface ICraftingRecipeWrapper extends IRecipeWrapper {

    @Override
    List getInputs();

    @Override
    List<ItemStack> getOutputs();

    @Override
    void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight);

}
