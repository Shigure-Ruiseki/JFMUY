package ruiseki.jfmuy.plugins.jfmuy.debug;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IItemListOverlay;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiFluidStackGroup;
import ruiseki.jfmuy.api.gui.IGuiIngredientGroup;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.BlankRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredient;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientRenderer;

public class DebugRecipeCategory extends BlankRecipeCategory<DebugRecipe> {

    public static final int recipeWidth = 160;
    public static final int recipeHeight = 60;
    private final IDrawable background;
    private final String localizedName;
    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;

    public DebugRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
        localizedName = "debug";

        ResourceLocation backgroundTexture = new ResourceLocation(
            Reference.MOD_ID,
            Reference.TEXTURE_RECIPE_BACKGROUND_PATH);
        tankBackground = guiHelper.createDrawable(backgroundTexture, 176, 0, 20, 55);
        tankOverlay = guiHelper.createDrawable(backgroundTexture, 176, 55, 12, 47);
    }

    @Override
    public String getUid() {
        return "debug";
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        tankBackground.draw(minecraft);
        IJFMUYRuntime runtime = JFMUYInternalPlugin.jfmuyRuntime;
        if (runtime != null) {
            IItemListOverlay itemListOverlay = runtime.getItemListOverlay();
            minecraft.fontRenderer.drawString(itemListOverlay.getFilterText(), 20, 52, 0);
            ItemStack stackUnderMouse = itemListOverlay.getStackUnderMouse();
            if (stackUnderMouse != null) {
                IIngredientRegistry ingredientRegistry = JFMUYInternalPlugin.ingredientRegistry;
                if (ingredientRegistry != null) {
                    IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry
                        .getIngredientHelper(stackUnderMouse);
                    String jeiUid = ingredientHelper.getUniqueId(stackUnderMouse);
                    minecraft.fontRenderer.drawString(jeiUid, 50, 52, 0);
                }
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, DebugRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.addTooltipCallback(new ITooltipCallback<ItemStack>() {

            @Override
            public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
                if (input) {
                    tooltip.add(slotIndex + " Input itemStack");
                } else {
                    tooltip.add(slotIndex + " Output itemStack");
                }
            }
        });

        guiItemStacks.init(0, false, 70, 0);
        guiItemStacks.init(1, true, 110, 0);
        guiItemStacks.set(0, new ItemStack(Items.water_bucket));
        guiItemStacks.set(1, new ItemStack(Items.lava_bucket));

        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
        guiFluidStacks.addTooltipCallback(new ITooltipCallback<FluidStack>() {

            @Override
            public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) {
                if (input) {
                    tooltip.add(slotIndex + " Input fluidStack");
                } else {
                    tooltip.add(slotIndex + " Output fluidStack");
                }
            }
        });

        guiFluidStacks.init(0, true, 4, 4, 12, 47, 2000, true, tankOverlay);
        guiFluidStacks.init(1, true, 24, 0, 12, 47, 16000, true, null);
        guiFluidStacks.init(2, false, 50, 0, 24, 24, 2000, true, tankOverlay);
        guiFluidStacks.init(3, false, 90, 0, 12, 47, 100, false, tankOverlay);

        IGuiIngredientGroup<DebugIngredient> debugIngredientsGroup = recipeLayout
            .getIngredientsGroup(DebugIngredient.class);
        debugIngredientsGroup.addTooltipCallback(new ITooltipCallback<DebugIngredient>() {

            @Override
            public void onTooltip(int slotIndex, boolean input, DebugIngredient ingredient, List<String> tooltip) {
                if (input) {
                    tooltip.add(slotIndex + " Input DebugIngredient");
                } else {
                    tooltip.add(slotIndex + " Output DebugIngredient");
                }
            }
        });

        DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer();
        debugIngredientsGroup.init(0, true, ingredientRenderer, 40, 0, 16, 16, 0, 0);
        debugIngredientsGroup.init(1, false, ingredientRenderer, 40, 16, 16, 16, 0, 0);
        debugIngredientsGroup.init(2, false, ingredientRenderer, 40, 32, 16, 16, 0, 0);

        debugIngredientsGroup.set(ingredients);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Collections.singletonList("Debug Recipe Category Tooltip");
    }
}
