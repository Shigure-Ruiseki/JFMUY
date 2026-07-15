package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.util.Translator;

public class SmeltingRecipe implements IRecipeWrapper {

    private final List<List<ItemStack>> inputs;
    private final ItemStack output;

    public SmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
        this.inputs = Collections.singletonList(inputs);
        this.output = output;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        FurnaceRecipes furnaceRecipes = FurnaceRecipes.smelting();
        float experience;
        try {
            experience = furnaceRecipes.func_151398_b(output);
        } catch (RuntimeException ignored) {
            experience = 0;
        }
        if (experience > 0) {
            String experienceString = Translator
                .translateToLocalFormatted("gui.jfmuy.category.smelting.experience", experience);
            FontRenderer fontRenderer = minecraft.fontRenderer;
            int stringWidth = fontRenderer.getStringWidth(experienceString);
            fontRenderer.drawString(experienceString, recipeWidth - stringWidth, 0, Color.gray.getRGB());
        }
    }
}
