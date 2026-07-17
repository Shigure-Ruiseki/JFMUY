package ruiseki.jfmuy.plugins.jfmuy.info;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiIngredientGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.util.Translator;

public class IngredientInfoRecipeCategory implements IRecipeCategory<IngredientInfoRecipe> {

    public static final int recipeWidth = 160;
    public static final int recipeHeight = 125;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;
    private final String localizedName;

    public IngredientInfoRecipeCategory(GuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
        icon = guiHelper.getInfoIcon();
        slotBackground = guiHelper.getSlotDrawable();
        localizedName = Translator.translateToLocal("gui.jfmuy.category.itemInformation");
    }

    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.INFORMATION;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public String getModName() {
        return Reference.MOD_NAME;
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
    public void setRecipe(IRecipeLayout recipeLayout, IngredientInfoRecipe recipeWrapper, IIngredients ingredients) {

        int xPos = (recipeWidth - 18) / 2;
        for (IIngredientType<?> ingredientType : JFMUYInternalPlugin.ingredientRegistry
            .getRegisteredIngredientTypes()) {
            if (ingredients.getInputs(ingredientType)
                .isEmpty()
                || ingredients.getOutputs(ingredientType)
                    .isEmpty())
                continue;
            IGuiIngredientGroup<?> group = recipeLayout.getIngredientsGroup(ingredientType);
            group.init(0, true, xPos + 1, 1);
            // only render the background for items
            if (ingredientType == VanillaTypes.ITEM) group.setBackground(0, slotBackground);
            group.set(ingredients);
        }
    }
}
