package ruiseki.jfmuy.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.render.IngredientListBatchRenderer;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.okcore.client.gui.tooltip.ITooltipLine;
import ruiseki.okcore.client.gui.tooltip.TooltipLineHandler;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.event.gui.RenderTooltipEvent;
import ruiseki.okcore.helper.GuiHelpers;
import ruiseki.okcore.helper.ItemHelpers;

public final class TooltipRenderer {

    private TooltipRenderer() {}

    public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
        drawHoveringText(ItemHelpers.EMPTY, minecraft, Lists.newArrayList(textLine), x, y, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y) {
        drawHoveringText(ItemHelpers.EMPTY, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth) {
        drawHoveringText(ItemHelpers.EMPTY, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
        drawHoveringText(ItemHelpers.EMPTY, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth,
        FontRenderer font) {
        drawHoveringText(ItemHelpers.EMPTY, minecraft, textLines, x, y, maxWidth, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y,
        FontRenderer font) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y,
        int maxWidth, FontRenderer font) {
        drawHoveringText(itemStack, itemStack, minecraft, textLines, x, y, maxWidth, font);
    }

    public static <T> void drawHoveringText(T ingredient, ItemStack itemStack, Minecraft minecraft,
        List<String> textLines, int x, int y, FontRenderer font) {
        drawHoveringText(ingredient, itemStack, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(@Nullable Object ingredient, ItemStack itemStack, Minecraft minecraft,
        List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
        List<String> safeTextLines = null; // assume that null string almost never happens, so allocate lazily
        for (int i = 0, textLinesSize = textLines.size(); i < textLinesSize; i++) {
            String textLine = textLines.get(i);
            if (textLine == null && safeTextLines == null) {
                safeTextLines = new ArrayList<>(textLinesSize);
                safeTextLines.addAll(textLines.subList(0, i));
            }
            if (safeTextLines != null && textLine != null) {
                safeTextLines.add(textLine);
            }
        }
        if (safeTextLines == null) {
            safeTextLines = textLines;
        }
        if (safeTextLines.isEmpty()) {
            return;
        }
        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        try {
            GuiHelpers.drawHoveringText(
                itemStack,
                safeTextLines,
                x,
                y,
                scaledresolution.getScaledWidth(),
                scaledresolution.getScaledHeight(),
                maxWidth,
                font);
        } catch (RuntimeException | LinkageError e) {
            String stringTooltip = String.join("\n", safeTextLines);
            String message = String
                .format("Failed to render tooltip for %s:\n%s", getIngredientInfo(ingredient), stringTooltip);
            JFMUY.okLog(Level.ERROR, message, e);
        }
    }

    private static String getIngredientInfo(@Nullable Object ingredient) {
        if (ingredient == null || ingredient == ItemHelpers.EMPTY) {
            return "JFMUY";
        }
        try {
            return ErrorUtil.getIngredientInfo(ingredient);
        } catch (RuntimeException | LinkageError e) {
            return ingredient.getClass()
                .getName();
        }
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y,
        int maxWidth) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
    }

    public static void drawHoveringTextAndItems(Minecraft minecraft, List<String> textLines,
        List<IngredientListBatchRenderer> itemLines, int x, int y) {
        drawHoveringTextAndItems(
            ItemHelpers.EMPTY,
            minecraft,
            textLines,
            itemLines,
            x,
            y,
            -1,
            minecraft.fontRenderer,
            null);
    }

    public static void drawHoveringTextAndItems(ItemStack stack, Minecraft minecraft, List<String> lines,
        List<IngredientListBatchRenderer> itemLines, int mouseX, int mouseY, int maxTextWidth, FontRenderer font,
        @Nullable Consumer<Rectangle> postLayoutHook) {
        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int screenWidth = scaledresolution.getScaledWidth();
        int screenHeight = scaledresolution.getScaledHeight();

        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(
            stack,
            lines,
            mouseX,
            mouseY,
            screenWidth,
            screenHeight,
            maxTextWidth,
            font);

        if (MinecraftForge.EVENT_BUS.post(event)) {
            TooltipLineHandler.clear();
            return;
        }

        mouseX = event.getX();
        mouseY = event.getY();
        screenWidth = event.getScreenWidth();
        screenHeight = event.getScreenHeight();
        maxTextWidth = event.getMaxWidth();
        font = event.getFontRenderer();

        try {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            int tooltipTextWidth = 0;
            boolean hasFlexibleWidth = false;

            for (String textLine : lines) {
                ITooltipLine handler = TooltipLineHandler.getTipLine(textLine);

                if (handler != null) {
                    int width = handler.getWidth(font);

                    if (width < 0) {
                        hasFlexibleWidth = true;
                    } else {
                        tooltipTextWidth = Math.max(tooltipTextWidth, width);
                    }
                } else {
                    tooltipTextWidth = Math.max(tooltipTextWidth, font.getStringWidth(textLine));
                }
            }

            boolean needsWrap = false;
            int titleLinesCount = 1;
            int tooltipX = mouseX + 12;

            if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
                tooltipX = mouseX - 16 - tooltipTextWidth;

                if (tooltipX < 4) {
                    if (mouseX > screenWidth / 2) {
                        tooltipTextWidth = mouseX - 12 - 8;
                    } else {
                        tooltipTextWidth = screenWidth - 16 - mouseX;
                    }

                    needsWrap = true;
                }
            }

            if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }

            if (hasFlexibleWidth) {
                int availableWidth = mouseX > screenWidth / 2 ? mouseX - 20 : screenWidth - 16 - mouseX;

                if (maxTextWidth > 0) {
                    availableWidth = Math.min(availableWidth, maxTextWidth);
                }

                tooltipTextWidth = Math.max(tooltipTextWidth, availableWidth);
            }

            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<String> wrappedTextLines = new ArrayList<>();

                for (int i = 0; i < lines.size(); i++) {
                    String textLine = lines.get(i);
                    ITooltipLine handler = TooltipLineHandler.getTipLine(textLine);

                    if (handler != null) {
                        int width = getTooltipLineWidth(handler, font, tooltipTextWidth);

                        wrappedTooltipWidth = Math.max(wrappedTooltipWidth, width);
                        wrappedTextLines.add(textLine);
                        continue;
                    }

                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);

                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }

                    for (String wrapped : wrappedLine) {
                        wrappedTooltipWidth = Math.max(wrappedTooltipWidth, font.getStringWidth(wrapped));
                        wrappedTextLines.add(wrapped);
                    }
                }

                tooltipTextWidth = wrappedTooltipWidth;
                lines = wrappedTextLines;

                if (mouseX > screenWidth / 2) {
                    tooltipX = mouseX - 16 - tooltipTextWidth;
                } else {
                    tooltipX = mouseX + 12;
                }

                for (IngredientListBatchRenderer renderer : itemLines) {
                    renderer.moveSlotsToFit(tooltipTextWidth);
                }
            } else {
                for (IngredientListBatchRenderer renderer : itemLines) {
                    renderer.moveSlotsToFit(screenWidth / 2);
                    tooltipTextWidth = Math.max(tooltipTextWidth, renderer.getWidth());
                }
            }

            int tooltipY = mouseY - 12;
            int tooltipHeight = 8;

            for (int i = 0; i < lines.size(); i++) {
                String textLine = lines.get(i);
                ITooltipLine handler = TooltipLineHandler.getTipLine(textLine);

                if (handler != null) {
                    int width = getTooltipLineWidth(handler, font, tooltipTextWidth);
                    tooltipHeight += Math.max(0, handler.getHeight(font, width));
                } else {
                    tooltipHeight += 10;
                }

                if (i == titleLinesCount - 1 && i < lines.size() - 1) {
                    tooltipHeight += 2;
                }

                if (i < lines.size() - 1 && textLine.endsWith(TooltipLineHandler.TOOLTIP_LINESPACE)) {
                    tooltipHeight += 2;
                }
            }

            if (!itemLines.isEmpty()) {
                for (IngredientListBatchRenderer renderer : itemLines) {
                    tooltipHeight += renderer.getHeight();
                }
            }

            if (tooltipY < 4) {
                tooltipY = 4;
            } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
                tooltipY = screenHeight - tooltipHeight - 4;
            }

            if (postLayoutHook != null) {
                postLayoutHook.accept(new Rectangle(tooltipX, tooltipY, tooltipTextWidth, tooltipHeight));
            }

            final int zLevel = 300;
            int backgroundColor = 0xF0100010;
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

            RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(
                stack,
                lines,
                tooltipX,
                tooltipY,
                font,
                backgroundColor,
                borderColorStart,
                borderColorEnd);

            MinecraftForge.EVENT_BUS.post(colorEvent);

            backgroundColor = colorEvent.getBackground();
            borderColorStart = colorEvent.getBorderStart();
            borderColorEnd = colorEvent.getBorderEnd();

            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 4,
                tooltipX + tooltipTextWidth + 3,
                tooltipY - 3,
                backgroundColor,
                backgroundColor);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 4,
                backgroundColor,
                backgroundColor);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3,
                backgroundColor,
                backgroundColor);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 4,
                tooltipY - 3,
                tooltipX - 3,
                tooltipY + tooltipHeight + 3,
                backgroundColor,
                backgroundColor);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX + tooltipTextWidth + 3,
                tooltipY - 3,
                tooltipX + tooltipTextWidth + 4,
                tooltipY + tooltipHeight + 3,
                backgroundColor,
                backgroundColor);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 2,
                tooltipX - 2,
                tooltipY + tooltipHeight + 2,
                borderColorStart,
                borderColorEnd);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX + tooltipTextWidth + 2,
                tooltipY - 2,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 2,
                borderColorStart,
                borderColorEnd);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + tooltipTextWidth + 3,
                tooltipY - 2,
                borderColorStart,
                borderColorStart);
            GuiHelpers.drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3,
                borderColorEnd,
                borderColorEnd);

            MinecraftForge.EVENT_BUS.post(
                new RenderTooltipEvent.PostBackground(
                    stack,
                    lines,
                    tooltipX,
                    tooltipY,
                    font,
                    tooltipTextWidth,
                    tooltipHeight));

            int tooltipTop = tooltipY;

            for (int i = 0; i < lines.size(); i++) {
                String textLine = lines.get(i);
                ITooltipLine handler = TooltipLineHandler.getTipLine(textLine);

                if (handler != null) {
                    int width = getTooltipLineWidth(handler, font, tooltipTextWidth);
                    int height = Math.max(0, handler.getHeight(font, width));

                    handler.draw(font, tooltipX, tooltipY, mouseX, mouseY, width, height);

                    tooltipY += height;
                } else {
                    String renderedLine = textLine.endsWith(TooltipLineHandler.TOOLTIP_LINESPACE)
                        ? textLine.substring(0, textLine.length() - TooltipLineHandler.TOOLTIP_LINESPACE.length())
                        : textLine;

                    font.drawStringWithShadow(renderedLine, tooltipX, tooltipY, -1);

                    tooltipY += 10;
                }

                if (i == titleLinesCount - 1 && i < lines.size() - 1) {
                    tooltipY += 2;
                }

                if (i < lines.size() - 1 && textLine.endsWith(TooltipLineHandler.TOOLTIP_LINESPACE)) {
                    tooltipY += 2;
                }
            }

            for (IngredientListBatchRenderer line : itemLines) {
                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(tooltipX, tooltipY, 300.0F);
                    line.setRenderOrigin(tooltipX, tooltipY);
                    line.render(minecraft);
                } finally {
                    GlStateManager.popMatrix();
                }

                tooltipY += line.getHeight();
            }

            if (!itemLines.isEmpty()) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.disableRescaleNormal();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.disableStandardItemLighting();
            }

            MinecraftForge.EVENT_BUS.post(
                new RenderTooltipEvent.PostText(
                    stack,
                    lines,
                    tooltipX,
                    tooltipTop,
                    font,
                    tooltipTextWidth,
                    tooltipHeight));
        } finally {
            TooltipLineHandler.clear();

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    private static int getTooltipLineWidth(ITooltipLine handler, FontRenderer font, int tooltipWidth) {
        int width = handler.getWidth(font);
        return width < 0 ? tooltipWidth : Math.min(width, tooltipWidth);
    }
}
