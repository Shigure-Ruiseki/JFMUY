package ruiseki.jfmuy.bookmarks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.autocrafting.IngredientUtil;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.overlay.IIngredientGridSource;
import ruiseki.jfmuy.gui.overlay.bookmarks.group.BookmarkGroupOrganizer;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.util.Log;

@SuppressWarnings("rawtypes")
public class BookmarkList implements IIngredientGridSource {

    private static final String MARKER_GROUP = "B:";
    private static final String MARKER_RECIPE_GROUP = "R:";

    private final List<BookmarkGroup> list = new LinkedList<>();
    private final IngredientRegistry ingredientRegistry;
    private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();
    private int nextId = 0;
    private BookmarkGroupOrganizer bookmarkGroupOrganizer;

    public BookmarkList(IngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
    }

    public <T> boolean add(BookmarkItem<T> ingredient) {
        return add(ingredient, false);
    }

    public boolean add(BookmarkGroup group) {
        list.add(group);
        notifyListenersOfChange();
        saveBookmarks();
        return true;
    }

    public <T> boolean add(BookmarkItem<T> ingredient, boolean forceFront) {
        BookmarkItem<T> normalized = IngredientUtil.normalizeBookmark(ingredient);
        if (!contains(normalized)) {
            if (addToLists(normalized, forceFront || Config.isAddingBookmarksToFront())) {
                notifyListenersOfChange();
                saveBookmarks();
                return true;
            }
        } else if (forceFront) {
            // avoid boolean expression short-circuiting
            boolean flag1 = remove(normalized, true);
            boolean flag2 = addToLists(normalized, true);
            if (flag1 || flag2) {
                notifyListenersOfChange();
                saveBookmarks();
                return true;
            }
        }
        return false;
    }

    private boolean contains(Object ingredient) {
        // We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
        IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

        for (BookmarkGroup group : list) {
            for (BookmarkItem existing : group.getItems()) {
                if (ingredient == existing) {
                    return true;
                }
                if (existing != null && existing.getClass() == ingredient.getClass()) {
                    if (ingredientHelper.getUniqueId(existing)
                        .equals(ingredientHelper.getUniqueId(ingredient))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean remove(Object ingredient) {
        return remove(ingredient, false);
    }

    public boolean remove(Object ingredient, boolean looseEqualCheck) {
        for (BookmarkGroup group : list) {
            for (int i = 0; i < group.getItems()
                .size(); i++) {
                BookmarkItem existing = group.getItems()
                    .get(i);
                if (looseEqualCheck) {
                    String id1 = ingredientRegistry.getIngredientHelper(ingredient)
                        .getUniqueId(ingredient);
                    String id2 = ingredientRegistry.getIngredientHelper(existing)
                        .getUniqueId(existing);
                    if (id1.equals(id2)) {
                        removeItemFromGroup(group, existing);
                        return true;
                    }
                }
                if (ingredient == existing) {
                    removeItemFromGroup(group, existing);
                    return true;
                }
            }
        }
        return false;
    }

    private void removeItemFromGroup(BookmarkGroup group, BookmarkItem<?> item) {
        group.removeItem(item);
        if (group.items.isEmpty() && !containsAnyAddableGroups()) {
            list.remove(group);
        }
        notifyListenersOfChange();
        saveBookmarks();
    }

    private boolean containsAnyAddableGroups() {
        return this.list.stream()
            .anyMatch(BookmarkGroup::acceptsChanges);
    }

    public void saveBookmarks() {
        List<String> strings = new ArrayList<>();
        for (BookmarkGroup group : list) {
            if (group instanceof RecipeBookmarkGroup) {
                strings.add(MARKER_RECIPE_GROUP);
            } else {
                strings.add(MARKER_GROUP);
            }
            for (BookmarkItem<?> item : group.getItems()) {
                String serialized = item.serialize();
                if (serialized == null) {
                    continue;
                }
                strings.add(serialized);
            }
        }

        File file = Config.getBookmarkFile();
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                IOUtils.writeLines(strings, "\n", writer);
            } catch (IOException e) {
                Log.get()
                    .error("Failed to save bookmarks list to file {}", file, e);
            }
        }
    }

    private static <T> String getUid(IIngredientListElement<T> element) {
        IIngredientHelper<T> ingredientHelper = element.getIngredientHelper();
        return ingredientHelper.getUniqueId(element.getIngredient());
    }

    public void loadBookmarks() {
        File file = Config.getBookmarkFile();
        if (file == null || !file.exists()) {
            return;
        }
        List<String> ingredientJsonStrings;
        try (FileReader reader = new FileReader(file)) {
            ingredientJsonStrings = IOUtils.readLines(reader);
        } catch (IOException e) {
            Log.get()
                .error("Failed to load bookmarks from file {}", file, e);
            return;
        }

        Collection<IIngredientType> otherIngredientTypes = new ArrayList<>(
            ingredientRegistry.getRegisteredIngredientTypes());
        otherIngredientTypes.remove(VanillaTypes.ITEM);

        list.clear();
        BookmarkGroup group = new BookmarkGroup(nextId++);
        for (String ingredientJsonString : ingredientJsonStrings) {
            BookmarkItem<?> item = BookmarkItem.deserialize(ingredientJsonString, otherIngredientTypes);
            if (item != null) {
                group.addItemInternal(item); // Don't cause recipe chains to update
            } else if (ingredientJsonString.startsWith(MARKER_GROUP)) {
                if (!group.items.isEmpty()) {
                    list.add(group);
                }
                group = new BookmarkGroup(nextId++);
            } else if (ingredientJsonString.startsWith(MARKER_RECIPE_GROUP)) {
                if (!group.items.isEmpty()) {
                    list.add(group);
                }
                group = new RecipeBookmarkGroup(nextId++);
            } else {
                Log.get()
                    .error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
            }
        }
        if (!group.items.isEmpty()) {
            list.add(group);
        }
        for (BookmarkGroup newGroup : list) {
            newGroup.finishLoading();
        }
        // notifyListenersOfChange();
    }

    public BookmarkGroup getBookmarkGroup(int id) {
        for (BookmarkGroup group : list) {
            if (group.id == id) {
                return group;
            }
        }
        return null;
    }

    public boolean removeGroup(BookmarkGroup group) {
        if (list.remove(group)) {
            notifyListenersOfChange();
            saveBookmarks();
            return true;
        }
        return false;
    }

    private boolean addToLists(BookmarkItem<?> ingredient, boolean addToFront) { // false = stackT ingredient, boolean
                                                                                 // addToFront) {
        return getAddingGroup(addToFront).addItem(ingredient, addToFront);
    }

    private BookmarkGroup getAddingGroup(boolean front) {
        if (list.isEmpty()) {
            list.add(new BookmarkGroup(nextId++));
        }
        if (front) {
            BookmarkGroup group = list.get(0);
            if (group.acceptsChanges()) {
                return group;
            } else {
                list.add(0, new BookmarkGroup(nextId++));
                return list.get(0);
            }
        } else {
            BookmarkGroup group = list.get(list.size() - 1);
            if (group.acceptsChanges()) {
                return group;
            }
            list.add(new BookmarkGroup(nextId++));
            return list.get(list.size() - 1);
        }
    }

    @Override
    public List<IIngredientListElement> getIngredientList() {
        return this.list.stream()
            .flatMap(
                group -> group.getIngredientListElements()
                    .stream())
            .collect(Collectors.toList());
    }

    @Override
    public int size() {
        return getIngredientList().size();
    }

    public boolean isEmpty() {
        return getIngredientList().isEmpty();
    }

    @Override
    public void addListener(IIngredientGridSource.Listener listener) {
        listeners.add(listener);
    }

    public void notifyListenersOfChange() {
        for (IIngredientGridSource.Listener listener : listeners) {
            listener.onChange();
        }
    }

    public int nextId() {
        return nextId++;
    }

    @Nullable
    public BookmarkGroupOrganizer getGroupOrganizer() {
        return bookmarkGroupOrganizer;
    }

    public void setGroupOrganizer(BookmarkGroupOrganizer bookmarkGroupOrganizer) {
        this.bookmarkGroupOrganizer = bookmarkGroupOrganizer;
    }

    public int getBookmarkIndex(int id) {
        for (int index = 0; index < list.size(); index++) {
            if (list.get(index).id == id) {
                return index;
            }
        }
        return -1;
    }

    public boolean moveGroup(BookmarkGroup group, boolean up) {
        int groupIndex = getBookmarkIndex(group.id);
        if (up && groupIndex > 0) {
            Collections.swap(list, groupIndex, groupIndex - 1);
            notifyListenersOfChange();
            saveBookmarks();
            return true;
        } else if (!up && groupIndex < list.size() - 1) {
            Collections.swap(list, groupIndex, groupIndex + 1);
            notifyListenersOfChange();
            saveBookmarks();
            return true;
        }
        return false;
    }
}
