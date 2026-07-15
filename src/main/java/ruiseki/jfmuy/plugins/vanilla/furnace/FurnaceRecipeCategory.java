package ruiseki.jfmuy.plugins.vanilla.furnace;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawableAnimated;
import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public abstract class FurnaceRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {

    protected static final int inputSlot = 0;
    protected static final int fuelSlot = 1;
    protected static final int outputSlot = 2;

    protected final IDrawableStatic staticFlame;
    protected final IDrawableAnimated animatedFlame;
    protected final IDrawableAnimated arrow;

    public FurnaceRecipeCategory(IGuiHelper guiHelper) {
        staticFlame = guiHelper.createDrawable(Reference.RECIPE_GUI_VANILLA, 82, 114, 14, 14);
        animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, IDrawableAnimated.StartDirection.TOP, true);

        arrow = guiHelper.drawableBuilder(Reference.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
            .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }
}
