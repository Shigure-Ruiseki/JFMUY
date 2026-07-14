package ruiseki.jfmuy.plugins.vanilla.furnace;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.util.Translator;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory<SmeltingRecipe> {

    private final IDrawable background;
    private final String localizedName;

    public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
        background = guiHelper.createDrawable(location, 55, 16, 82, 54);
        localizedName = Translator.translateToLocal("gui.jfmuy.category.smelting");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        flame.draw(minecraft, 2, 20);
        arrow.draw(minecraft, 24, 18);
    }

    @Override
    public String getTitle() {
        return localizedName;
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
