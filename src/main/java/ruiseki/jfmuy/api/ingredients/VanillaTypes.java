package ruiseki.jfmuy.api.ingredients;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.jfmuy.api.recipe.IIngredientType;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 */
public final class VanillaTypes {

    public static final IIngredientType<ItemStack> ITEM = () -> ItemStack.class;
    public static final IIngredientType<FluidStack> FLUID = () -> FluidStack.class;

    private VanillaTypes() {

    }
}
