package ruiseki.jfmuy.plugins.jfmuy.debug;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IItemListOverlay;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiFluidStackGroup;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.recipe.BlankRecipeCategory;

public class DebugRecipeCategory extends BlankRecipeCategory<DebugRecipe> {

    public static final int recipeWidth = 160;
    public static final int recipeHeight = 60;
    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final String localizedName;
    @Nonnull
    private final IDrawable tankBackground;
    @Nonnull
    private final IDrawable tankOverlay;

    public DebugRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
        localizedName = "debug";

        ResourceLocation backgroundTexture = new ResourceLocation(
            Reference.MOD_ID,
            Reference.TEXTURE_GUI_PATH + "recipeBackground.png");
        tankBackground = guiHelper.createDrawable(backgroundTexture, 176, 0, 20, 55);
        tankOverlay = guiHelper.createDrawable(backgroundTexture, 176, 55, 12, 47);
    }

    @Nonnull
    @Override
    public String getUid() {
        return "debug";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        tankBackground.draw(minecraft);
        IItemListOverlay itemListOverlay = Internal.getRuntime()
            .getItemListOverlay();
        minecraft.fontRenderer.drawString(itemListOverlay.getFilterText(), 20, 52, 0);
        minecraft.fontRenderer.drawString(String.valueOf(itemListOverlay.getStackUnderMouse()), 50, 52, 0);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull DebugRecipe recipeWrapper) {
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

        List<FluidStack> fluidInputs = recipeWrapper.getFluidInputs();
        guiFluidStacks.set(0, fluidInputs.get(0));
        guiFluidStacks.set(1, fluidInputs.get(1));
        guiFluidStacks.set(3, fluidInputs.get(0));
    }
}
