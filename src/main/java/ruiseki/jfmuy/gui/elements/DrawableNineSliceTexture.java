package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.gui.textures.TextureInfo;

/**
 * Breaks a texture into 9 pieces so that it can be scaled to any size.
 * Draws the corners and then repeats any middle textures to fill the remaining area.
 */
public class DrawableNineSliceTexture {

    private final TextureInfo info;

    public DrawableNineSliceTexture(TextureInfo info) {
        this.info = info;
    }

    public void draw(Minecraft minecraft, int xOffset, int yOffset, int width, int height) {
        ResourceLocation location = info.getLocation();
        TextureAtlasSprite sprite = info.getSprite();
        int leftWidth = info.getSliceLeft();
        int rightWidth = info.getSliceRight();
        int topHeight = info.getSliceTop();
        int bottomHeight = info.getSliceBottom();
        int textureWidth = info.getWidth();
        int textureHeight = info.getHeight();

        TextureManager textureManager = minecraft.getTextureManager();
        textureManager.bindTexture(location);

        float uMin = sprite.getMinU();
        float uMax = sprite.getMaxU();
        float vMin = sprite.getMinV();
        float vMax = sprite.getMaxV();
        float uSize = uMax - uMin;
        float vSize = vMax - vMin;

        float uLeft = uMin + uSize * (leftWidth / (float) textureWidth);
        float uRight = uMax - uSize * (rightWidth / (float) textureWidth);
        float vTop = vMin + vSize * (topHeight / (float) textureHeight);
        float vBottom = vMax - vSize * (bottomHeight / (float) textureHeight);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        // left top
        draw(tessellator, uMin, vMin, uLeft, vTop, xOffset, yOffset, leftWidth, topHeight);
        // left bottom
        draw(
            tessellator,
            uMin,
            vBottom,
            uLeft,
            vMax,
            xOffset,
            yOffset + height - bottomHeight,
            leftWidth,
            bottomHeight);
        // right top
        draw(tessellator, uRight, vMin, uMax, vTop, xOffset + width - rightWidth, yOffset, rightWidth, topHeight);
        // right bottom
        draw(
            tessellator,
            uRight,
            vBottom,
            uMax,
            vMax,
            xOffset + width - rightWidth,
            yOffset + height - bottomHeight,
            rightWidth,
            bottomHeight);

        int middleWidth = textureWidth - leftWidth - rightWidth;
        int middleHeight = textureHeight - topHeight - bottomHeight;
        int tiledMiddleWidth = width - leftWidth - rightWidth;
        int tiledMiddleHeight = height - topHeight - bottomHeight;

        if (tiledMiddleWidth > 0) {
            // top edge
            drawTiled(
                tessellator,
                uLeft,
                vMin,
                uRight,
                vTop,
                xOffset + leftWidth,
                yOffset,
                tiledMiddleWidth,
                topHeight,
                middleWidth,
                topHeight);
            // bottom edge
            drawTiled(
                tessellator,
                uLeft,
                vBottom,
                uRight,
                vMax,
                xOffset + leftWidth,
                yOffset + height - bottomHeight,
                tiledMiddleWidth,
                bottomHeight,
                middleWidth,
                bottomHeight);
        }
        if (tiledMiddleHeight > 0) {
            // left side
            drawTiled(
                tessellator,
                uMin,
                vTop,
                uLeft,
                vBottom,
                xOffset,
                yOffset + topHeight,
                leftWidth,
                tiledMiddleHeight,
                leftWidth,
                middleHeight);
            // right side
            drawTiled(
                tessellator,
                uRight,
                vTop,
                uMax,
                vBottom,
                xOffset + width - rightWidth,
                yOffset + topHeight,
                rightWidth,
                tiledMiddleHeight,
                rightWidth,
                middleHeight);
        }
        if (tiledMiddleHeight > 0 && tiledMiddleWidth > 0) {
            // middle area
            drawTiled(
                tessellator,
                uLeft,
                vTop,
                uRight,
                vBottom,
                xOffset + leftWidth,
                yOffset + topHeight,
                tiledMiddleWidth,
                tiledMiddleHeight,
                middleWidth,
                middleHeight);
        }

        tessellator.draw();
    }

    private void drawTiled(Tessellator tessellator, float uMin, float vMin, float uMax, float vMax, int xOffset,
        int yOffset, int tiledWidth, int tiledHeight, int width, int height) {
        int xTileCount = tiledWidth / width;
        int xRemainder = tiledWidth - (xTileCount * width);
        int yTileCount = tiledHeight / height;
        int yRemainder = tiledHeight - (yTileCount * height);

        int yStart = yOffset + tiledHeight;

        float uSize = uMax - uMin;
        float vSize = vMax - vMin;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int tileWidth = (xTile == xTileCount) ? xRemainder : width;
                int tileHeight = (yTile == yTileCount) ? yRemainder : height;
                int x = xOffset + (xTile * width);
                int y = yStart - ((yTile + 1) * height);
                if (tileWidth > 0 && tileHeight > 0) {
                    int maskRight = width - tileWidth;
                    int maskTop = height - tileHeight;
                    float uOffset = (maskRight / (float) width) * uSize;
                    float vOffset = (maskTop / (float) height) * vSize;

                    draw(
                        tessellator,
                        uMin,
                        vMin + vOffset,
                        uMax - uOffset,
                        vMax,
                        x,
                        y + maskTop,
                        tileWidth,
                        tileHeight);
                }
            }
        }
    }

    private static void draw(Tessellator tessellator, float minU, double minV, float maxU, float maxV, int xOffset,
        int yOffset, int width, int height) {
        tessellator.addVertexWithUV(xOffset, yOffset + height, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(xOffset + width, yOffset + height, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(xOffset + width, yOffset, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(xOffset, yOffset, 0.0D, minU, minV);
    }
}
