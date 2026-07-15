package ruiseki.jfmuy.plugins.vanilla.furnace;

import javax.annotation.Nullable;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.util.Translator;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {

    private final IDrawableStatic background;
    private final IDrawableStatic flameTransparentBackground;
    private final String localizedName;

    public FurnaceFuelCategory(GuiHelper guiHelper) {
        super(guiHelper);
        background = guiHelper.drawableBuilder(Reference.RECIPE_GUI_VANILLA, 0, 134, 18, 34)
            .addPadding(0, 0, 0, 88)
            .build();

        flameTransparentBackground = guiHelper.getFlameIcon();
        localizedName = Translator.translateToLocal("gui.jei.category.fuel");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.FUEL;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public String getModName() {
        return Reference.MINECRAFT_NAME;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return flameTransparentBackground;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FuelRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(fuelSlot, true, 0, 16);
        guiItemStacks.set(ingredients);
    }
}
