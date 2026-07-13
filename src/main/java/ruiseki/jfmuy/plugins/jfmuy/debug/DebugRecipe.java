package ruiseki.jfmuy.plugins.jfmuy.debug;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.realmsclient.gui.ChatFormatting;

import ruiseki.jfmuy.api.recipe.BlankRecipeWrapper;

public class DebugRecipe extends BlankRecipeWrapper {

    public DebugRecipe() {

    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return Arrays.asList(
            new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000)),
            new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000)));
    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Arrays.asList(ChatFormatting.BOLD + "tooltip debug", mouseX + ", " + mouseY);
    }
}
