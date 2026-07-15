package ruiseki.jfmuy.plugins.jfmuy.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.HoverChecker;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredient;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class DebugRecipe extends BlankRecipeWrapper {

    private final GuiButtonExt button;
    private final HoverChecker buttonHoverChecker;

    public DebugRecipe() {
        this.button = new GuiButtonExt(0, 110, 30, "test");
        this.button.width = (40);
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

        ingredients.setInputs(FluidStack.class, Arrays.asList(water, lava));

        ingredients.setInput(ItemStack.class, new ItemStack(Items.stick));

        ingredients.setInputLists(
            DebugIngredient.class,
            Collections.singletonList(Arrays.asList(new DebugIngredient(0), new DebugIngredient(1))));

        ingredients.setOutputs(DebugIngredient.class, Arrays.asList(new DebugIngredient(2), new DebugIngredient(3)));
    }

    @Nullable
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
            if (GuiScreen.isCtrlKeyDown()) {
                IJFMUYRuntime jfmuyRuntime = JFMUYInternalPlugin.jfmuyRuntime;
                if (jfmuyRuntime != null) {
                    List<IRecipe> recipeList = CraftingManager.getInstance()
                        .getRecipeList();
                    if (!recipeList.isEmpty()) {
                        IRecipe randomRecipe = recipeList.get(minecraft.theWorld.rand.nextInt(recipeList.size()));
                        IRecipeRegistry recipeRegistry = jfmuyRuntime.getRecipeRegistry();
                        IRecipeHandler<IRecipe> recipeHandler = recipeRegistry
                            .getRecipeHandler(randomRecipe.getClass());
                        if (recipeHandler != null) {
                            String recipeInfo = ErrorUtil.getInfoFromRecipe(randomRecipe, recipeHandler);
                            Log.warning("Removing random recipe: {}", recipeInfo);
                            recipeRegistry.removeRecipe(randomRecipe);
                        }
                    }
                }
            } else {
                GuiScreen screen = new GuiInventory(minecraft.thePlayer);
                minecraft.displayGuiScreen(screen);

                IJFMUYRuntime runtime = JFMUYInternalPlugin.jfmuyRuntime;
                if (runtime != null) {
                    IItemListOverlay itemListOverlay = runtime.getItemListOverlay();
                    String filterText = itemListOverlay.getFilterText();
                    itemListOverlay.setFilterText(filterText + " test");
                }
                return true;
            }
        }
        return false;
    }
}
