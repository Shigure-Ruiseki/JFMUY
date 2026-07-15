package ruiseki.jfmuy.plugins.vanilla.ingredients.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public final class FluidStackListFactory {

    private FluidStackListFactory() {

    }

    public static List<FluidStack> create() {
        List<FluidStack> fluidStacks = new ArrayList<>();

        Map<String, Fluid> registeredFluids = FluidRegistry.getRegisteredFluids();
        for (Fluid fluid : registeredFluids.values()) {
            Block fluidBlock = fluid.getBlock();
            if (Item.getItemFromBlock(fluidBlock) == null) {
                FluidStack fluidStack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
                fluidStacks.add(fluidStack);
            }
        }

        return fluidStacks;
    }
}
