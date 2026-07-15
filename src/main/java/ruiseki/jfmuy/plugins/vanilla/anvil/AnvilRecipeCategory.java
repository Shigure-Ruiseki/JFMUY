package ruiseki.jfmuy.plugins.vanilla.anvil;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;

    public AnvilRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(Reference.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
            .addPadding(0, 20, 0, 0)
            .build();
        icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.anvil));
    }

    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.ANVIL;
    }

    @Override
    public String getTitle() {
        return Blocks.anvil.getLocalizedName();
    }

    @Override
    public String getModName() {
        return Reference.MINECRAFT_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AnvilRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 0, 0);
        guiItemStacks.init(1, true, 49, 0);
        guiItemStacks.init(2, false, 107, 0);

        guiItemStacks.set(ingredients);

        AnvilRecipeDisplayData displayData = AnvilRecipeDataCache.getDisplayData(recipeWrapper);
        displayData.setCurrentIngredients(guiItemStacks.getGuiIngredients());
    }
}
