package ruiseki.jfmuy.plugins.vanilla.furnace;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.Translator;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory<SmeltingRecipe> {

    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;

    public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        background = guiHelper.createDrawable(Reference.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
        icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.furnace));
        localizedName = Translator.translateToLocal("gui.jfmuy.category.smelting");
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
    public void drawExtras(Minecraft minecraft) {
        animatedFlame.draw(minecraft, 1, 20);
        arrow.draw(minecraft, 24, 18);
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public String getModName() {
        return Reference.MINECRAFT_NAME;
    }

    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.SMELTING;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SmeltingRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(inputSlot, true, 0, 0);
        guiItemStacks.init(outputSlot, false, 60, 18);

        guiItemStacks.set(ingredients);
    }
}
