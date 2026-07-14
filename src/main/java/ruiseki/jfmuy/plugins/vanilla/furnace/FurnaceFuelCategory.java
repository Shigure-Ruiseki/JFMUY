package ruiseki.jfmuy.plugins.vanilla.furnace;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.Translator;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {

    private final IDrawable background;
    private final IDrawable flame;
    private final String localizedName;

    public FurnaceFuelCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        background = guiHelper.createDrawable(backgroundLocation, 55, 38, 18, 32, 0, 0, 0, 80);

        ResourceLocation recipeBackgroundResource = new ResourceLocation(
            Reference.MOD_ID,
            Reference.TEXTURE_RECIPE_BACKGROUND_PATH);
        flame = guiHelper.createDrawable(recipeBackgroundResource, 215, 0, 14, 14);
        localizedName = Translator.translateToLocal("gui.jfmuy.category.fuel");
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

    @Nullable
    @Override
    public IDrawable getIcon() {
        return flame;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FuelRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(fuelSlot, true, 0, 14);
        guiItemStacks.set(ingredients);
    }
}
