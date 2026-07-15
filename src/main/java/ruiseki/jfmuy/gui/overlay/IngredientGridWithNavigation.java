package ruiseki.jfmuy.gui.overlay;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.PageNavigation;
import ruiseki.jfmuy.gui.ghost.IGhostIngredientDragSource;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.input.IMouseHandler;
import ruiseki.jfmuy.input.IPaged;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;
import ruiseki.jfmuy.input.MouseHelper;
import ruiseki.jfmuy.render.IngredientListSlot;
import ruiseki.jfmuy.render.IngredientRenderer;
import ruiseki.jfmuy.util.CommandUtil;
import ruiseki.jfmuy.util.MathUtil;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IShowsRecipeFocuses, IMouseHandler, IGhostIngredientDragSource {

    private static final int NAVIGATION_HEIGHT = 20;

    private int firstItemIndex = 0;
    private final IPaged pageDelegate;
    private final PageNavigation navigation;
    private final GuiScreenHelper guiScreenHelper;
    private final IngredientGrid ingredientGrid;
    private final IIngredientGridSource ingredientSource;
    private Rectangle area = new Rectangle();

    public IngredientGridWithNavigation(IIngredientGridSource ingredientSource, GuiScreenHelper guiScreenHelper,
        GridAlignment alignment) {
        this.ingredientGrid = new IngredientGrid(alignment);
        this.ingredientSource = ingredientSource;
        this.guiScreenHelper = guiScreenHelper;
        this.pageDelegate = new IngredientGridPaged();
        this.navigation = new PageNavigation(this.pageDelegate, false);
    }

    public void updateLayout(boolean resetToFirstPage) {
        if (resetToFirstPage) {
            firstItemIndex = 0;
        }
        List<IIngredientListElement> ingredientList = ingredientSource.getIngredientList();
        if (firstItemIndex >= ingredientList.size()) {
            firstItemIndex = 0;
        }
        this.ingredientGrid.guiIngredientSlots.set(firstItemIndex, ingredientList);
        this.navigation.updatePageState();
    }

    public boolean updateBounds(Rectangle availableArea, Set<Rectangle> guiExclusionAreas, int minWidth) {
        Rectangle estimatedNavigationArea = new Rectangle(
            availableArea.x,
            availableArea.y,
            availableArea.width,
            NAVIGATION_HEIGHT);
        Rectangle movedNavigationArea = MathUtil
            .moveDownToAvoidIntersection(guiExclusionAreas, estimatedNavigationArea);
        int navigationMaxY = movedNavigationArea.y + movedNavigationArea.height;
        int boundsHeight = availableArea.y + availableArea.height - navigationMaxY;
        if (availableArea.width <= 0 || boundsHeight <= 0) {
            return false;
        }
        Rectangle boundsWithoutNavigation = new Rectangle(
            availableArea.x,
            navigationMaxY,
            availableArea.width,
            boundsHeight);
        boolean gridHasRoom = this.ingredientGrid.updateBounds(boundsWithoutNavigation, minWidth, guiExclusionAreas);
        if (!gridHasRoom) {
            return false;
        }
        Rectangle displayArea = this.ingredientGrid.getArea();
        Rectangle navigationArea = new Rectangle(
            displayArea.x,
            movedNavigationArea.y,
            displayArea.width,
            NAVIGATION_HEIGHT);
        this.navigation.updateBounds(navigationArea);
        this.area = displayArea.union(navigationArea);
        return true;
    }

    public Rectangle getArea() {
        return this.area;
    }

    public int size() {
        return this.ingredientGrid.size();
    }

    public void draw(Minecraft minecraft, int mouseX, int mouseY) {
        this.ingredientGrid.draw(minecraft, mouseX, mouseY);
        this.navigation.draw(minecraft, mouseX, mouseY);
    }

    public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
        this.ingredientGrid.drawTooltips(minecraft, mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return this.area.contains(mouseX, mouseY) && !guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        return !guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY)
            && (this.ingredientGrid.handleMouseClicked(mouseX, mouseY)
                || this.navigation.handleMouseClickedButtons(mouseX, mouseY));
    }

    @Override
    public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
        if (scrollDelta < 0) {
            return this.pageDelegate.nextPage();
        } else if (scrollDelta > 0) {
            return this.pageDelegate.previousPage();
        }
        return false;
    }

    public boolean onKeyPressed(char typedChar, int keyCode) {
        if (KeyBindings.nextPage.getKeyCode() == keyCode) {
            return this.pageDelegate.nextPage();
        } else if (KeyBindings.previousPage.getKeyCode() == keyCode) {
            return this.pageDelegate.previousPage();
        }
        return checkHotbarKeys(keyCode);
    }

    /**
     * Modeled after {@link GuiContainer#checkHotbarKeys(int)}
     * Sets the stack in a hotbar slot to the one that's hovered over.
     */
    protected boolean checkHotbarKeys(int keyCode) {
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (Config.isCheatItemsEnabled() && guiScreen != null && !(guiScreen instanceof RecipesGui)) {
            final int mouseX = MouseHelper.getX();
            final int mouseY = MouseHelper.getY();
            if (isMouseOver(mouseX, mouseY)) {
                GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
                for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
                    if (gameSettings.keyBindsHotbar[hotbarSlot].getKeyCode() == keyCode) {
                        IClickedIngredient<?> ingredientUnderMouse = getIngredientUnderMouse(mouseX, mouseY);
                        if (ingredientUnderMouse != null) {
                            ItemStack itemStack = ingredientUnderMouse.getCheatItemStack();
                            if (itemStack != null) {
                                CommandUtil.setHotbarStack(itemStack, hotbarSlot);
                            }
                            ingredientUnderMouse.onClickHandled();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY);
    }

    @Nullable
    @Override
    public IIngredientListElement getElementUnderMouse() {
        return this.ingredientGrid.getElementUnderMouse();
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return this.ingredientGrid.canSetFocusWithMouse();
    }

    public List<IIngredientListElement> getVisibleElements() {
        List<IIngredientListElement> visibleElements = new ArrayList<>();
        for (IngredientListSlot slot : this.ingredientGrid.guiIngredientSlots.getAllGuiIngredientSlots()) {
            IngredientRenderer renderer = slot.getIngredientRenderer();
            if (renderer != null) {
                visibleElements.add(renderer.getElement());
            }
        }
        return visibleElements;
    }

    private class IngredientGridPaged implements IPaged {

        @Override
        public boolean nextPage() {
            if (getPageCount() <= 1) {
                return false;
            }
            final int itemsCount = ingredientSource.size();
            if (itemsCount > 0) {
                firstItemIndex += ingredientGrid.size();
                if (firstItemIndex >= itemsCount) {
                    firstItemIndex = 0;
                }
                updateLayout(false);
                return true;
            } else {
                firstItemIndex = 0;
                updateLayout(false);
                return false;
            }
        }

        @Override
        public boolean previousPage() {
            if (getPageCount() <= 1) {
                return false;
            }
            final int itemsPerPage = ingredientGrid.size();
            if (itemsPerPage == 0) {
                firstItemIndex = 0;
                updateLayout(false);
                return false;
            }
            final int itemsCount = ingredientSource.size();

            int pageNum = firstItemIndex / itemsPerPage;
            if (pageNum == 0) {
                pageNum = itemsCount / itemsPerPage;
            } else {
                pageNum--;
            }

            firstItemIndex = itemsPerPage * pageNum;
            if (firstItemIndex > 0 && firstItemIndex == itemsCount) {
                pageNum--;
                firstItemIndex = itemsPerPage * pageNum;
            }
            updateLayout(false);
            return true;
        }

        @Override
        public boolean hasNext() {
            // true if there is more than one page because this wraps around
            return getPageCount() > 1;
        }

        @Override
        public boolean hasPrevious() {
            // true if there is more than one page because this wraps around
            return getPageCount() > 1;
        }

        @Override
        public int getPageCount() {
            final int itemCount = ingredientSource.size();
            final int stacksPerPage = ingredientGrid.size();
            if (stacksPerPage == 0) {
                return 1;
            }
            int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
            pageCount = Math.max(1, pageCount);
            return pageCount;
        }

        @Override
        public int getPageNumber() {
            final int stacksPerPage = ingredientGrid.size();
            if (stacksPerPage == 0) {
                return 0;
            }
            return firstItemIndex / stacksPerPage;
        }
    }
}
