package ruiseki.jfmuy.api.recipe;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public abstract class BlankRecipeWrapper implements IRecipeWrapper {

    @Override
    public List getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public List<FluidStack> getFluidOutputs() {
        return Collections.emptyList();
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

    }

    @Override
    public void drawAnimations(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight) {

    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(@NotNull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
