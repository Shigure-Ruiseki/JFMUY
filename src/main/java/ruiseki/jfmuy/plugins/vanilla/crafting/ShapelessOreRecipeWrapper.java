package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import ruiseki.jfmuy.api.recipe.wrapper.ICraftingRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.VanillaRecipeWrapper;

public class ShapelessOreRecipeWrapper extends VanillaRecipeWrapper implements ICraftingRecipeWrapper {

    @Nonnull
    private final ShapelessOreRecipe recipe;

    public ShapelessOreRecipeWrapper(@Nonnull ShapelessOreRecipe recipe) {
        this.recipe = recipe;
        for (Object input : this.recipe.getInput()) {
            if (input instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) input;
                if (itemStack.stackSize != 1) {
                    itemStack.stackSize = 1;
                }
            }
        }
    }

    @Nonnull
    @Override
    public List getInputs() {
        return recipe.getInput();
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputs() {
        return Collections.singletonList(recipe.getRecipeOutput());
    }
}
