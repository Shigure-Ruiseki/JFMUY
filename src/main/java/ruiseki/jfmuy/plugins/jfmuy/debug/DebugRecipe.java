package ruiseki.jfmuy.plugins.jfmuy.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.HoverChecker;
import ruiseki.jfmuy.api.IIngredientFilter;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredient;

public class DebugRecipe implements IRecipeWrapper {

    private final GuiButtonExt button;
    private final HoverChecker buttonHoverChecker;
    private boolean hiddenRecipes;

    public DebugRecipe() {
        this.button = new GuiButtonExt(0, 110, 30, "test");
        this.button.width = 40;
        this.buttonHoverChecker = new HoverChecker(this.button, 0);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        button.drawButton(minecraft, mouseX, mouseY);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        FluidStack water = new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000));
        FluidStack lava = new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000));

        ingredients.setInputs(VanillaTypes.FLUID, Arrays.asList(water, lava));

        ingredients.setInput(VanillaTypes.ITEM, new ItemStack(Items.stick));

        ingredients.setInputLists(
            DebugIngredient.TYPE,
            Collections.singletonList(Arrays.asList(new DebugIngredient(0), new DebugIngredient(1))));

        ingredients.setOutputs(DebugIngredient.TYPE, Arrays.asList(new DebugIngredient(2), new DebugIngredient(3)));
    }

    public List<FluidStack> getFluidInputs() {
        return Arrays.asList(
            new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000)),
            new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000)));
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltipStrings = new ArrayList<>();
        if (buttonHoverChecker.checkHover(mouseX, mouseY)) {
            tooltipStrings.add("button tooltip!");
        } else {
            tooltipStrings.add(EnumChatFormatting.BOLD + "tooltip debug");
        }
        tooltipStrings.add(mouseX + ", " + mouseY);
        return tooltipStrings;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && button.mousePressed(minecraft, mouseX, mouseY)) {
            EntityPlayerSP player = minecraft.thePlayer;
            if (player != null) {
                GuiScreen screen = new GuiInventory(player);
                minecraft.displayGuiScreen(screen);
            }
            IJFMUYRuntime runtime = JFMUYInternalPlugin.jfmuyRuntime;
            if (runtime != null) {
                IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
                String filterText = ingredientFilter.getFilterText();
                ingredientFilter.setFilterText(filterText + " test");

                IRecipeRegistry recipeRegistry = runtime.getRecipeRegistry();
                if (!hiddenRecipes) {
                    recipeRegistry.hideRecipeCategory(VanillaRecipeCategoryUid.CRAFTING);
                    hiddenRecipes = true;
                } else {
                    recipeRegistry.unhideRecipeCategory(VanillaRecipeCategoryUid.CRAFTING);
                    hiddenRecipes = false;
                }
            }
            return true;
        }
        return false;
    }
}
