package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.gui.IDrawableStatic;

public class DrawableResource implements IDrawableStatic {

    private final ResourceLocation resourceLocation;
    private final int textureWidth;
    private final int textureHeight;

    private final int u;
    private final int v;
    private final int width;
    private final int height;
    private final int paddingTop;
    private final int paddingBottom;
    private final int paddingLeft;
    private final int paddingRight;

    public DrawableResource(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop,
        int paddingBottom, int paddingLeft, int paddingRight, int textureWidth, int textureHeight) {
        this.resourceLocation = resourceLocation;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;

        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    @Override
    public int getWidth() {
        return width + paddingLeft + paddingRight;
    }

    @Override
    public int getHeight() {
        return height + paddingTop + paddingBottom;
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        draw(minecraft, xOffset, yOffset, 0, 0, 0, 0);
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft,
        int maskRight) {
        minecraft.getTextureManager()
            .bindTexture(this.resourceLocation);

        int x = xOffset + this.paddingLeft + maskLeft;
        int y = yOffset + this.paddingTop + maskTop;
        int u = this.u + maskLeft;
        int v = this.v + maskTop;
        int width = this.width - maskRight - maskLeft;
        int height = this.height - maskBottom - maskTop;
        if (width <= 0 || height <= 0) return;
        drawCustomSizedTexture(x, y, u, v, width, height, this.textureWidth, this.textureHeight);
    }

    private static void drawCustomSizedTexture(int x, int y, float u, float v, int width, int height,
        float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        tessellator.addVertexWithUV(x, y + height, 0.0D, u * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, (u + width) * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y, 0.0D, (u + width) * f, v * f1);
        tessellator.addVertexWithUV(x, y, 0.0D, u * f, v * f1);

        tessellator.draw();
    }
}
