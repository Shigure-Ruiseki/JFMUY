package ruiseki.jfmuy.api.gui;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

/**
 * Represents something to be drawn on screen.
 * Useful for drawing miscellaneous things in {@link IRecipeCategory#drawExtras(Minecraft)} and
 * {@link IRecipeWrapper#drawInfo(Minecraft, int, int, int, int)}.
 * {@link IGuiHelper} has many functions to create IDrawables.
 *
 * @see IDrawableAnimated
 * @see IDrawableStatic
 */
public interface IDrawable {

    int getWidth();

    int getHeight();

    void draw(Minecraft minecraft);

    void draw(Minecraft minecraft, int xOffset, int yOffset);

}
