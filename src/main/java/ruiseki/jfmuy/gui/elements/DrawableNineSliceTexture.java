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

    public void draw(Minecraft mc, int xOffset, int yOffset, int width, int height) {
        ResourceLocation location = info.getLocation();

        int leftWidth = info.getSliceLeft();
        int rightWidth = info.getSliceRight();
        int topHeight = info.getSliceTop();
        int bottomHeight = info.getSliceBottom();

        int textureWidth = info.getWidth();
        int textureHeight = info.getHeight();

        mc.getTextureManager().bindTexture(location);

        float uMin = 0.0F;
        float vMin = 0.0F;
        float uMax = 1.0F;
        float vMax = 1.0F;

        float uLeft = leftWidth / (float) textureWidth;
        float uRight = 1.0F - rightWidth / (float) textureWidth;

        float vTop = topHeight / (float) textureHeight;
        float vBottom = 1.0F - bottomHeight / (float) textureHeight;

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        // left top
        draw(
            tess,
            0F, 0F,
            uLeft, vTop,
            xOffset, yOffset,
            leftWidth, topHeight
        );

        // left bottom
        draw(
            tess,
            0F, vBottom,
            uLeft, 1F,
            xOffset,
            yOffset + height - bottomHeight,
            leftWidth,
            bottomHeight
        );

        // right top
        draw(
            tess,
            uRight, 0F,
            1F, vTop,
            xOffset + width - rightWidth,
            yOffset,
            rightWidth,
            topHeight
        );

        // right bottom
        draw(
            tess,
            uRight, vBottom,
            1F, 1F,
            xOffset + width - rightWidth,
            yOffset + height - bottomHeight,
            rightWidth,
            bottomHeight
        );

        int middleWidth = textureWidth - leftWidth - rightWidth;
        int middleHeight = textureHeight - topHeight - bottomHeight;

        int tiledMiddleWidth = width - leftWidth - rightWidth;
        int tiledMiddleHeight = height - topHeight - bottomHeight;

        if (tiledMiddleWidth > 0) {
            drawTiled(
                tess,
                uLeft, 0F,
                uRight, vTop,
                xOffset + leftWidth,
                yOffset,
                tiledMiddleWidth,
                topHeight,
                middleWidth,
                topHeight
            );

            drawTiled(
                tess,
                uLeft, vBottom,
                uRight, 1F,
                xOffset + leftWidth,
                yOffset + height - bottomHeight,
                tiledMiddleWidth,
                bottomHeight,
                middleWidth,
                bottomHeight
            );
        }

        if (tiledMiddleHeight > 0) {
            drawTiled(
                tess,
                0F, vTop,
                uLeft, vBottom,
                xOffset,
                yOffset + topHeight,
                leftWidth,
                tiledMiddleHeight,
                leftWidth,
                middleHeight
            );

            drawTiled(
                tess,
                uRight, vTop,
                1F, vBottom,
                xOffset + width - rightWidth,
                yOffset + topHeight,
                rightWidth,
                tiledMiddleHeight,
                rightWidth,
                middleHeight
            );
        }

        if (tiledMiddleWidth > 0 && tiledMiddleHeight > 0) {
            drawTiled(
                tess,
                uLeft, vTop,
                uRight, vBottom,
                xOffset + leftWidth,
                yOffset + topHeight,
                tiledMiddleWidth,
                tiledMiddleHeight,
                middleWidth,
                middleHeight
            );
        }

        tess.draw();
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
