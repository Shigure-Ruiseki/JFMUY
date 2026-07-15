package ruiseki.jfmuy.gui.elements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.gui.TooltipRenderer;

public class GuiIconButton extends GuiButton {

    private final Consumer<List<String>> tooltipCallback;
    private final Supplier<IDrawable> iconSupplier;
    private final IMouseClickedButtonCallback mouseClickCallback;

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

    public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
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
