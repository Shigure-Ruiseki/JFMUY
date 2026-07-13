package ruiseki.jfmuy.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.Translator;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory {

    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final String localizedName;

    public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
        background = guiHelper.createDrawable(location, 55, 16, 82, 54);
        localizedName = Translator.translateToLocal("gui.jfmuy.category.smelting");
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {

    }

    @Override
    public void drawAnimations(Minecraft minecraft) {
        flame.draw(minecraft, 2, 20);
        arrow.draw(minecraft, 24, 18);
    }

    @Nonnull
    @Override
    public String getTitle() {
        return localizedName;
    }

    @Nonnull
    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.SMELTING;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(inputSlot, true, 0, 0);
        guiItemStacks.init(outputSlot, false, 60, 18);

        guiItemStacks.setFromRecipe(inputSlot, recipeWrapper.getInputs());
        guiItemStacks.setFromRecipe(outputSlot, recipeWrapper.getOutputs());
    }
}
