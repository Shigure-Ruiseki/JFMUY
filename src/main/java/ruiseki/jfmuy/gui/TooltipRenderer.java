package ruiseki.jfmuy.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Lists;

import ruiseki.jfmuy.autocrafting.favorites.FavoriteRecipes;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.render.IngredientListBatchRenderer;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.event.gui.RenderTooltipEvent;
import ruiseki.okcore.helper.GuiHelpers;
import ruiseki.okcore.helper.ItemHelpers;

public final class TooltipRenderer {
    private TooltipRenderer() {
    }

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

    public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
        drawHoveringText(ItemHelpers.EMPTY, minecraft, textLines, x, y, maxWidth, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, -1, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        GuiHelpers.drawHoveringText(itemStack, textLines, x, y, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), maxWidth, font);
    }

    public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth) {
        drawHoveringText(itemStack, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
    }

    public static void drawHoveringTextAndItems(Minecraft minecraft, List<String> textLines, List<IngredientListBatchRenderer> itemLines, int x, int y) {
        drawHoveringTextAndItems(ItemHelpers.EMPTY, minecraft, textLines, itemLines, x, y, -1, minecraft.fontRenderer, -1);
    }

    /**
     * Draws the standard Minecraft tooltip, but allows extra {@link IngredientListBatchRenderer} lines
     * to be rendered as item grids below the text lines.
     *
     * @param itemGridMaxWidth the width the item grids are laid out into, or -1 to let them use the
     *                         available screen space. Pass a multiple of
     *                         {@link ruiseki.jfmuy.gui.overlay.IngredientGrid#INGREDIENT_WIDTH} to force a
     *                         fixed number of columns.
     * @return the screen rectangle the tooltip occupies, or null if the tooltip was cancelled by
     *         {@link RenderTooltipEvent.Pre}.
     */
    @Nullable
    public static Rectangle drawHoveringTextAndItems(
        ItemStack stack,
        Minecraft minecraft,
        List<String> lines,
        List<IngredientListBatchRenderer> itemLines,
        int mouseX,
        int mouseY,
        int maxTextWidth,
        FontRenderer font,
        int itemGridMaxWidth
    ) {
        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int screenWidth = scaledresolution.getScaledWidth();
        int screenHeight = scaledresolution.getScaledHeight();
        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, lines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return null;
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

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
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
            tooltipTextWidth = wrappedTooltipWidth;
            lines = wrappedTextLines;
        }

        if (!itemLines.isEmpty()) {
            int gridMaxWidth = itemGridMaxWidth > 0
                ? itemGridMaxWidth
                : (needsWrap ? tooltipTextWidth : screenWidth / 2);
            for (IngredientListBatchRenderer renderer : itemLines) {
                renderer.moveSlotsToFit(gridMaxWidth);
                tooltipTextWidth = Math.max(tooltipTextWidth, renderer.getWidth());
            }
        }

        if (needsWrap) {
            if (mouseX > screenWidth / 2) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
            } else {
                tooltipX = mouseX + 12;
            }
        }

        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            int flippedX = mouseX - 16 - tooltipTextWidth;
            tooltipX = flippedX >= 4 ? flippedX : Math.max(4, screenWidth - tooltipTextWidth - 4);
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

        final int zLevel = 300;
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, lines, tooltipX, tooltipY, font, backgroundColor, borderColorStart, borderColorEnd);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundColor = colorEvent.getBackground();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd, zLevel);
        GuiHelpers.drawGradientRect( tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd, zLevel);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, lines, tooltipX, tooltipY, font, tooltipTextWidth, tooltipHeight));
        int tooltipTop = tooltipY;
        Rectangle tooltipRect = new Rectangle(tooltipX, tooltipTop, tooltipTextWidth, tooltipHeight);

        for (int lineNumber = 0; lineNumber < lines.size(); ++lineNumber) {
            font.drawStringWithShadow(lines.get(lineNumber), tooltipX, tooltipY, -1);
            tooltipY += 10;

            if (lineNumber + 1 == titleLinesCount) {
                tooltipY += 2;
            }
        }
        for (IngredientListBatchRenderer line : itemLines) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(tooltipX, tooltipY, 300.0F);
            line.setRenderOrigin(tooltipX, tooltipY);
            line.render(minecraft);
            GlStateManager.popMatrix();
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

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, lines, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        return tooltipRect;
    }

    @Nullable
    public static Rectangle drawHoveringTextWithFavorite(Object ingredient, Minecraft minecraft, List<String> textLines,
                                                         int x, int y) {
        return drawHoveringTextWithFavorite(ingredient, null, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
    }

    @Nullable
    public static Rectangle drawHoveringTextWithFavorite(Object ingredient, ItemStack itemStack, Minecraft minecraft,
                                                         List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
        List<String> lines = new ArrayList<>(textLines);
        RecipeLayout favoriteEntry = null;

        if (ingredient != null) {
            favoriteEntry = FavoriteRecipes.getRecipeLayout(ingredient);
        }

        int extraWidth = 0;
        int extraHeight = 0;

        if (favoriteEntry != null) {
            extraWidth = favoriteEntry.getRecipeCategory()
                .getBackground()
                .getWidth() + 8;
            extraHeight = favoriteEntry.getRecipeCategory()
                .getBackground()
                .getHeight() + 12;
        }

        int[] result = drawTooltipBackgroundAndText(
            itemStack,
            minecraft,
            lines,
            x,
            y,
            maxWidth,
            font,
            extraWidth,
            extraHeight);

        if (result == null) {
            return null;
        }

        int tooltipX = result[0];
        int nextY = result[1];
        int tooltipWidth = result[2];
        int tooltipHeight = result[3];

        if (favoriteEntry != null) {
            int recipeX = tooltipX + 4;
            int recipeY = nextY + 4;

            favoriteEntry.setPosition(recipeX, recipeY);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 300.0F);

            favoriteEntry.drawRecipe(minecraft, x, y);
            favoriteEntry.drawOverlays(minecraft, x, y);

            GlStateManager.popMatrix();

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.disableStandardItemLighting();

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }

        return new Rectangle(tooltipX - 3, (nextY - tooltipHeight) - 3, tooltipWidth + 6, tooltipHeight + 6);
    }

    @Nullable
    private static int[] drawTooltipBackgroundAndText(
        @Nullable ItemStack stack,
        Minecraft minecraft,
        List<String> lines,
        int mouseX,
        int mouseY,
        int maxTextWidth,
        FontRenderer font,
        int extraWidth,
        int extraHeight
    ) {
        ScaledResolution scaledresolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int screenWidth = scaledresolution.getScaledWidth();
        int screenHeight = scaledresolution.getScaledHeight();

        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, lines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return null;
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

        boolean needsWrap = false;
        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;

        if (tooltipX + Math.max(tooltipTextWidth, extraWidth) + 4 > screenWidth) {
            tooltipX = mouseX - 16 - Math.max(tooltipTextWidth, extraWidth);
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
            tooltipTextWidth = wrappedTooltipWidth;
            lines = wrappedTextLines;
        }

        int tooltipWidth = Math.max(tooltipTextWidth, extraWidth);
        int tooltipHeight = 8 + extraHeight;

        if (lines.size() > 1) {
            for (int i = 1; i < lines.size(); i++) {
                tooltipHeight += 10;
            }
            if (lines.size() > titleLinesCount) {
                tooltipHeight += 2;
            }
        }

        if (needsWrap) {
            if (mouseX > screenWidth / 2) {
                tooltipX = mouseX - 16 - tooltipWidth;
            } else {
                tooltipX = mouseX + 12;
            }
        }

        if (tooltipX + tooltipWidth + 4 > screenWidth) {
            int flippedX = mouseX - 16 - tooltipWidth;
            tooltipX = flippedX >= 4 ? flippedX : Math.max(4, screenWidth - tooltipWidth - 4);
        }

        int tooltipY = mouseY - 12;
        if (tooltipY < 4) {
            tooltipY = 4;
        } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 4;
        }

        final int zLevel = 300;
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, lines, tooltipX, tooltipY, font, backgroundColor, borderColorStart, borderColorEnd);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundColor = colorEvent.getBackground();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();

        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd, zLevel);
        GuiHelpers.drawGradientRect( tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart, zLevel);
        GuiHelpers.drawGradientRect( tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd, zLevel);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, lines, tooltipX, tooltipY, font, tooltipWidth, tooltipHeight));
        int tooltipTop = tooltipY;

        for (int lineNumber = 0; lineNumber < lines.size(); ++lineNumber) {
            font.drawStringWithShadow(lines.get(lineNumber), tooltipX, tooltipY, -1);
            tooltipY += 10;
            if (lineNumber + 1 == titleLinesCount) {
                tooltipY += 2;
            }
        }

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, lines, tooltipX, tooltipTop, font, tooltipWidth, tooltipHeight));

        return new int[]{tooltipX, tooltipY, tooltipWidth, tooltipHeight};
    }
}
