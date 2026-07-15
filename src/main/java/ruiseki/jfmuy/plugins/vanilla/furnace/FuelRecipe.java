package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawableAnimated;
import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.util.Translator;

public class FuelRecipe extends BlankRecipeWrapper {

    private final List<List<ItemStack>> inputs;
    private final String smeltCountString;
    private final String burnTimeString;
    private final IDrawableAnimated flame;

    public FuelRecipe(IGuiHelper guiHelper, Collection<ItemStack> input, int burnTime) {
        List<ItemStack> inputList = new ArrayList<ItemStack>(input);
        this.inputs = Collections.singletonList(inputList);

        if (burnTime == 200) {
            this.smeltCountString = Translator.translateToLocal("gui.jfmuy.category.fuel.smeltCount.single");
        } else {
            NumberFormat numberInstance = NumberFormat.getNumberInstance();
            numberInstance.setMaximumFractionDigits(2);
            String smeltCount = numberInstance.format(burnTime / 200f);
            this.smeltCountString = Translator
                .translateToLocalFormatted("gui.jfmuy.category.fuel.smeltCount", smeltCount);
        }

        this.burnTimeString = Translator.translateToLocalFormatted("gui.jfmuy.category.fuel.burnTime", burnTime);

        ResourceLocation furnaceBackgroundLocation = new ResourceLocation(
            "minecraft",
            "textures/gui/container/furnace.png");
        IDrawableStatic flameDrawable = guiHelper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
        this.flame = guiHelper
            .createAnimatedDrawable(flameDrawable, burnTime, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, inputs);
    }

    public List<List<ItemStack>> getInputs() {
        return inputs;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        flame.draw(minecraft, 2, 0);
        minecraft.fontRenderer.drawString(smeltCountString, 24, 8, Color.gray.getRGB());
        minecraft.fontRenderer.drawString(burnTimeString, 24, 18, Color.gray.getRGB());
    }
}
