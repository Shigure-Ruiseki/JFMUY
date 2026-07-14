package ruiseki.jfmuy.plugins.jfmuy.description;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.BlankRecipeCategory;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.Translator;

public class ItemDescriptionRecipeCategory extends BlankRecipeCategory<ItemDescriptionRecipe> {

    public static final int recipeWidth = 160;
    public static final int recipeHeight = 125;
    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;

    public ItemDescriptionRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
        ResourceLocation recipeBackgroundResource = new ResourceLocation(
            Reference.MOD_ID,
            Reference.TEXTURE_RECIPE_BACKGROUND_PATH);
        icon = guiHelper.createDrawable(recipeBackgroundResource, 196, 39, 16, 16);
        localizedName = Translator.translateToLocal("gui.jfmuy.category.itemDescription");
    }

    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.DESCRIPTION;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemDescriptionRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        int xPos = (recipeWidth - 18) / 2;
        guiItemStacks.init(0, true, xPos, 0);
        guiItemStacks.set(ingredients);
    }
}
