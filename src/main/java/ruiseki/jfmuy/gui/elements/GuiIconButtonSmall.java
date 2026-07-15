package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.helper.GuideHelpers;

/**
 * A small gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButtonSmall extends GuiButton {

    private final IDrawable icon;

    public GuiIconButtonSmall(int buttonId, int x, int y, int widthIn, int heightIn, IDrawable icon) {
        super(buttonId, x, y, widthIn, heightIn, "");
        this.icon = icon;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);
            GuiHelper guiHelper = Internal.getHelpers()
                .getGuiHelper();
            DrawableNineSliceTexture texture = guiHelper.getButtonForState(k);
            texture.draw(mc, this.xPosition, this.yPosition, this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);

            int color = 14737632;
            if (!this.enabled) {
                color = 10526880;
            } else if (this.field_146123_n) {
                color = 16777120;
            }
            color |= -16777216;

            float red = (float) (color >> 16 & 255) / 255.0F;
            float blue = (float) (color >> 8 & 255) / 255.0F;
            float green = (float) (color & 255) / 255.0F;
            float alpha = (float) (color >> 24 & 255) / 255.0F;
            GlStateManager.color(red, blue, green, alpha);

            double xOffset = xPosition + (height - this.icon.getWidth()) / 2.0;
            double yOffset = yPosition + (width - this.icon.getHeight()) / 2.0;
            GlStateManager.pushMatrix();
            GlStateManager.translate(xOffset, yOffset, 0);
            this.icon.draw(mc);
            GlStateManager.popMatrix();
        }
    }
}
