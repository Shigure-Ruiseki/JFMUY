package ruiseki.jfmuy.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Reference;
import ruiseki.okcore.helper.GuiHelpers;

public final class TooltipRenderer {

    private TooltipRenderer() {}

    public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
        @SuppressWarnings("unchecked")
        List<String> textLines = minecraft.fontRenderer
            .listFormattedStringToWidth(textLine, Reference.MAX_TOOLTIP_WIDTH);
        drawHoveringText(null, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, @Nonnull List<String> textLines, int x, int y) {
        drawHoveringText(null, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth) {
        drawHoveringText(null, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, @Nonnull List<String> textLines, int x, int y,
        FontRenderer font) {
        drawHoveringText(null, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, @Nonnull List<String> textLines,
        int x, int y, FontRenderer font) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, @Nonnull List<String> textLines,
        int x, int y, int maxWidth, FontRenderer font) {
        List<String> safeTextLines = new ArrayList<>(textLines.size());
        for (String textLine : textLines) {
            if (textLine != null) {
                safeTextLines.add(textLine);
            }
        }
        if (safeTextLines.isEmpty()) {
            return;
        }

        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        GuiHelpers.drawHoveringText(
            itemStack,
            safeTextLines,
            x,
            y,
            scaledresolution.getScaledWidth(),
            scaledresolution.getScaledHeight(),
            maxWidth,
            font);
    }
}
