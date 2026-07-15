package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.gui.textures.TextureInfo;

public class DrawableSprite implements IDrawableStatic {

    private final TextureInfo info;

    public DrawableSprite(TextureInfo info) {
        this.info = info;
    }

    @Override
    public int getWidth() {
        return info.getWidth();
    }

    @Override
    public int getHeight() {
        return info.getHeight();
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        draw(minecraft, xOffset, yOffset, 0, 0, 0, 0);
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft,
        int maskRight) {
        ResourceLocation location = info.getLocation();
        TextureAtlasSprite sprite = info.getSprite();
        int textureWidth = info.getWidth();
        int textureHeight = info.getHeight();

        TextureManager textureManager = minecraft.getTextureManager();
        textureManager.bindTexture(location);

        maskTop += info.getTrimTop();
        maskBottom += info.getTrimBottom();
        maskLeft += info.getTrimLeft();
        maskRight += info.getTrimRight();

        int x = xOffset + maskLeft;
        int y = yOffset + maskTop;
        int width = textureWidth - maskRight - maskLeft;
        int height = textureHeight - maskBottom - maskTop;

        if (width <= 0 || height <= 0) {
            return;
        }

        float uSize = sprite.getMaxU() - sprite.getMinU();
        float vSize = sprite.getMaxV() - sprite.getMinV();

        float minU = sprite.getMinU() + uSize * (maskLeft / (float) textureWidth);
        float minV = sprite.getMinV() + vSize * (maskTop / (float) textureHeight);
        float maxU = sprite.getMaxU() - uSize * (maskRight / (float) textureWidth);
        float maxV = sprite.getMaxV() - vSize * (maskBottom / (float) textureHeight);

        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();

        tessellator.addVertexWithUV(x, y + height, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(x + width, y, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(x, y, 0.0D, minU, minV);

        tessellator.draw();
    }
}
