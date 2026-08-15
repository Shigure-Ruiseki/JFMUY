package ruiseki.jfmuy.gui.overlay.bookmarks.group;

import java.awt.Rectangle;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;
import ruiseki.jfmuy.bookmarks.BookmarkGroup;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.overlay.IngredientGrid;
import ruiseki.jfmuy.gui.overlay.bookmarks.BookmarkGridWithNavigation;

public class BookmarkGroupDisplay implements IGhostIngredientHandler.AwareTarget {

    private final BookmarkGroupOrganizer organizer;

    Rectangle area;
    BookmarkGroup group;

    public BookmarkGroupDisplay(Rectangle area, BookmarkGroup group, BookmarkGroupOrganizer organizer) {
        this.area = area;
        this.group = group;
        this.organizer = organizer;
    }

    @Override
    public Rectangle getArea() {
        return area;
    }

    @Override
    public void accept(Object ingredient, int mouseX, int mouseY) {
        if (!canAccept(ingredient)) {
            return;
        }
        BookmarkList bookmarkList = Internal.getBookmarkList();
        int insertionIndex = getInsertionIndex(mouseX, mouseY);

        if (ingredient instanceof BookmarkItem) {
            BookmarkItem<?> item = (BookmarkItem<?>) ingredient;
            BookmarkGroup oldGroup = item.getGroup();
            boolean canAdd = group.addItem(item, false);
            if (canAdd) {
                if (oldGroup != null) {
                    oldGroup.removeItem(item);
                }
                moveItemToIndex(item, insertionIndex);
                bookmarkList.notifyListenersOfAddition(item);
                bookmarkList.saveBookmarks();
                bookmarkList.notifyListenersOfChange();
            }
        } else {
            BookmarkItem<?> item = new BookmarkItem<>(ingredient);
            if (group.addItem(item, false)) {
                moveItemToIndex(item, insertionIndex);
                if (!Config.isBookmarkOverlayEnabled()) {
                    Config.toggleBookmarkEnabled();
                }
                bookmarkList.notifyListenersOfAddition(item);
                bookmarkList.saveBookmarks();
                bookmarkList.notifyListenersOfChange();
            }
        }
    }

    boolean canAccept(Object ingredient) {
        if (!group.acceptsChanges()) {
            return false;
        }
        BookmarkItem<?> item;
        if (ingredient instanceof BookmarkItem) {
            item = (BookmarkItem<?>) ingredient;
            BookmarkGroup oldGroup = item.getGroup();
            if (oldGroup != null && !oldGroup.acceptsChanges()) {
                return false;
            }
        } else {
            item = new BookmarkItem<>(ingredient);
        }
        return group.canAddItem(item);
    }

    @Override
    public void onDrag(Object ingredient, int mouseX, int mouseY) {
        organizer.setInsertionPreview(group, getInsertionIndex(mouseX, mouseY), ingredient);
    }

    @Override
    public void onDragComplete() {
        organizer.clearInsertionPreview();
    }

    private int getInsertionIndex(int mouseX, int mouseY) {
        int gridWidth = area.width - BookmarkGridWithNavigation.BOOKMARK_TAB_WIDTH;
        int columns = Math.max(1, Math.min(gridWidth / IngredientGrid.INGREDIENT_WIDTH, Config.getMaxColumns()));
        int column = (mouseX - area.x - BookmarkGridWithNavigation.BOOKMARK_TAB_WIDTH)
            / IngredientGrid.INGREDIENT_WIDTH;
        column = Math.max(0, Math.min(column, columns - 1));
        int row = Math.max(0, (mouseY - area.y) / IngredientGrid.INGREDIENT_HEIGHT);
        return row * columns + column;
    }

    private void moveItemToIndex(BookmarkItem<?> item, int insertionIndex) {
        int currentIndex = group.getItems()
            .lastIndexOf(item);
        if (currentIndex == -1) {
            return;
        }
        BookmarkItem<?> addedItem = group.getItems()
            .remove(currentIndex);
        IIngredientListElement<?> element = group.getIngredientListElements()
            .remove(currentIndex);
        int targetIndex = Math.min(
            insertionIndex,
            group.getItems()
                .size());
        group.getItems()
            .add(targetIndex, addedItem);
        group.getIngredientListElements()
            .add(targetIndex, element);
    }
}
