package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.awt.Color;
import java.text.NumberFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.google.common.base.Preconditions;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawableAnimated;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.util.Translator;

public class FuelRecipe implements IRecipeWrapper {

    private final ItemStack input;
    private final int burnTime;
    private final String burnTimeString;
    private final String smeltCountString;
    private final IDrawableAnimated flame;

    public FuelRecipe(IGuiHelper guiHelper, ItemStack input, int burnTime) {
        Preconditions.checkArgument(burnTime > 0, "burn time must be greater than 0");
        this.input = input;
        this.burnTime = burnTime;
        this.burnTimeString = Translator.translateToLocalFormatted("jfmuy.generic.ticks", burnTime);
        if (burnTime == 200) {
            this.smeltCountString = Translator.translateToLocal("gui.jfmuy.category.fuel.smeltCount.single");
        } else {
            NumberFormat numberInstance = NumberFormat.getNumberInstance();
            numberInstance.setMaximumFractionDigits(2);
            String smeltCount = numberInstance.format(burnTime / 200f);
            this.smeltCountString = Translator
                .translateToLocalFormatted("gui.jfmuy.category.fuel.smeltCount", smeltCount);
        }

        this.flame = guiHelper.drawableBuilder(Reference.RECIPE_GUI_VANILLA, 82, 114, 14, 14)
            .buildAnimated(burnTime, IDrawableAnimated.StartDirection.TOP, true);
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, this.input);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.flame.draw(minecraft, 1, 0);
        minecraft.fontRenderer.drawString(this.smeltCountString, 24, 8, Color.gray.getRGB());
        minecraft.fontRenderer.drawString(this.burnTimeString, 24, 24, Color.gray.getRGB());
    }
}
