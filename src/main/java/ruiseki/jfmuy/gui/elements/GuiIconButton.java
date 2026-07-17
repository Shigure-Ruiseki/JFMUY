package ruiseki.jfmuy.gui.elements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.okcore.client.renderer.GlStateManager;

public class GuiIconButton extends GuiButton {

    protected Consumer<List<String>> tooltipCallback;
    protected Supplier<IDrawable> iconSupplier;
    protected IMouseClickedButtonCallback mouseClickCallback;

    public GuiIconButton(int buttonId, IDrawable icon, IMouseClickedButtonCallback mouseClickCallback) {
        this(buttonId, (tooltip) -> {}, () -> icon, mouseClickCallback);
    }

    public GuiIconButton(int buttonId, Consumer<List<String>> tooltipCallback, Supplier<IDrawable> iconSupplier,
        IMouseClickedButtonCallback mouseClickCallback) {
        super(buttonId, 0, 0, 0, 0, "");
        this.tooltipCallback = tooltipCallback;
        this.iconSupplier = iconSupplier;
        this.mouseClickCallback = mouseClickCallback;
    }

    public void updateBounds(Rectangle area) {
        this.xPosition = area.x;
        this.yPosition = area.y;
        this.width = area.width;
        this.height = area.height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.func_146115_a());
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
            GuiHelper guiHelper = Internal.getHelpers()
                .getGuiHelper();
            DrawableNineSliceTexture texture = guiHelper.getButtonForState(i);
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

            IDrawable icon = iconSupplier.get();
            double xOffset = xPosition + (width - icon.getWidth()) / 2.0;
            double yOffset = yPosition + (height - icon.getHeight()) / 2.0;
            GlStateManager.pushMatrix();
            GlStateManager.translate(xOffset, yOffset, 0);
            icon.draw(mc);
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void drawToolTip(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.func_146115_a()) {
            List<String> tooltip = new ArrayList<>();
            this.tooltipCallback.accept(tooltip);
            TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, Reference.MAX_TOOLTIP_WIDTH);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            if (mouseClickCallback.mousePressed(mc, mouseX, mouseY)) {
                this.func_146113_a(mc.getSoundHandler());
                return true;
            }
        }
        return false;
    }
}
