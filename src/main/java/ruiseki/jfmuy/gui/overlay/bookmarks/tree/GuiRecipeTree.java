package ruiseki.jfmuy.gui.overlay.bookmarks.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.ingredients.GuiIngredient;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.input.ClickedIngredient;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class GuiRecipeTree extends GuiScreen {

    private final RecipeBookmarkGroup group;
    private final GuiScreen parentScreen;
    private final List<RecipeTreeNode> rootNodes = new ArrayList<>();

    private static final float DEFAULT_OFFSET_X = 50.0f;
    private static final float DEFAULT_OFFSET_Y = 50.0f;
    private static final float DEFAULT_ZOOM_SCALE = 1.0f;

    private float offsetX = DEFAULT_OFFSET_X;
    private float offsetY = DEFAULT_OFFSET_Y;
    private float zoomScale = DEFAULT_ZOOM_SCALE;

    private int lastMouseX;
    private int lastMouseY;
    private boolean isDragging = false;

    private static final int X_PADDING = 40;
    private static final int Y_PADDING = 15;
    private static final float SCROLL_SPEED = 20.0f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 3.0f;

    public GuiRecipeTree(RecipeBookmarkGroup group, GuiScreen parentScreen) {
        this.group = group;
        this.parentScreen = parentScreen;
    }

    public GuiRecipeTree(RecipeBookmarkGroup group) {
        this(group, null);
    }

    @Override
    public void initGui() {
        super.initGui();
        buildTreeLayout();
    }

    private void buildTreeLayout() {
        rootNodes.clear();
        int currentY = 0;

        Set<RecipeBookmarkItem<?>> childRecipes = new HashSet<>();

        for (BookmarkItem<?> item : group.getItems()) {
            if (item instanceof RecipeBookmarkItem<?>recipeItem) {
                if (recipeItem.secondaryTo == null) {
                    RecipeTreeNode.buildTree(recipeItem, group, childRecipes);
                }
            }
        }

        for (BookmarkItem<?> item : group.getItems()) {
            if (item instanceof RecipeBookmarkItem<?>recipeItem) {
                if (recipeItem.secondaryTo == null && !childRecipes.contains(recipeItem)) {
                    RecipeTreeNode root = RecipeTreeNode.buildTree(recipeItem, group);
                    currentY = root.layout(0, currentY, X_PADDING, Y_PADDING);
                    root.updateXPosition(0, X_PADDING);
                    rootNodes.add(root);
                    currentY += Y_PADDING;
                }
            }
        }
    }

    private void resetView() {
        this.offsetX = DEFAULT_OFFSET_X;
        this.offsetY = DEFAULT_OFFSET_Y;
        this.zoomScale = DEFAULT_ZOOM_SCALE;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);
        GlStateManager.scale(zoomScale, zoomScale, 1.0f);

        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            drawNodeConnections(root);
        }

        for (RecipeTreeNode root : rootNodes) {
            drawNodeRecipes(root, scaledMouseX, scaledMouseY);
        }

        GlStateManager.popMatrix();

        drawNodeTooltips(mouseX, mouseY);

        drawMissingIngredientsFooter(mouseX, mouseY);

        drawHelpOverlay();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHelpOverlay() {
        int startX = 10;
        int startY = 10;
        int lineHeight = fontRendererObj.FONT_HEIGHT + 3;

        String zoomText = String.format(Translator.translateToLocal("jfmuy.gui.tree.zoom"), zoomScale * 100.0f);
        fontRendererObj.drawStringWithShadow(zoomText, startX, startY, 0xFFA500);

        String helpZoom = Translator.translateToLocal("jfmuy.gui.tree.help.zoom");
        String helpMoveX = Translator.translateToLocal("jfmuy.gui.tree.help.moveX");
        String helpMoveY = Translator.translateToLocal("jfmuy.gui.tree.help.moveY");
        String helpReset = Translator.translateToLocal("jfmuy.gui.tree.help.reset");

        fontRendererObj.drawStringWithShadow(helpZoom, startX, startY + lineHeight, 0xFFA500);
        fontRendererObj.drawStringWithShadow(helpMoveX, startX, startY + (lineHeight * 2), 0xFFA500);
        fontRendererObj.drawStringWithShadow(helpMoveY, startX, startY + (lineHeight * 3), 0xFFA500);
        fontRendererObj.drawStringWithShadow(helpReset, startX, startY + (lineHeight * 4), 0xFFA500);
    }

    private void drawNodeConnections(RecipeTreeNode node) {
        for (RecipeTreeNode child : node.children) {
            int startX = child.x;
            int startY = child.y + child.height / 2;

            int endX = node.x + node.width;
            int endY = node.y + node.height / 2;

            int color = 0xFFA0A0A0;
            int midX = startX + (endX - startX) / 2;

            drawHorizontalLine(startX, midX, startY, color);
            drawVerticalLine(midX, Math.min(startY, endY), Math.max(startY, endY), color);
            drawHorizontalLine(midX, endX, endY, color);

            drawNodeConnections(child);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void drawNodeRecipes(RecipeTreeNode node, int scaledMouseX, int scaledMouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(node.x, node.y, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int relMouseX = scaledMouseX - node.x;
        int relMouseY = scaledMouseY - node.y;

        boolean hasRecipe = (node.recipeLayout != null && node.item.category != null);
        Object ingredient = node.item.ingredient;

        if (ingredient != null) {
            int iconY = (node.height - 18) / 2;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, iconY, 0);

            drawRect(-1, -1, 19, 19, 0x80000000);
            drawRect(0, 0, 18, 18, 0xFF373737);

            IIngredientType ingredientType = Internal.getIngredientRegistry()
                .getIngredientType(ingredient);
            if (ingredientType != null) {
                IIngredientRenderer renderer = Internal.getIngredientRegistry()
                    .getIngredientRenderer(ingredientType);
                if (renderer != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    renderer.render(mc, 1, 1, ingredient, (int) node.item.amount);
                    RenderHelper.disableStandardItemLighting();
                }
            }
            GlStateManager.popMatrix();
        }

        if (hasRecipe) {
            int recipeOffsetX = (ingredient != null) ? 26 : 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(recipeOffsetX, 0, 0);

            int recipeRelMouseX = relMouseX - recipeOffsetX;

            if (node.item.category.getBackground() != null) {
                node.item.category.getBackground()
                    .draw(mc, 0, 0);
            }

            node.item.category.drawExtras(mc);

            if (node.item.recipe != null) {
                node.item.recipe.drawInfo(mc, node.width - recipeOffsetX, node.height, recipeRelMouseX, relMouseY);
            }

            RenderHelper.enableGUIStandardItemLighting();
            if (node.recipeLayout instanceof RecipeLayout layout) {
                layout.setPosition(0, 0);
                layout.drawRecipe(mc, recipeRelMouseX, relMouseY);
            }
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        for (RecipeTreeNode child : node.children) {
            drawNodeRecipes(child, scaledMouseX, scaledMouseY);
        }
    }

    private void drawNodeTooltips(int mouseX, int mouseY) {
        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            RecipeTreeNode hovered = getHoveredNode(root, scaledMouseX, scaledMouseY);
            if (hovered != null) {
                int nodeRelMouseX = scaledMouseX - hovered.x;
                int nodeRelMouseY = scaledMouseY - hovered.y;

                boolean hasRecipe = (hovered.recipeLayout != null && hovered.item.category != null);
                int recipeOffsetX = (hovered.item.ingredient != null) ? 26 : 0;

                if (hovered.item.ingredient != null) {
                    int iconY = (hovered.height - 18) / 2;
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= 18
                        && nodeRelMouseY >= iconY
                        && nodeRelMouseY <= iconY + 18) {
                        renderIngredientTooltip(hovered.item.ingredient, mouseX, mouseY);
                        break;
                    }
                }

                if (hasRecipe && hovered.recipeLayout instanceof RecipeLayout drawable) {
                    int recipeRelMouseX = nodeRelMouseX - recipeOffsetX;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(
                        offsetX + ((hovered.x + recipeOffsetX) * zoomScale),
                        offsetY + (hovered.y * zoomScale),
                        0);
                    GlStateManager.scale(zoomScale, zoomScale, 1.0f);

                    RenderHelper.enableGUIStandardItemLighting();
                    drawable.drawOverlays(mc, recipeRelMouseX, nodeRelMouseY);
                    RenderHelper.disableStandardItemLighting();

                    GlStateManager.popMatrix();
                }
                break;
            }
        }
    }

    private <T> void renderIngredientTooltip(T ingredient, int mouseX, int mouseY) {
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
                    drawHoveringText(tooltip, mouseX, mouseY, renderer.getFontRenderer(mc, ingredient));
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }
    }

    private RecipeTreeNode getHoveredNode(RecipeTreeNode node, int relX, int relY) {
        if (relX >= node.x && relX <= node.x + node.width && relY >= node.y && relY <= node.y + node.height) {
            return node;
        }
        for (RecipeTreeNode child : node.children) {
            RecipeTreeNode found = getHoveredNode(child, relX, relY);
            if (found != null) return found;
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void drawMissingIngredientsFooter(int mouseX, int mouseY) {
        List<IIngredientListElement> missingList = group.getMissingIngredients();

        if (missingList == null || missingList.isEmpty()) {
            return;
        }

        int itemSize = 18;
        int padding = 4;
        int height = itemSize + (padding * 2);
        int footerY = this.height - height;

        drawRect(0, footerY, this.width, this.height, 0xCC000000);
        drawRect(0, footerY - 1, this.width, footerY, 0xFF444444);

        String label = Translator.translateToLocal("jfmuy.tooltip.missing_ingredients");
        int labelWidth = fontRendererObj.getStringWidth(label);
        fontRendererObj
            .drawStringWithShadow(label, padding * 2, footerY + (height - fontRendererObj.FONT_HEIGHT) / 2, 0xFF5555);

        int startX = padding * 3 + labelWidth;
        int currentX = startX;

        IIngredientListElement<?> hoveredMissingElement = null;

        RenderHelper.enableGUIStandardItemLighting();

        for (IIngredientListElement<?> element : missingList) {
            if (currentX + itemSize > this.width - padding) {
                break;
            }

            Object ingredient = element.getIngredient();
            IIngredientType ingredientType = Internal.getIngredientRegistry()
                .getIngredientType(ingredient);

            if (ingredientType != null) {
                IIngredientRenderer renderer = Internal.getIngredientRegistry()
                    .getIngredientRenderer(ingredientType);
                if (renderer != null) {
                    drawRect(
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
            renderIngredientTooltip(hoveredMissingElement.getIngredient(), mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 2) {
            resetView();
            return;
        }

        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            RecipeTreeNode hovered = getHoveredNode(root, scaledMouseX, scaledMouseY);
            if (hovered != null) {
                int nodeRelMouseX = scaledMouseX - hovered.x;
                int nodeRelMouseY = scaledMouseY - hovered.y;

                int recipeOffsetX = (hovered.item.ingredient != null) ? 26 : 0;

                if (hovered.item.ingredient != null) {
                    int iconY = (hovered.height - 18) / 2;
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= 18
                        && nodeRelMouseY >= iconY
                        && nodeRelMouseY <= iconY + 18) {
                        JFMUYRuntime runtime = Internal.getRuntime();
                        if (runtime != null) {
                            IFocus.Mode mode = (mouseButton == 1) ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT;
                            runtime.getRecipesGui()
                                .show(new Focus<>(mode, hovered.item.ingredient));
                            return;
                        }
                    }
                }

                if (hovered.recipeLayout instanceof RecipeLayout layoutDrawable) {
                    int recipeRelMouseX = nodeRelMouseX - recipeOffsetX;

                    layoutDrawable.setPosition(0, 0);

                    GuiIngredient<?> guiIngredient = layoutDrawable
                        .getGuiIngredientUnderMouse(recipeRelMouseX, nodeRelMouseY);
                    IClickedIngredient<?> clicked = getIngredientUnderMouse(guiIngredient);
                    JFMUYRuntime runtime = Internal.getRuntime();

                    if (clicked != null && runtime != null) {
                        IFocus.Mode mode = (mouseButton == 1) ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT;
                        runtime.getRecipesGui()
                            .show(new Focus<>(mode, clicked.getValue()));
                        return;
                    }

                    if (layoutDrawable.handleClick(mc, recipeRelMouseX, nodeRelMouseY, mouseButton)) {
                        return;
                    }
                }
                return;
            }
        }

        if (mouseButton == 0) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int scrollDirection = dWheel > 0 ? 1 : -1;

            if (isCtrlKeyDown()) {
                offsetX += scrollDirection * SCROLL_SPEED;
            } else if (isShiftKeyDown()) {
                offsetY += scrollDirection * SCROLL_SPEED;
            } else {
                int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
                int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

                float oldScale = zoomScale;
                if (scrollDirection > 0) {
                    zoomScale = Math.min(MAX_ZOOM, zoomScale + ZOOM_SPEED);
                } else {
                    zoomScale = Math.max(MIN_ZOOM, zoomScale - ZOOM_SPEED);
                }

                float scaleFactor = zoomScale / oldScale;
                offsetX = mouseX - (mouseX - offsetX) * scaleFactor;
                offsetY = mouseY - (mouseY - offsetY) * scaleFactor;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE || KeyBindings.isInventoryCloseKey(keyCode)) {
            closeScreen();
            return;
        }

        int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;

        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            RecipeTreeNode hovered = getHoveredNode(root, scaledMouseX, scaledMouseY);
            if (hovered != null && hovered.recipeLayout instanceof RecipeLayout layoutDrawable) {
                int recipeOffsetX = (hovered.item.ingredient != null) ? 26 : 0;
                int nodeRelMouseX = scaledMouseX - hovered.x;
                int nodeRelMouseY = scaledMouseY - hovered.y;
                int recipeRelMouseX = nodeRelMouseX - recipeOffsetX;

                layoutDrawable.setPosition(0, 0);

                GuiIngredient<?> guiIngredient = layoutDrawable
                    .getGuiIngredientUnderMouse(recipeRelMouseX, nodeRelMouseY);
                IClickedIngredient<?> clicked = getIngredientUnderMouse(guiIngredient);

                if (clicked != null) {
                    JFMUYRuntime runtime = Internal.getRuntime();
                    if (runtime != null) {
                        if (KeyBindings.showRecipe.isActiveAndMatches(keyCode)) {
                            runtime.getRecipesGui()
                                .show(new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue()));
                            return;
                        }
                        if (KeyBindings.showUses.isActiveAndMatches(keyCode)) {
                            runtime.getRecipesGui()
                                .show(new Focus<>(IFocus.Mode.INPUT, clicked.getValue()));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void closeScreen() {
        if (this.parentScreen != null) {
            this.mc.displayGuiScreen(this.parentScreen);
        } else {
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
    }

    public IClickedIngredient<?> getIngredientUnderMouse(GuiIngredient<?> hovered) {
        if (hovered != null) {
            Object ingredientUnderMouse = hovered.getDisplayedIngredient();
            if (ingredientUnderMouse != null) {
                return ClickedIngredient.create(ingredientUnderMouse, hovered.getRect());
            }
        }
        return null;
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        if (state == 0) {
            isDragging = false;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (isDragging) {
            offsetX += (mouseX - lastMouseX);
            offsetY += (mouseY - lastMouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
