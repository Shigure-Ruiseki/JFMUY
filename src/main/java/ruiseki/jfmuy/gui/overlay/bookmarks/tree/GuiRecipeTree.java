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
import ruiseki.jfmuy.input.ClickedIngredient;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.runtime.JFMUYRuntime;

public class GuiRecipeTree extends GuiScreen {

    private final RecipeBookmarkGroup group;
    private final GuiScreen parentScreen;
    private final List<RecipeTreeNode> rootNodes = new ArrayList<>();
    private final RecipeTreeRenderer renderer = new RecipeTreeRenderer(this);

    private static final int TREE_GAP = 32;

    private static final float DEFAULT_OFFSET_X = 50.0f;
    private static final float DEFAULT_OFFSET_Y = 50.0f;
    private static final float DEFAULT_ZOOM_SCALE = 1.0f;

    private float offsetX = DEFAULT_OFFSET_X;
    private float offsetY = DEFAULT_OFFSET_Y;
    private float zoomScale = DEFAULT_ZOOM_SCALE;

    private int lastMouseX;
    private int lastMouseY;
    private boolean isDragging = false;

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
        centerView();
    }

    public void centerView() {
        if (rootNodes.isEmpty()) {
            resetView();
            return;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (RecipeTreeNode root : rootNodes) {
            int[] bounds = getTreeBounds(root);
            minX = Math.min(minX, bounds[0]);
            maxX = Math.max(maxX, bounds[1]);
            minY = Math.min(minY, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }

        float treeCenterX = (minX + maxX) / 2.0f;
        float treeCenterY = (minY + maxY) / 2.0f;

        this.offsetX = (this.width / 2.0f) - (treeCenterX * zoomScale);
        this.offsetY = (this.height / 2.0f) - (treeCenterY * zoomScale);
    }

    private int[] getTreeBounds(RecipeTreeNode node) {
        int minX = node.x;
        int maxX = node.x + node.width;
        int minY = node.y;
        int maxY = node.y + node.height;

        for (RecipeTreeNode child : node.children) {
            int[] childBounds = getTreeBounds(child);
            minX = Math.min(minX, childBounds[0]);
            maxX = Math.max(maxX, childBounds[1]);
            minY = Math.min(minY, childBounds[2]);
            maxY = Math.max(maxY, childBounds[3]);
        }

        return new int[] { minX, maxX, minY, maxY };
    }

    private void resetView() {
        zoomScale = DEFAULT_ZOOM_SCALE;
        centerView();
    }

    private void buildTreeLayout() {
        rootNodes.clear();
        Set<RecipeBookmarkItem<?>> childRecipes = new HashSet<>();

        for (BookmarkItem<?> item : group.getItems()) {
            if (item instanceof RecipeBookmarkItem<?>recipeItem) {
                if (recipeItem.secondaryTo == null) {
                    RecipeTreeNode.buildTree(recipeItem, group, childRecipes);
                }
            }
        }

        int currentStartX = 0;
        for (BookmarkItem<?> item : group.getItems()) {
            if (item instanceof RecipeBookmarkItem<?>recipeItem) {
                if (recipeItem.secondaryTo == null && !childRecipes.contains(recipeItem)) {
                    RecipeTreeNode root = RecipeTreeNode.buildTree(recipeItem, group);

                    root.layout(currentStartX, 0);
                    root.updateYPosition(0);

                    rootNodes.add(root);

                    currentStartX = root.getMaxX() + TREE_GAP;
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        renderer.render(mc, mouseX, mouseY, rootNodes, group, offsetX, offsetY, zoomScale);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public RecipeTreeNode getHoveredNode(RecipeTreeNode node, int relX, int relY) {
        if (relX >= node.x && relX <= node.x + node.width && relY >= node.y && relY <= node.y + node.height) {
            return node;
        }

        for (RecipeTreeNode child : node.children) {
            RecipeTreeNode found = getHoveredNode(child, relX, relY);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private HoverResult getHoveredResultAt(int mouseX, int mouseY) {
        int scaledMouseX = (int) ((mouseX - offsetX) / zoomScale);
        int scaledMouseY = (int) ((mouseY - offsetY) / zoomScale);

        for (RecipeTreeNode root : rootNodes) {
            RecipeTreeNode hovered = getHoveredNode(root, scaledMouseX, scaledMouseY);
            if (hovered != null) {
                int nodeRelMouseX = scaledMouseX - hovered.x;
                int nodeRelMouseY = scaledMouseY - hovered.y;
                return new HoverResult(hovered, nodeRelMouseX, nodeRelMouseY);
            }
        }
        return null;
    }

    private record HoverResult(RecipeTreeNode node, int relX, int relY) {}

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 2) {
            resetView();
            return;
        }

        Object rawIngredient = renderer.getRawInputIngredientUnderMouse(mc, group, mouseX, mouseY);
        if (rawIngredient != null) {
            JFMUYRuntime runtime = Internal.getRuntime();
            if (runtime != null) {
                IFocus.Mode mode = (mouseButton == 1) ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT;
                runtime.getRecipesGui()
                    .show(new Focus<>(mode, rawIngredient));
                return;
            }
        }

        HoverResult result = getHoveredResultAt(mouseX, mouseY);
        if (result != null) {
            RecipeTreeNode hovered = result.node();
            int nodeRelMouseX = result.relX();
            int nodeRelMouseY = result.relY();

            if (nodeRelMouseY >= 0 && nodeRelMouseY <= RecipeTreeRenderer.ROW_HEIGHT) {
                if (hovered.hasSecondColumn()) {
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= RecipeTreeRenderer.COL_WIDTH) {
                        if (hovered.item.category != null) {
                            showCategoryRecipes(hovered.item.category);
                            return;
                        }
                    } else if (nodeRelMouseX > RecipeTreeRenderer.COL_WIDTH && nodeRelMouseX <= hovered.width) {
                        openIngredientFocus(hovered, mouseButton);
                        return;
                    }
                } else {
                    if (nodeRelMouseX >= 0 && nodeRelMouseX <= hovered.width) {
                        openIngredientFocus(hovered, mouseButton);
                        return;
                    }
                }
            }
            return;
        }

        if (mouseButton == 0) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    private void openIngredientFocus(RecipeTreeNode hoveredNode, int mouseButton) {
        if (hoveredNode.item.getIngredient() != null) {
            JFMUYRuntime runtime = Internal.getRuntime();
            if (runtime != null) {
                IFocus.Mode mode = (mouseButton == 1) ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT;
                runtime.getRecipesGui()
                    .show(new Focus<>(mode, hoveredNode.item.getIngredient()));
            }
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

        Object rawIngredient = renderer.getRawInputIngredientUnderMouse(mc, group, mouseX, mouseY);
        if (rawIngredient != null) {
            JFMUYRuntime runtime = Internal.getRuntime();
            if (runtime != null) {
                if (KeyBindings.showRecipe.isActiveAndMatches(keyCode)) {
                    runtime.getRecipesGui()
                        .show(new Focus<>(IFocus.Mode.OUTPUT, rawIngredient));
                    return;
                }
                if (KeyBindings.showUses.isActiveAndMatches(keyCode)) {
                    runtime.getRecipesGui()
                        .show(new Focus<>(IFocus.Mode.INPUT, rawIngredient));
                    return;
                }
            }
        }

        HoverResult result = getHoveredResultAt(mouseX, mouseY);
        if (result != null) {
            RecipeTreeNode hovered = result.node();
            int nodeRelMouseX = result.relX();
            int nodeRelMouseY = result.relY();

            if (nodeRelMouseY >= 0 && nodeRelMouseY <= RecipeTreeRenderer.ROW_HEIGHT) {
                boolean isCategoryCol = hovered.hasSecondColumn()
                    ? (nodeRelMouseX >= 0 && nodeRelMouseX <= RecipeTreeRenderer.COL_WIDTH)
                    : false;

                boolean isIngredientCol = hovered.hasSecondColumn()
                    ? (nodeRelMouseX > RecipeTreeRenderer.COL_WIDTH && nodeRelMouseX <= hovered.width)
                    : (nodeRelMouseX >= 0 && nodeRelMouseX <= hovered.width);

                JFMUYRuntime runtime = Internal.getRuntime();

                if (isIngredientCol && hovered.item.getIngredient() != null && runtime != null) {
                    if (KeyBindings.showRecipe.isActiveAndMatches(keyCode)) {
                        runtime.getRecipesGui()
                            .show(new Focus<>(IFocus.Mode.OUTPUT, hovered.item.getIngredient()));
                        return;
                    }
                    if (KeyBindings.showUses.isActiveAndMatches(keyCode)) {
                        runtime.getRecipesGui()
                            .show(new Focus<>(IFocus.Mode.INPUT, hovered.item.getIngredient()));
                        return;
                    }
                }

                if (isCategoryCol && hovered.item.category != null) {
                    if (KeyBindings.showRecipe.isActiveAndMatches(keyCode)
                        || KeyBindings.showUses.isActiveAndMatches(keyCode)) {
                        showCategoryRecipes(hovered.item.category);
                        return;
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
