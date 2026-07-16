package ruiseki.jfmuy.gui.overlay.bookmarks;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.ghost.IGhostIngredientDragSource;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.overlay.GridAlignment;
import ruiseki.jfmuy.gui.overlay.IIngredientGridSource;
import ruiseki.jfmuy.gui.overlay.bookmarks.group.BookmarkGroupOrganizer;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.input.IMouseHandler;
import ruiseki.jfmuy.input.IPaged;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;
import ruiseki.jfmuy.render.BookmarkListBatchRenderer;
import ruiseki.jfmuy.util.MathUtil;

public class BookmarkGridWithNavigation implements IShowsRecipeFocuses, IMouseHandler, IGhostIngredientDragSource {

    private static final int NAVIGATION_HEIGHT = 20;
    public static final int BOOKMARK_TAB_WIDTH = 10;

    private int firstItemIndex = 0;
    private final IPaged pageDelegate;
    private List<Integer> pageBoundaries;
    private final BookmarkPageNavigation navigation;

    private BookmarkGroupOrganizer groupOrganizer;
    private final GuiScreenHelper guiScreenHelper;
    private final BookmarkGrid bookmarkGrid;
    private final IIngredientGridSource ingredientSource;
    private Rectangle area = new Rectangle();

    public BookmarkGridWithNavigation(IIngredientGridSource ingredientSource, GuiScreenHelper guiScreenHelper,
        GridAlignment alignment) {
        this.groupOrganizer = new BookmarkGroupOrganizer();
        this.bookmarkGrid = new BookmarkGrid(alignment, groupOrganizer);
        this.ingredientSource = ingredientSource;
        this.guiScreenHelper = guiScreenHelper;
        this.pageDelegate = new BookmarkGridPaged();
        this.navigation = new BookmarkPageNavigation(this.pageDelegate, false);
    }

    public void updateLayout(boolean resetToFirstPage) {
        if (resetToFirstPage) {
            firstItemIndex = 0;
        }
        @SuppressWarnings("rawtypes")
        List<IIngredientListElement> ingredientList = ingredientSource.getIngredientList();
        if (firstItemIndex >= ingredientList.size()) {
            firstItemIndex = 0;
        }
        this.bookmarkGrid.getGuiIngredientSlots()
            .set(firstItemIndex, ingredientList);
        this.pageBoundaries = ((BookmarkListBatchRenderer) this.bookmarkGrid.getGuiIngredientSlots())
            .sizePages(ingredientList);
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
        Rectangle boundsWithoutNavigation = new Rectangle(
            availableArea.x + (Config.areRecipeBookmarksEnabled() ? BOOKMARK_TAB_WIDTH : 0),
            navigationMaxY,
            availableArea.width - (Config.areRecipeBookmarksEnabled() ? BOOKMARK_TAB_WIDTH : 0),
            availableArea.height - navigationMaxY);
        Rectangle groupOrganizerBounds = new Rectangle(
            availableArea.x,
            navigationMaxY,
            availableArea.width,
            availableArea.height - navigationMaxY);
        boolean gridHasRoom = this.bookmarkGrid.updateBounds(boundsWithoutNavigation, minWidth, guiExclusionAreas);
        if (!gridHasRoom) {
            return false;
        }
        Rectangle displayArea = this.bookmarkGrid.getArea();
        Rectangle navigationArea = new Rectangle(2, movedNavigationArea.y, displayArea.width, NAVIGATION_HEIGHT);
        this.navigation.updateBounds(navigationArea);
        this.groupOrganizer.updateBounds(groupOrganizerBounds);
        this.area = displayArea.union(navigationArea);
        return true;
    }

    public Rectangle getArea() {
        return this.area;
    }

    public void draw(Minecraft minecraft, int mouseX, int mouseY) {
        this.bookmarkGrid.draw(minecraft, mouseX, mouseY);
        if (this.pageDelegate.getPageCount() > 1) {
            this.navigation.draw(minecraft, mouseX, mouseY);
        }
        this.groupOrganizer.draw(minecraft, mouseX, mouseY);
    }

    public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
        if (!this.guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY)) {
            this.bookmarkGrid.drawTooltips(minecraft, mouseX, mouseY);
            this.groupOrganizer.drawTooltips(minecraft, mouseX, mouseY);
        }
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return this.area.contains(mouseX, mouseY) && !guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean clickedGrid = !guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY)
            && this.bookmarkGrid.handleMouseClicked(mouseX, mouseY);
        boolean clickedNavigation = this.pageDelegate.getPageCount() > 1
            && this.navigation.handleMouseClickedButtons(mouseX, mouseY);
        return clickedGrid || clickedNavigation;
    }

    @Override
    public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
        IIngredientListElement<?> element = this.getElementUnderMouse();
        if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
            && element != null) {
            BookmarkItem<?> item = (BookmarkItem<?>) element.getIngredient();
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                if (item.ingredient instanceof ItemStack) {
                    int stackSize = ((ItemStack) item.ingredient).getMaxStackSize();
                    item.changeAmount(scrollDelta < 0 ? -stackSize : stackSize);
                } else if (item.ingredient instanceof FluidStack) {
                    item.changeAmount(scrollDelta < 0 ? -1000 : 1000);
                } else {
                    item.changeAmount(scrollDelta < 0 ? -1 : 1);
                }
            } else {
                item.changeAmount(scrollDelta < 0 ? -1 : 1);
            }
            Internal.getBookmarkList()
                .saveBookmarks();
            bookmarkGrid.getGuiIngredientSlots()
                .invalidateBuffer();
            return true;
        } else {
            if (scrollDelta < 0) {
                this.pageDelegate.nextPage();
                return true;
            } else if (scrollDelta > 0) {
                this.pageDelegate.previousPage();
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        return this.bookmarkGrid.getIngredientUnderMouse(mouseX, mouseY);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public IIngredientListElement getElementUnderMouse() {
        return this.bookmarkGrid.getElementUnderMouse();
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return this.bookmarkGrid.canSetFocusWithMouse();
    }

    public BookmarkGroupOrganizer getBookmarkGroupOrganizer() {
        return groupOrganizer;
    }

    private class BookmarkGridPaged implements IPaged {

        @Override
        public boolean nextPage() {
            int pageNum = getPageNumber();
            if (pageNum == getPageCount() - 1) {
                updateLayout(true);
                return true;
            }
            firstItemIndex = pageBoundaries.get(pageNum + 1);
            updateLayout(false);
            return true;
        }

        @Override
        public boolean previousPage() {
            int pageNum = getPageNumber();
            firstItemIndex = pageBoundaries.get(pageNum == 0 ? pageBoundaries.size() - 1 : pageNum - 1);
            updateLayout(false);
            return true;
        }

        @Override
        public boolean hasNext() {
            // true if there is more than one page because this wraps around
            return true;
        }

        @Override
        public boolean hasPrevious() {
            // true if there is more than one page because this wraps around
            return true;
        }

        @Override
        public int getPageCount() {
            return pageBoundaries.size();
        }

        @Override
        public int getPageNumber() {
            if (pageBoundaries.isEmpty()) {
                firstItemIndex = 0;
                return 0;
            }
            // Binary search on page boundaries to find the index of the page boundary that is closest to firstItemIndex
            // without going over it
            int index = Collections.binarySearch(pageBoundaries, firstItemIndex);
            if (index < 0) { // This is just how Collections.binarySearch returns if it doesn't find an exact match
                index = -index - 1;
                if (index == pageBoundaries.size()) { // And here's what it does if it's larger than everything in it.
                    index--;
                }
            }
            firstItemIndex = pageBoundaries.get(index); // This side effect is fine.
            return index;
        }
    }
}
