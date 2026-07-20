package ruiseki.jfmuy.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Lists;

import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.render.IngredientListBatchRenderer;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.event.gui.RenderTooltipEvent;
import ruiseki.okcore.helper.GuiHelpers;

public final class TooltipRenderer {

    private TooltipRenderer() {}

    public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
        drawHoveringText(null, minecraft, Lists.newArrayList(textLine), x, y, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y) {
        drawHoveringText(null, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth) {
        drawHoveringText(null, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
        drawHoveringText(null, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth,
        FontRenderer font) {
        drawHoveringText(null, minecraft, textLines, x, y, maxWidth, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y,
        FontRenderer font) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y,
        int maxWidth, FontRenderer font) {
        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        GuiHelpers.drawHoveringText(
            itemStack,
            textLines,
            x,
            y,
            scaledresolution.getScaledWidth(),
            scaledresolution.getScaledHeight(),
            maxWidth,
            font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y,
        int maxWidth) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
    }

    public static <T> void drawHoveringTextAndExtras(ItemStack itemStack, Minecraft minecraft, List<String> textLines,
        int x, int y, FontRenderer font, IIngredientRenderer<T> ingredientRenderer, List<T> allIngredients,
        int activeIndex) {
        drawHoveringTextAndExtras(
            itemStack,
            minecraft,
            textLines,
            x,
            y,
            -1,
            font,
            ingredientRenderer,
            allIngredients,
            activeIndex);
    }

    public static <T> void drawHoveringTextAndExtras(ItemStack stack, Minecraft minecraft, List<String> lines,
        int mouseX, int mouseY, int maxTextWidth, FontRenderer font, IIngredientRenderer<T> ingredientRenderer,
        List<T> allIngredients, int activeIndex) { // <-- Thêm activeIndex ở đây

        int extraWidth = 0;
        int extraHeight = 0;

        if (ingredientRenderer != null) {
            IIngredientRenderer.ExtraSize size = ingredientRenderer
                .renderTooltipExtras(minecraft, mouseX, mouseY, allIngredients, activeIndex, false);
            if (size != null) {
                extraWidth = size.width;
                extraHeight = size.height;
            }
        }

        int nextY = drawTooltipBackgroundAndText(
            stack,
            minecraft,
            lines,
            mouseX,
            mouseY,
            maxTextWidth,
            font,
            extraWidth,
            extraHeight,
            null);

        if (nextY != -1 && ingredientRenderer != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(mouseX + 12, nextY, 300.0F);

            ingredientRenderer.renderTooltipExtras(minecraft, mouseX, mouseY, allIngredients, activeIndex, true);

            GlStateManager.popMatrix();

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    public static void drawHoveringTextAndItems(Minecraft minecraft, List<String> lines,
        List<IngredientListBatchRenderer> itemLines, int mouseX, int mouseY) {
        drawHoveringTextAndItems(null, minecraft, lines, itemLines, mouseX, mouseY, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringTextAndItems(Minecraft minecraft, List<String> lines,
        List<IngredientListBatchRenderer> itemLines, int mouseX, int mouseY, FontRenderer font) {
        drawHoveringTextAndItems(null, minecraft, lines, itemLines, mouseX, mouseY, -1, font);
    }

    public static void drawHoveringTextAndItems(ItemStack stack, Minecraft minecraft, List<String> lines,
        List<IngredientListBatchRenderer> itemLines, int mouseX, int mouseY) {
        drawHoveringTextAndItems(stack, minecraft, lines, itemLines, mouseX, mouseY, -1, minecraft.fontRenderer);
    }

    public static void drawHoveringTextAndItems(ItemStack stack, Minecraft minecraft, List<String> lines,
        List<IngredientListBatchRenderer> itemLines, int mouseX, int mouseY, int maxTextWidth, FontRenderer font) {

        int extraHeight = 0;
        int extraWidth = 0;
        if (!itemLines.isEmpty()) {
            for (IngredientListBatchRenderer renderer : itemLines) {
                extraHeight += renderer.getHeight();
                extraWidth = Math.max(extraWidth, renderer.getWidth());
            }
        }

        int nextY = drawTooltipBackgroundAndText(
            stack,
            minecraft,
            lines,
            mouseX,
            mouseY,
            maxTextWidth,
            font,
            extraWidth,
            extraHeight,
            itemLines);

        if (nextY != -1 && !itemLines.isEmpty()) {
            int tooltipX = mouseX + 12;
            for (IngredientListBatchRenderer line : itemLines) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(tooltipX, nextY, 300.0F);
                line.render(minecraft);
                GlStateManager.popMatrix();
                nextY += line.getHeight();
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    private static int drawTooltipBackgroundAndText(ItemStack stack, Minecraft minecraft, List<String> lines,
        int mouseX, int mouseY, int maxTextWidth, FontRenderer font, int extraWidth, int extraHeight,
        List<IngredientListBatchRenderer> itemLines) {

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
            return -1;
        }

        mouseX = event.getX();
        mouseY = event.getY();
        screenWidth = event.getScreenWidth();
        screenHeight = event.getScreenHeight();
        maxTextWidth = event.getMaxWidth();
        font = event.getFontRenderer();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int tooltipTextWidth = 0;
        for (String line : lines) {
            int textLineWidth = font.getStringWidth(line);
            if (textLineWidth > tooltipTextWidth) {
                tooltipTextWidth = textLineWidth;
            }
        }

        tooltipTextWidth = Math.max(tooltipTextWidth, extraWidth);

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

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String textLine = lines.get(i);
                List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                if (i == 0) {
                    titleLinesCount = wrappedLine.size();
                }
                for (String line : wrappedLine) {
                    int lineWidth = font.getStringWidth(line);
                    if (lineWidth > wrappedTooltipWidth) {
                        wrappedTooltipWidth = lineWidth;
                    }
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = Math.max(wrappedTooltipWidth, extraWidth);
            lines = wrappedTextLines;

            if (mouseX > screenWidth / 2) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
            } else {
                tooltipX = mouseX + 12;
            }

            if (itemLines != null) {
                for (IngredientListBatchRenderer renderer : itemLines) {
                    renderer.moveSlotsToFit(tooltipTextWidth);
                }
            }
        } else if (itemLines != null) {
            for (IngredientListBatchRenderer renderer : itemLines) {
                renderer.moveSlotsToFit(screenWidth / 2);
                tooltipTextWidth = Math.max(tooltipTextWidth, renderer.getWidth());
            }
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (lines.size() > 1) {
            for (int i = 1; i < lines.size(); i++) {
                tooltipHeight += 10;
            }
            if (lines.size() > titleLinesCount) {
                tooltipHeight += 2;
            }
        }

        tooltipHeight += extraHeight;

        if (tooltipY < 4) {
            tooltipY = 4;
        } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 4;
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
            tooltipY - 3 + 1,
            tooltipX - 3 + 1,
            tooltipY + tooltipHeight + 3 - 1,
            borderColorStart,
            borderColorEnd);
        GuiHelpers.drawGradientRect(
            zLevel,
            tooltipX + tooltipTextWidth + 2,
            tooltipY - 3 + 1,
            tooltipX + tooltipTextWidth + 3,
            tooltipY + tooltipHeight + 3 - 1,
            borderColorStart,
            borderColorEnd);
        GuiHelpers.drawGradientRect(
            zLevel,
            tooltipX - 3,
            tooltipY - 3,
            tooltipX + tooltipTextWidth + 3,
            tooltipY - 3 + 1,
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

        for (int lineNumber = 0; lineNumber < lines.size(); ++lineNumber) {
            font.drawStringWithShadow(lines.get(lineNumber), tooltipX, tooltipY, -1);
            tooltipY += 10;
            if (lineNumber + 1 == titleLinesCount) {
                tooltipY += 2;
            }
        }

        MinecraftForge.EVENT_BUS.post(
            new RenderTooltipEvent.PostText(stack, lines, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));

        return tooltipY;
    }
}
