package ruiseki.jfmuy.gui.overlay.bookmarks.tree;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class RecipeTreeRenderer {

    private final GuiRecipeTree gui;

    public static final int ROW1_HEIGHT = 20;
    public static final int COL_WIDTH = 20;
    public static final int BUTTON_WIDTH = 18;

    public RecipeTreeRenderer(GuiRecipeTree gui) {
        this.gui = gui;
    }

    public void render(Minecraft mc, int mouseX, int mouseY, List<RecipeTreeNode> rootNodes, RecipeBookmarkGroup group,
        float offsetX, float offsetY, float zoomScale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);
        GlStateManager.scale(zoomScale, zoomScale, 1.0f);

        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            drawNodeConnections(root);
        }

        for (RecipeTreeNode root : rootNodes) {
            drawNodeRecipes(mc, root, scaledMouseX, scaledMouseY);
        }

        GlStateManager.popMatrix();

        drawNodeTooltips(mc, mouseX, mouseY, rootNodes, offsetX, offsetY, zoomScale);
        drawMissingIngredientsFooter(mc, mouseX, mouseY, group);
        drawHelpOverlay(mc, zoomScale);
    }

    private void drawHelpOverlay(Minecraft mc, float zoomScale) {
        FontRenderer font = mc.fontRenderer;
        int startX = 10;
        int startY = 10;
        int lineHeight = font.FONT_HEIGHT + 3;

        String zoomText = String.format(Translator.translateToLocal("jfmuy.gui.tree.zoom"), zoomScale * 100.0f);
        font.drawStringWithShadow(zoomText, startX, startY, 0xFFA500);

        String helpZoom = Translator.translateToLocal("jfmuy.gui.tree.help.zoom");
        String helpMoveX = Translator.translateToLocal("jfmuy.gui.tree.help.moveX");
        String helpMoveY = Translator.translateToLocal("jfmuy.gui.tree.help.moveY");
        String helpReset = Translator.translateToLocal("jfmuy.gui.tree.help.reset");

        font.drawStringWithShadow(helpZoom, startX, startY + lineHeight, 0xFFA500);
        font.drawStringWithShadow(helpMoveX, startX, startY + (lineHeight * 2), 0xFFA500);
        font.drawStringWithShadow(helpMoveY, startX, startY + (lineHeight * 3), 0xFFA500);
        font.drawStringWithShadow(helpReset, startX, startY + (lineHeight * 4), 0xFFA500);
    }

    private void drawNodeConnections(RecipeTreeNode node) {
        for (RecipeTreeNode child : node.children) {
            int startX = child.x;
            int startY = child.y + ROW1_HEIGHT / 2;

            int endX = node.x + node.width;
            int endY = node.y + ROW1_HEIGHT / 2;

            int color = 0xFFA0A0A0;
            int midX = startX + (endX - startX) / 2;

            gui.drawHorizontalLine(startX, midX, startY, color);

            int minY = Math.min(startY, endY);
            int maxY = Math.max(startY, endY);
            gui.drawVerticalLine(midX, minY, maxY, color);

            gui.drawHorizontalLine(midX, endX, endY, color);

            drawNodeConnections(child);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void drawNodeRecipes(Minecraft mc, RecipeTreeNode node, int scaledMouseX, int scaledMouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(node.x, node.y, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int relMouseX = scaledMouseX - node.x;
        int relMouseY = scaledMouseY - node.y;

        boolean hasRecipe = (node.recipeLayout != null && node.item.category != null);
        boolean showPreview = node.showRecipePreview;
        Object ingredient = node.item.ingredient;

        int row1X = 0;
        int row1Y = 0;
        int row1Width = COL_WIDTH + COL_WIDTH + BUTTON_WIDTH + 2;
        int previewOffsetY = ROW1_HEIGHT;
        int recipeContentHeight = (node.height > 0) ? node.height : 100;

        int totalWidth = (showPreview && hasRecipe) ? Math.max(row1Width, node.width + 12) : row1Width;
        int totalHeight = (showPreview && hasRecipe) ? (previewOffsetY + recipeContentHeight - 8) : ROW1_HEIGHT;

        gui.drawRectPublic(-1, -1, totalWidth + 1, totalHeight + 1, 0xFF8B8B8B);
        gui.drawRectPublic(0, 0, totalWidth, totalHeight, 0xFFC6C6C6);

        int col1X = row1X + 1;
        int col1Y = row1Y + 1;
        if (ingredient != null) {
            IIngredientType ingredientType = Internal.getIngredientRegistry()
                .getIngredientType(ingredient);
            if (ingredientType != null) {
                IIngredientRenderer renderer = Internal.getIngredientRegistry()
                    .getIngredientRenderer(ingredientType);
                if (renderer != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    renderer.render(mc, col1X + 1, col1Y + 1, ingredient);
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }

        int col2X = col1X + COL_WIDTH;
        int col2Y = row1Y + 1;
        if (hasRecipe && node.item.category != null) {
            IDrawable categoryIcon = node.item.category.getIcon();
            if (categoryIcon != null) {
                categoryIcon.draw(mc, col2X + 1, col2Y + 1);
            } else {
                JFMUYRuntime runtime = Internal.getRuntime();
                if (runtime != null) {
                    List<Object> catalysts = runtime.getRecipeRegistry()
                        .getRecipeCatalysts(node.item.category);
                    if (!catalysts.isEmpty()) {
                        Object catalyst = catalysts.get(0);
                        IIngredientType catType = Internal.getIngredientRegistry()
                            .getIngredientType(catalyst);
                        if (catType != null) {
                            IIngredientRenderer catRenderer = Internal.getIngredientRegistry()
                                .getIngredientRenderer(catType);
                            if (catRenderer != null) {
                                RenderHelper.enableGUIStandardItemLighting();
                                catRenderer.render(mc, col2X + 1, col2Y + 1, catalyst);
                                RenderHelper.disableStandardItemLighting();
                            }
                        }
                    }
                }
            }
        }

        int col3X = col2X + COL_WIDTH;
        int col3Y = row1Y + 1;
        boolean isHoveredButton = (relMouseX >= col3X && relMouseX <= col3X + BUTTON_WIDTH
            && relMouseY >= col3Y
            && relMouseY <= col3Y + 18);

        if (isHoveredButton) {
            gui.drawRectPublic(col3X, col3Y, col3X + BUTTON_WIDTH, col3Y + 18, 0x44FFFFFF);
        }

        String btnText = showPreview ? "-" : "+";
        int textColor = !hasRecipe ? 0xFF777777 : (showPreview ? 0xFFFF5555 : 0xFF55FF55);
        int textWidth = mc.fontRenderer.getStringWidth(btnText);
        mc.fontRenderer.drawStringWithShadow(btnText, col3X + (BUTTON_WIDTH - textWidth) / 2, col3Y + 5, textColor);

        if (showPreview && hasRecipe) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(6, previewOffsetY + 8, 0);

            int recipeRelMouseY = relMouseY - previewOffsetY;

            if (node.item.category.getBackground() != null) {
                node.item.category.getBackground()
                    .draw(mc, 0, 0);
            }

            node.item.category.drawExtras(mc);

            if (node.item.recipe != null) {
                node.item.recipe.drawInfo(mc, node.width, recipeContentHeight, relMouseX - 6, recipeRelMouseY);
            }

            RenderHelper.enableGUIStandardItemLighting();
            if (node.recipeLayout instanceof RecipeLayout layout) {
                layout.setPosition(0, 0);
                layout.drawRecipe(mc, relMouseX - 6, recipeRelMouseY);
            }
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        for (RecipeTreeNode child : node.children) {
            drawNodeRecipes(mc, child, scaledMouseX, scaledMouseY);
        }
    }

    private void drawNodeTooltips(Minecraft mc, int mouseX, int mouseY, List<RecipeTreeNode> rootNodes, float offsetX,
        float offsetY, float zoomScale) {
        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            RecipeTreeNode hovered = gui.getHoveredNode(root, scaledMouseX, scaledMouseY);
            if (hovered != null) {
                int nodeRelMouseX = scaledMouseX - hovered.x;
                int nodeRelMouseY = scaledMouseY - hovered.y;

                boolean hasRecipe = (hovered.recipeLayout != null && hovered.item.category != null);

                if (nodeRelMouseY >= 0 && nodeRelMouseY <= ROW1_HEIGHT) {
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= COL_WIDTH && hovered.item.ingredient != null) {
                        renderIngredientTooltip(mc, hovered.item.ingredient, mouseX, mouseY);
                        break;
                    }

                    if (nodeRelMouseX > COL_WIDTH && nodeRelMouseX <= COL_WIDTH * 2
                        && hasRecipe
                        && hovered.item.category != null) {
                        IDrawable categoryIcon = hovered.item.category.getIcon();
                        if (categoryIcon != null) {
                            String categoryTitle = hovered.item.category.getTitle();
                            if (categoryTitle != null && !categoryTitle.isEmpty()) {
                                gui.drawHoveringText(
                                    Collections.singletonList(categoryTitle),
                                    mouseX,
                                    mouseY,
                                    mc.fontRenderer);
                                break;
                            }
                        } else {
                            JFMUYRuntime runtime = Internal.getRuntime();
                            if (runtime != null) {
                                List<Object> catalysts = runtime.getRecipeRegistry()
                                    .getRecipeCatalysts(hovered.item.category);
                                if (!catalysts.isEmpty()) {
                                    renderIngredientTooltip(mc, catalysts.get(0), mouseX, mouseY);
                                    break;
                                }
                            }
                        }
                    }

                    int col3X = COL_WIDTH * 2;
                    if (nodeRelMouseX > col3X && nodeRelMouseX <= col3X + BUTTON_WIDTH) {
                        String tip = hovered.showRecipePreview
                            ? Translator.translateToLocal("jfmuy.gui.tree.hide_recipe")
                            : Translator.translateToLocal("jfmuy.gui.tree.show_recipe");
                        gui.drawHoveringText(Collections.singletonList(tip), mouseX, mouseY, mc.fontRenderer);
                        break;
                    }
                }

                int previewOffsetY = ROW1_HEIGHT + 8;
                if (hovered.showRecipePreview && hasRecipe && nodeRelMouseY >= previewOffsetY) {
                    if (hovered.recipeLayout instanceof RecipeLayout drawable) {
                        int recipeRelMouseX = nodeRelMouseX - 6;
                        int recipeRelMouseY = nodeRelMouseY - previewOffsetY;

                        GlStateManager.pushMatrix();
                        GlStateManager.translate(
                            offsetX + ((hovered.x + 6) * zoomScale),
                            offsetY + ((hovered.y + previewOffsetY) * zoomScale),
                            0);
                        GlStateManager.scale(zoomScale, zoomScale, 1.0f);

                        RenderHelper.enableGUIStandardItemLighting();
                        drawable.drawOverlays(mc, recipeRelMouseX, recipeRelMouseY);
                        RenderHelper.disableStandardItemLighting();

                        GlStateManager.popMatrix();
                    }
                    break;
                }
                break;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void drawMissingIngredientsFooter(Minecraft mc, int mouseX, int mouseY, RecipeBookmarkGroup group) {
        List<IIngredientListElement> missingList = group.getMissingIngredients();

        if (missingList == null || missingList.isEmpty()) {
            return;
        }

        int itemSize = 18;
        int padding = 4;
        int height = itemSize + (padding * 2);
        int footerY = gui.height - height;

        gui.drawRectPublic(0, footerY, gui.width, gui.height, 0xCC000000);
        gui.drawRectPublic(0, footerY - 1, gui.width, footerY, 0xFF444444);

        String label = Translator.translateToLocal("jfmuy.tooltip.missing_ingredients");
        int labelWidth = mc.fontRenderer.getStringWidth(label);
        mc.fontRenderer
            .drawStringWithShadow(label, padding * 2, footerY + (height - mc.fontRenderer.FONT_HEIGHT) / 2, 0xFF5555);

        int startX = padding * 3 + labelWidth;
        int currentX = startX;

        IIngredientListElement<?> hoveredMissingElement = null;

        RenderHelper.enableGUIStandardItemLighting();

        for (IIngredientListElement<?> element : missingList) {
            if (currentX + itemSize > gui.width - padding) {
                break;
            }

            Object ingredient = element.getIngredient();
            IIngredientType ingredientType = Internal.getIngredientRegistry()
                .getIngredientType(ingredient);

            if (ingredientType != null) {
                IIngredientRenderer renderer = Internal.getIngredientRegistry()
                    .getIngredientRenderer(ingredientType);
                if (renderer != null) {
                    gui.drawRectPublic(
                        currentX - 1,
                        footerY + padding - 1,
                        currentX + itemSize - 1,
                        footerY + padding + itemSize - 1,
                        0xFF373737);

                    renderer.render(mc, currentX, footerY + padding, ingredient);

                    if (mouseX >= currentX && mouseX <= currentX + 16
                        && mouseY >= footerY + padding
                        && mouseY <= footerY + padding + 16) {
                        hoveredMissingElement = element;
                    }
                }
            }

            currentX += itemSize + padding;
        }

        RenderHelper.disableStandardItemLighting();

        if (hoveredMissingElement != null) {
            renderIngredientTooltip(mc, hoveredMissingElement.getIngredient(), mouseX, mouseY);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> void renderIngredientTooltip(Minecraft mc, T ingredient, int mouseX, int mouseY) {
        IIngredientType<T> ingredientType = (IIngredientType<T>) Internal.getIngredientRegistry()
            .getIngredientType(ingredient);
        if (ingredientType != null) {
            IIngredientRenderer<T> renderer = Internal.getIngredientRegistry()
                .getIngredientRenderer(ingredientType);
            if (renderer != null) {
                boolean advancedItemTooltips = mc.gameSettings.advancedItemTooltips;
                List<String> tooltip = renderer.getTooltip(mc, ingredient, advancedItemTooltips);
                if (tooltip != null && !tooltip.isEmpty()) {
                    RenderHelper.enableGUIStandardItemLighting();
                    gui.drawHoveringText(tooltip, mouseX, mouseY, renderer.getFontRenderer(mc, ingredient));
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }
    }
}
