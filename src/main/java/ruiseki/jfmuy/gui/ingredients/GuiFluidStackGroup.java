package ruiseki.jfmuy.gui.ingredients;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiFluidStackGroup;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.plugins.vanilla.ingredients.fluid.FluidStackRenderer;

public class GuiFluidStackGroup extends GuiIngredientGroup<FluidStack> implements IGuiFluidStackGroup {

    public GuiFluidStackGroup(@Nullable IFocus<FluidStack> focus, int cycleOffset) {
        super(VanillaTypes.FLUID, focus, cycleOffset);
    }

    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb,
        boolean showCapacity, @Nullable IDrawable overlay) {
        FluidStackRenderer renderer = new FluidStackRenderer(capacityMb, showCapacity, width, height, overlay);
        init(slotIndex, input, renderer, xPosition, yPosition, width, height, 0, 0);
    }
}
