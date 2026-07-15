package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.gui.IDrawableAnimated;
import ruiseki.jfmuy.api.gui.IDrawableStatic;

public class DrawableBlank implements IDrawableStatic, IDrawableAnimated {

    private final int width;
    private final int height;

    public DrawableBlank(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft,
        int maskRight) {
        // draws nothing
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        // draws nothing
    }
}
