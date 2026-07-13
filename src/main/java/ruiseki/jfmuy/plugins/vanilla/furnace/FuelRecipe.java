package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawableAnimated;
import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.plugins.vanilla.VanillaRecipeWrapper;
import ruiseki.jfmuy.util.Translator;

public class FuelRecipe extends VanillaRecipeWrapper {

    @Nonnull
    private final List<List<ItemStack>> inputs;
    @Nonnull
    private final String burnTimeString;
    @Nonnull
    private final IDrawableAnimated flame;

    public FuelRecipe(@Nonnull IGuiHelper guiHelper, @Nonnull Collection<ItemStack> input, int burnTime) {
        List<ItemStack> inputList = new ArrayList<>(input);
        this.inputs = Collections.singletonList(inputList);
        this.burnTimeString = Translator.translateToLocalFormatted("gui.jfmuy.category.fuel.burnTime", burnTime);

        ResourceLocation furnaceBackgroundLocation = new ResourceLocation(
            "minecraft",
            "textures/gui/container/furnace.png");
        IDrawableStatic flameDrawable = guiHelper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
        this.flame = guiHelper
            .createAnimatedDrawable(flameDrawable, burnTime, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Nonnull
    @Override
    public List<List<ItemStack>> getInputs() {
        return inputs;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
        minecraft.fontRenderer.drawString(burnTimeString, 24, 12, Color.gray.getRGB());
    }

    @Override
    public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
        flame.draw(minecraft, 2, 0);
    }
}
