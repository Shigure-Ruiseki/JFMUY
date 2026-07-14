package ruiseki.jfmuy.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.Translator;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {

    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final String localizedName;

    public FurnaceFuelCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        background = guiHelper.createDrawable(backgroundLocation, 55, 38, 18, 32, 0, 0, 0, 80);
        localizedName = Translator.translateToLocal("gui.jfmuy.category.fuel");
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Nonnull
    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.FUEL;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull FuelRecipe recipeWrapper) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(fuelSlot, true, 0, 14);
        guiItemStacks.setFromRecipe(fuelSlot, recipeWrapper.getInputs());
    }
}
