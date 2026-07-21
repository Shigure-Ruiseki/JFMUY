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

import it.unimi.dsi.fastutil.ints.IntList;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.ghost.IGhostIngredientDragSource;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.navigation.NavigationLayout;
import ruiseki.jfmuy.gui.overlay.GridAlignment;
import ruiseki.jfmuy.gui.overlay.IIngredientGridSource;
import ruiseki.jfmuy.gui.overlay.bookmarks.group.BookmarkGroupOrganizer;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.input.IMouseHandler;
import ruiseki.jfmuy.input.IPaged;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;
import ruiseki.jfmuy.render.BookmarkListBatchRenderer;

public class BookmarkGridWithNavigation implements IShowsRecipeFocuses, IMouseHandler, IGhostIngredientDragSource {

    private static final int NAVIGATION_HEIGHT = 20;
    public static final int BOOKMARK_TAB_WIDTH = 10;

    private int firstItemIndex = 0;
    private final IPaged pageDelegate;
    private IntList pageBoundaries;
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
        ((BookmarkListBatchRenderer) this.bookmarkGrid.getGuiIngredientSlots())
            .addBookmarkCollapseListener(() -> this.updateLayout(false));
    }

    public void updateLayout(boolean resetToFirstPage) {
        if (resetToFirstPage) {
            firstItemIndex = 0;
        }
        @SuppressWarnings("rawtypes")
        List<IIngredientListElement> ingredientList = ingredientSource.getIngredientList();
        BookmarkListBatchRenderer renderer = (BookmarkListBatchRenderer) this.bookmarkGrid.getGuiIngredientSlots();
        // Bounds check
        int prevDisplaySize = renderer.getDisplaySize();
        int boundsLimit = prevDisplaySize > 0 ? prevDisplaySize : ingredientList.size();
        if (firstItemIndex >= boundsLimit) {
            firstItemIndex = 0;
        }
        List<IIngredientListElement> collapsedList = ingredientSource.getCollapsedIngredientList();
        this.bookmarkGrid.getGuiIngredientSlots()
            .setCollapsed(firstItemIndex, collapsedList);
        // Re-clamp if the display list shrank (e.g. group expanded) and firstItemIndex is now past the end.
        if (firstItemIndex > 0 && firstItemIndex >= renderer.getDisplaySize()) {
            firstItemIndex = 0;
            this.bookmarkGrid.getGuiIngredientSlots()
                .setCollapsed(0, collapsedList);
        }
        this.pageBoundaries = renderer.sizePages();
        this.navigation.updatePageState();
    }

    public boolean updateBounds(Rectangle availableArea, Set<Rectangle> guiExclusionAreas, int minWidth) {
        clearLayout();

        int bookmarkTabWidth = Config.areRecipeBookmarksEnabled() ? BOOKMARK_TAB_WIDTH : 0;
        Rectangle initialContentArea = new Rectangle(
            availableArea.x + bookmarkTabWidth,
            availableArea.y + NAVIGATION_HEIGHT,
            availableArea.width - bookmarkTabWidth,
            availableArea.height - NAVIGATION_HEIGHT);
        if (!this.bookmarkGrid.updateBoundsForNavigation(initialContentArea, minWidth, guiExclusionAreas)) {
            return false;
        }

        int maximumNavigationWidth = this.bookmarkGrid.getArea().width;
        NavigationLayout.Result layout = NavigationLayout.calculate(
            availableArea,
            guiExclusionAreas,
            NavigationLayout.Alignment.LEFT,
            NAVIGATION_HEIGHT,
            minWidth,
            maximumNavigationWidth);
        if (layout == null) {
            clearLayout();
            return false;
        }

        Rectangle contentArea = layout.getContentArea();
        Rectangle gridContentArea = new Rectangle(
            contentArea.x + bookmarkTabWidth,
            contentArea.y,
            contentArea.width - bookmarkTabWidth,
            contentArea.height);
        if (!this.bookmarkGrid.updateBounds(gridContentArea, minWidth, guiExclusionAreas)) {
            clearLayout();
            return false;
        }

        Rectangle displayArea = this.bookmarkGrid.getArea();
        Rectangle navigationArea = layout.getNavigationArea();
        this.navigation.updateBounds(navigationArea);
        this.groupOrganizer.updateBounds(contentArea);
        this.area = displayArea.union(navigationArea);
        return true;
    }

    private void clearLayout() {
        this.area = new Rectangle();
        this.navigation.updateBounds(new Rectangle());
        this.bookmarkGrid.clearLayout();
        this.groupOrganizer.clearLayout();
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
        return !guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY)
            && (this.groupOrganizer.handleMouseClicked(mouseX, mouseY, mouseButton)
                || this.bookmarkGrid.handleMouseClicked(mouseX, mouseY)
                || this.navigation.handleMouseClickedButtons(mouseX, mouseY));

    }

    public boolean handleMouseReleased(int mouseX, int mouseY, int mouseButton) {
        return this.groupOrganizer.handleMouseReleased(mouseX, mouseY, mouseButton);
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
            firstItemIndex = pageBoundaries.getInt(pageNum + 1);
            updateLayout(false);
            return true;
        }

        @Override
        public boolean previousPage() {
            int pageNum = getPageNumber();
            firstItemIndex = pageBoundaries.getInt(pageNum == 0 ? pageBoundaries.size() - 1 : pageNum - 1);
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
            firstItemIndex = pageBoundaries.getInt(index); // This side effect is fine.
            return index;
        }
    }
}
