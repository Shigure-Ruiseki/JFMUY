package ruiseki.jfmuy.plugins.vanilla;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.plugins.vanilla.anvil.AnvilRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.furnace.SmeltingRecipe;
import ruiseki.jfmuy.util.ErrorUtil;

public class VanillaRecipeFactory implements IVanillaRecipeFactory {

    @Override
    public IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        ErrorUtil.checkNotEmpty(leftInput, "leftInput");
        ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
        ErrorUtil.checkNotEmpty(outputs, "outputs");

        return new AnvilRecipeWrapper(Collections.singletonList(leftInput), rightInputs, outputs);
    }

    @Override
    public IRecipeWrapper createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs,
        List<ItemStack> outputs) {
        ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
        ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
        ErrorUtil.checkNotEmpty(outputs, "outputs");

        return new AnvilRecipeWrapper(leftInputs, rightInputs, outputs);
    }

    @Override
    public IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
        ErrorUtil.checkNotEmpty(inputs, "inputs");
        ErrorUtil.checkNotEmpty(output, "output");

        return new SmeltingRecipe(inputs, output);
    }

    @Override
    public IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput,
        ItemStack potionOutput) {
        ErrorUtil.checkNotEmpty(ingredients, "ingredients");
        ErrorUtil.checkNotEmpty(potionInput, "potionInput");
        ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

        return new BrewingRecipeWrapper(ingredients, potionInput, potionOutput);
    }
}
