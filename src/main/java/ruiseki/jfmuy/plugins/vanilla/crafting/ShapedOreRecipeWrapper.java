package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.VanillaRecipeWrapper;

public class ShapedOreRecipeWrapper extends VanillaRecipeWrapper implements IShapedCraftingRecipeWrapper {

    @Nonnull
    private final ShapedOreRecipe recipe;
    private final int width;
    private final int height;

    public ShapedOreRecipeWrapper(@Nonnull ShapedOreRecipe recipe) {
        this.recipe = recipe;
        for (Object input : this.recipe.getInput()) {
            if (input instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) input;
                if (itemStack.stackSize != 1) {
                    itemStack.stackSize = 1;
                }
            }
        }
        this.width = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, this.recipe, "width");
        this.height = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, this.recipe, "height");
    }

    @Nonnull
    @Override
    public List getInputs() {
        return Arrays.asList(recipe.getInput());
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputs() {
        return Collections.singletonList(recipe.getRecipeOutput());
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

}
