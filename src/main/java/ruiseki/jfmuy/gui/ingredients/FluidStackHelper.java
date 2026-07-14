package ruiseki.jfmuy.gui.ingredients;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.jfmuy.gui.Focus;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {

    @Override
    public Collection<FluidStack> expandSubtypes(Collection<FluidStack> contained) {
        return contained;
    }

    @Override
    public FluidStack getMatch(Iterable<FluidStack> contained, @Nonnull Focus toMatch) {
        if (toMatch.getFluid() == null) {
            return null;
        }
        for (FluidStack fluidStack : contained) {
            if (toMatch.getFluid() == fluidStack.getFluid()) {
                return fluidStack;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Focus createFocus(@Nonnull FluidStack ingredient) {
        return new Focus(ingredient.getFluid());
    }
}
