package ruiseki.jfmuy.gui.overlay.bookmarks.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;

import net.minecraft.util.EnumChatFormatting;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.startup.ForgeModIdHelper;
import ruiseki.jfmuy.util.LegacyUtil;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class RecipeTreeRenderer {

    public static final int ROW_HEIGHT = 20;
    public static final int COL_WIDTH = 20;

    private static final int COLOR_LINE = 0xFFFFFFFF;
    private static final int COLOR_BORDER = 0xFFFFFFFF;
    private static final int COLOR_HELP_TEXT = 0xFFA500;

    private final GuiRecipeTree gui;

    public RecipeTreeRenderer(GuiRecipeTree gui) {
        this.gui = gui;
    }

    public void render(Minecraft mc, int mouseX, int mouseY, List<RecipeTreeNode> rootNodes, RecipeBookmarkGroup group,
        float offsetX, float offsetY, float zoomScale) {
        int screenWidth = mc.displayWidth;
        int screenHeight = mc.displayHeight;

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);
        GlStateManager.scale(zoomScale, zoomScale, 1.0f);

        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            drawNodeConnections(root);
        }

        for (RecipeTreeNode root : rootNodes) {
            drawNodeRecipes(
                mc,
                root,
                scaledMouseX,
                scaledMouseY,
                offsetX,
                offsetY,
                zoomScale,
                screenWidth,
                screenHeight);
        }

        GlStateManager.popMatrix();

        drawNodeTooltips(mc, mouseX, mouseY, rootNodes, offsetX, offsetY, zoomScale);
        drawHelpOverlay(mc, zoomScale);
    }

    private void drawHelpOverlay(Minecraft mc, float zoomScale) {
        FontRenderer font = mc.fontRenderer;
        int startX = 10;
        int startY = 10;
        int lineHeight = font.FONT_HEIGHT + 3;

        String zoomText = String.format(Translator.translateToLocal("jfmuy.gui.tree.zoom"), zoomScale * 100.0f);
        font.drawStringWithShadow(zoomText, startX, startY, COLOR_HELP_TEXT);

        font.drawStringWithShadow(
            Translator.translateToLocal("jfmuy.gui.tree.help.zoom"),
            startX,
            startY + lineHeight,
            COLOR_HELP_TEXT);
        font.drawStringWithShadow(
            Translator.translateToLocal("jfmuy.gui.tree.help.moveX"),
            startX,
            startY + (lineHeight * 2),
            COLOR_HELP_TEXT);
        font.drawStringWithShadow(
            Translator.translateToLocal("jfmuy.gui.tree.help.moveY"),
            startX,
            startY + (lineHeight * 3),
            COLOR_HELP_TEXT);
        font.drawStringWithShadow(
            Translator.translateToLocal("jfmuy.gui.tree.help.reset"),
            startX,
            startY + (lineHeight * 4),
            COLOR_HELP_TEXT);
    }

    private void drawNodeConnections(RecipeTreeNode node) {
        if (node.children.isEmpty()) {
            return;
        }

        int parentCenterX = node.x + node.width / 2;
        int parentBottomY = node.y + node.height;

        int minChildX = Integer.MAX_VALUE;
        int maxChildX = Integer.MIN_VALUE;

        int firstChildY = node.children.getFirst().y;
        int midY = parentBottomY + (firstChildY - parentBottomY) / 2;

        gui.drawVerticalLine(parentCenterX, parentBottomY, midY, COLOR_LINE);

        for (RecipeTreeNode child : node.children) {
            int childCenterX = child.x + child.width / 2;
            int childTopY = child.y;

            minChildX = Math.min(minChildX, childCenterX);
            maxChildX = Math.max(maxChildX, childCenterX);

            gui.drawVerticalLine(childCenterX, midY, childTopY, COLOR_LINE);

            drawNodeConnections(child);
        }

        minChildX = Math.min(minChildX, parentCenterX);
        maxChildX = Math.max(maxChildX, parentCenterX);

        gui.drawHorizontalLine(minChildX, maxChildX, midY, COLOR_LINE);
    }

    private void drawNodeRecipes(Minecraft mc, RecipeTreeNode node, int scaledMouseX, int scaledMouseY, float offsetX,
        float offsetY, float zoomScale, int screenWidth, int screenHeight) {
        float screenNodeX = (node.x * zoomScale) + offsetX;
        float screenNodeY = (node.y * zoomScale) + offsetY;
        float screenNodeWidth = node.width * zoomScale;
        float screenNodeHeight = node.height * zoomScale;

        boolean isVisible = (screenNodeX + screenNodeWidth >= 0) && (screenNodeX <= screenWidth)
            && (screenNodeY + screenNodeHeight >= 0)
            && (screenNodeY <= screenHeight);

        if (isVisible) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(node.x, node.y, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (node.hasSecondColumn()) {
                drawNodeBorder(node.width, node.height);
                drawCategoryOrCatalyst(mc, node);
            }

            int col1X = 1;
            int col1Y = 1;
            int renderIngredientX = node.hasSecondColumn() ? (col1X + COL_WIDTH + 1) : (col1X + 1);
            int renderIngredientY = col1Y + 1;

            drawIngredient(mc, node.item, renderIngredientX, renderIngredientY);

            GlStateManager.popMatrix();
        }

        for (RecipeTreeNode child : node.children) {
            drawNodeRecipes(
                mc,
                child,
                scaledMouseX,
                scaledMouseY,
                offsetX,
                offsetY,
                zoomScale,
                screenWidth,
                screenHeight);
        }
    }

    private void drawNodeBorder(int width, int height) {
        gui.drawRectPublic(0, 0, width, 1, COLOR_BORDER);
        gui.drawRectPublic(0, height - 1, width, height, COLOR_BORDER);
        gui.drawRectPublic(0, 0, 1, height, COLOR_BORDER);
        gui.drawRectPublic(width - 1, 0, width, height, COLOR_BORDER);
    }

    private void drawCategoryOrCatalyst(Minecraft mc, RecipeTreeNode node) {
        if (node.item == null || node.item.category == null) return;

        IDrawable categoryIcon = node.item.category.getIcon();
        if (categoryIcon != null) {
            categoryIcon.draw(mc, 2, 2);
            return;
        }

        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) return;

        List<Object> catalysts = runtime.getRecipeRegistry()
            .getRecipeCatalysts(node.item.category);
        if (catalysts != null && !catalysts.isEmpty()) {
            drawIngredient(mc, catalysts.getFirst(), 2, 2);
        }
    }

    private <T> void drawIngredient(Minecraft mc, T ingredient, int x, int y) {
        if (ingredient == null) return;

        IIngredientRegistry registry = Internal.getIngredientRegistry();
        if (registry == null) return;

        IIngredientType<T> ingredientType = registry.getIngredientType(ingredient);
        if (ingredientType == null) return;

        IIngredientRenderer<T> renderer = registry.getIngredientRenderer(ingredientType);
        if (renderer != null) {
            renderer.render(mc, x, y, ingredient);
        }
    }

    private void drawNodeTooltips(Minecraft mc, int mouseX, int mouseY, List<RecipeTreeNode> rootNodes, float offsetX,
        float offsetY, float zoomScale) {
        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            RecipeTreeNode hovered = gui.getHoveredNode(root, scaledMouseX, scaledMouseY);
            if (hovered == null) continue;

            int nodeRelMouseX = scaledMouseX - hovered.x;
            int nodeRelMouseY = scaledMouseY - hovered.y;

            if (nodeRelMouseY >= 0 && nodeRelMouseY <= ROW_HEIGHT) {
                if (hovered.hasSecondColumn()) {
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= COL_WIDTH) {
                        if (renderCategoryTooltip(mc, hovered, mouseX, mouseY)) break;
                    }

                    if (nodeRelMouseX > COL_WIDTH && nodeRelMouseX <= hovered.width
                        && hovered.item.getIngredient() != null) {
                        renderIngredientTooltip(mc, hovered.item.getIngredient(), mouseX, mouseY);
                        break;
                    }
                } else {
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= hovered.width && hovered.item.getIngredient() != null) {
                        renderIngredientTooltip(mc, hovered.item.getIngredient(), mouseX, mouseY);
                        break;
                    }
                }
            }
            break;
        }
    }
    private boolean renderCategoryTooltip(Minecraft mc, RecipeTreeNode node, int mouseX, int mouseY) {
        if (node.item == null || node.item.category == null) return false;

        IRecipeCategory<?> category = node.item.category;
        IDrawable categoryIcon = category.getIcon();

        if (categoryIcon != null) {
            List<String> tooltip = new ArrayList<>();
            String title = category.getTitle();
            if (title != null && !title.isEmpty()) {
                tooltip.add(title);
            }

            if (mc.gameSettings.advancedItemTooltips) {
                tooltip.add(EnumChatFormatting.DARK_GRAY + category.getUid());
            }

            String modName = LegacyUtil.getModName(category);
            if (modName != null) {
                modName = ForgeModIdHelper.getInstance()
                    .getFormattedModNameForModId(modName);
                if (modName != null) {
                    tooltip.add(modName);
                }
            }

            if (!tooltip.isEmpty()) {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();

                gui.drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);

                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
                return true;
            }
        } else {
            JFMUYRuntime runtime = Internal.getRuntime();
            if (runtime != null) {
                List<Object> catalysts = runtime.getRecipeRegistry()
                    .getRecipeCatalysts(category);
                if (catalysts != null && !catalysts.isEmpty()) {
                    renderIngredientTooltip(mc, catalysts.getFirst(), mouseX, mouseY);
                    return true;
                }
            }
        }
        return false;
    }

    public <T> void renderIngredientTooltip(Minecraft mc, T ingredient, int mouseX, int mouseY) {
        IIngredientRegistry registry = Internal.getIngredientRegistry();
        IIngredientType<T> ingredientType = registry.getIngredientType(ingredient);
        if (ingredientType == null) return;

        IIngredientRenderer<T> renderer = registry.getIngredientRenderer(ingredientType);
        IIngredientHelper<T> helper = registry.getIngredientHelper(ingredientType);
        if (renderer == null) return;

        boolean advancedItemTooltips = mc.gameSettings.advancedItemTooltips;
        List<String> tooltip = renderer.getTooltip(mc, ingredient, advancedItemTooltips);
        tooltip = ForgeModIdHelper.getInstance()
            .addModNameToIngredientTooltip(tooltip, ingredient, helper);
        if (tooltip != null && !tooltip.isEmpty()) {
            FontRenderer fontRenderer = renderer.getFontRenderer(mc, ingredient);
            RenderHelper.enableGUIStandardItemLighting();
            TooltipRenderer.drawHoveringTextWithFavorite(ingredient, null, mc, tooltip, mouseX, mouseY, -1, fontRenderer);
            RenderHelper.disableStandardItemLighting();
        }
    }
}
