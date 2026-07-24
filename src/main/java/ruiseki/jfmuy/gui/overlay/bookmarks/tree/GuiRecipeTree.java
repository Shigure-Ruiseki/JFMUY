package ruiseki.jfmuy.gui.overlay.bookmarks.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.ingredients.GuiIngredient;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.input.ClickedIngredient;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.runtime.JFMUYRuntime;

public class GuiRecipeTree extends GuiScreen {

    private final RecipeBookmarkGroup group;
    private final GuiScreen parentScreen;
    private final List<RecipeTreeNode> rootNodes = new ArrayList<>();
    private final RecipeTreeRenderer renderer;

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
    private static final int Y_PADDING = 30;
    private static final float SCROLL_SPEED = 20.0f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 3.0f;

    public GuiRecipeTree(RecipeBookmarkGroup group, GuiScreen parentScreen) {
        this.group = group;
        this.parentScreen = parentScreen;
        this.renderer = new RecipeTreeRenderer(this);
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

    public void recalculateTreeLayout() {
        int currentY = 0;
        for (RecipeTreeNode root : rootNodes) {
            currentY = root.layout(0, currentY, X_PADDING, Y_PADDING);
            root.updateXPosition(0, X_PADDING);
            currentY += Y_PADDING;
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
        renderer.render(mc, mouseX, mouseY, rootNodes, group, offsetX, offsetY, zoomScale);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public RecipeTreeNode getHoveredNode(RecipeTreeNode node, int relX, int relY) {
        int minX = node.x;
        int minY = node.y;
        int maxX = node.x + node.width;
        int maxY = node.y + node.height;

        if (relX >= minX && relX <= maxX && relY >= minY && relY <= maxY) {
            return node;
        }
        for (RecipeTreeNode child : node.children) {
            RecipeTreeNode found = getHoveredNode(child, relX, relY);
            if (found != null) return found;
        }
        return null;
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

                if (nodeRelMouseY >= 0 && nodeRelMouseY <= RecipeTreeRenderer.ROW1_HEIGHT) {

                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= RecipeTreeRenderer.COL_WIDTH) {
                        if (hovered.item.ingredient != null) {
                            JFMUYRuntime runtime = Internal.getRuntime();
                            if (runtime != null) {
                                IFocus.Mode mode = (mouseButton == 1) ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT;
                                runtime.getRecipesGui()
                                    .show(new Focus<>(mode, hovered.item.ingredient));
                                return;
                            }
                        }
                    }

                    if (nodeRelMouseX > RecipeTreeRenderer.COL_WIDTH
                        && nodeRelMouseX <= RecipeTreeRenderer.COL_WIDTH * 2) {
                        if (hovered.item.category != null) {
                            showCategoryRecipes(hovered.item.category);
                            return;
                        }
                    }

                    int col3X = RecipeTreeRenderer.COL_WIDTH * 2;
                    if (nodeRelMouseX > col3X && nodeRelMouseX <= col3X + RecipeTreeRenderer.BUTTON_WIDTH) {
                        if (GuiScreen.isShiftKeyDown()) {
                            boolean targetState = !hovered.showRecipePreview;
                            hovered.setRecipePreviewRecursive(targetState);
                        } else {
                            hovered.toggleRecipePreview();
                        }
                        recalculateTreeLayout();
                        return;
                    }
                }

                if (hovered.showRecipePreview && nodeRelMouseY > RecipeTreeRenderer.ROW1_HEIGHT + 2) {
                    if (hovered.recipeLayout instanceof RecipeLayout layoutDrawable) {
                        int recipeRelMouseY = nodeRelMouseY - (RecipeTreeRenderer.ROW1_HEIGHT + 2);

                        layoutDrawable.setPosition(0, 0);

                        GuiIngredient<?> guiIngredient = layoutDrawable
                            .getGuiIngredientUnderMouse(nodeRelMouseX, recipeRelMouseY);
                        IClickedIngredient<?> clicked = getIngredientUnderMouse(guiIngredient);
                        JFMUYRuntime runtime = Internal.getRuntime();

                        if (clicked != null && runtime != null) {
                            IFocus.Mode mode = (mouseButton == 1) ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT;
                            runtime.getRecipesGui()
                                .show(new Focus<>(mode, clicked.getValue()));
                            return;
                        }

                        if (layoutDrawable.handleClick(mc, nodeRelMouseX, recipeRelMouseY, mouseButton)) {
                            return;
                        }
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
            if (hovered != null) {
                int nodeRelMouseX = scaledMouseX - hovered.x;
                int nodeRelMouseY = scaledMouseY - hovered.y;

                if (nodeRelMouseY >= 0 && nodeRelMouseY <= RecipeTreeRenderer.ROW1_HEIGHT) {
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= RecipeTreeRenderer.COL_WIDTH
                        && hovered.item.ingredient != null) {
                        JFMUYRuntime runtime = Internal.getRuntime();
                        if (runtime != null) {
                            if (KeyBindings.showRecipe.isActiveAndMatches(keyCode)) {
                                runtime.getRecipesGui()
                                    .show(new Focus<>(IFocus.Mode.OUTPUT, hovered.item.ingredient));
                                return;
                            }
                            if (KeyBindings.showUses.isActiveAndMatches(keyCode)) {
                                runtime.getRecipesGui()
                                    .show(new Focus<>(IFocus.Mode.INPUT, hovered.item.ingredient));
                                return;
                            }
                        }
                    }

                    if (nodeRelMouseX > RecipeTreeRenderer.COL_WIDTH
                        && nodeRelMouseX <= RecipeTreeRenderer.COL_WIDTH * 2
                        && hovered.item.category != null) {
                        if (KeyBindings.showRecipe.isActiveAndMatches(keyCode)
                            || KeyBindings.showUses.isActiveAndMatches(keyCode)) {
                            showCategoryRecipes(hovered.item.category);
                            return;
                        }
                    }
                }

                if (hovered.showRecipePreview && nodeRelMouseY > RecipeTreeRenderer.ROW1_HEIGHT + 2) {
                    if (hovered.recipeLayout instanceof RecipeLayout layoutDrawable) {
                        int recipeRelMouseY = nodeRelMouseY - (RecipeTreeRenderer.ROW1_HEIGHT + 2);

                        layoutDrawable.setPosition(0, 0);

                        GuiIngredient<?> guiIngredient = layoutDrawable
                            .getGuiIngredientUnderMouse(nodeRelMouseX, recipeRelMouseY);
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

                break;
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

    private void showCategoryRecipes(IRecipeCategory<?> category) {
        if (category == null) return;
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime != null) {
            runtime.getRecipesGui()
                .showCategories(Collections.singletonList(category.getUid()));
        }
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

    public void drawHorizontalLine(int startX, int endX, int y, int color) {
        super.drawHorizontalLine(startX, endX, y, color);
    }

    public void drawVerticalLine(int x, int startY, int endY, int color) {
        super.drawVerticalLine(x, startY, endY, color);
    }

    public void drawRectPublic(int left, int top, int right, int bottom, int color) {
        GuiScreen.drawRect(left, top, right, bottom, color);
    }

    public void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
        super.drawHoveringText(textLines, x, y, font);
    }
}
