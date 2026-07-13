package ruiseki.jfmuy.gui;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import ruiseki.jfmuy.Reference;

/**
 * Workaround for GuiScreen.drawHoveringText being protected instead of public.
 * (the method with FontRenderer is added by Forge and can't be AT'd).
 */
public class TooltipRenderer {

    private static final TooltipGuiScreen tooltipScreen = new TooltipGuiScreen();

    public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
        @SuppressWarnings("unchecked")
        List<String> textLines = minecraft.fontRenderer
            .listFormattedStringToWidth(textLine, Reference.MAX_TOOLTIP_WIDTH);
        drawHoveringText(minecraft, textLines, x, y, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, @Nonnull List<String> textLines, int x, int y) {
        drawHoveringText(minecraft, textLines, x, y, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, @Nonnull List<String> textLines, int x, int y,
        FontRenderer font) {
        tooltipScreen.set(minecraft);
        tooltipScreen.drawHoveringText(textLines, x, y, font);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    private static class TooltipGuiScreen extends GuiScreen {

        public void set(Minecraft minecraft) {
            this.mc = minecraft;
            this.itemRender = RenderItem.getInstance();
            this.width = minecraft.currentScreen.width;
            this.height = minecraft.currentScreen.height;
        }

        @Override
        public void drawHoveringText(@Nonnull List textLines, int x, int y, FontRenderer font) {
            super.drawHoveringText(textLines, x, y, font);
        }
    }
}
