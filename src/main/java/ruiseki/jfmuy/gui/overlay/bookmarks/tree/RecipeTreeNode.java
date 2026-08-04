package ruiseki.jfmuy.gui.overlay.bookmarks.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.ingredients.Ingredients;
import ruiseki.jfmuy.runtime.JFMUYRuntime;

public class RecipeTreeNode {

    public final RecipeBookmarkItem<?> item;
    public final List<RecipeTreeNode> children = new ArrayList<>();

    public IRecipeLayout recipeLayout;

    public int x;
    public int y;

    public static final int HEADER_HEIGHT = 20;
    public static final int X_PADDING = 8;
    public static final int Y_PADDING = 16;

    public int width = RecipeTreeRenderer.COL_WIDTH;
    public int height = HEADER_HEIGHT;

    public RecipeTreeNode(RecipeBookmarkItem<?> item) {
        this.item = item;
        initRecipeLayout();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initRecipeLayout() {
        if (item.isPopulated() && item.category != null && item.recipe != null) {
            this.recipeLayout = item.createLayout();

            try {
                Ingredients ingredients = new Ingredients();
                item.recipe.getIngredients(ingredients);

                IRecipeCategory category = item.category;
                IRecipeWrapper recipeWrapper = item.recipe;

                category.setRecipe(this.recipeLayout, recipeWrapper, ingredients);
            } catch (Exception ignored) {}
        }
        recalculateSize();
    }

    public boolean hasSecondColumn() {
        if (item == null || item.category == null) {
            return false;
        }
        if (item.category.getIcon() != null) {
            return true;
        }
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime != null) {
            List<Object> catalysts = runtime.getRecipeRegistry()
                .getRecipeCatalysts(item.category);
            return catalysts != null && !catalysts.isEmpty();
        }
        return false;
    }

    public void recalculateSize() {
        this.width = hasSecondColumn() ? (RecipeTreeRenderer.COL_WIDTH * 2) : RecipeTreeRenderer.COL_WIDTH;
        this.height = HEADER_HEIGHT;
    }

    public static RecipeTreeNode buildTree(RecipeBookmarkItem<?> item, RecipeBookmarkGroup group) {
        return buildTree(item, group, null);
    }

    public static RecipeTreeNode buildTree(RecipeBookmarkItem<?> item, RecipeBookmarkGroup group,
        Set<RecipeBookmarkItem<?>> visitedNodes) {
        RecipeTreeNode node = new RecipeTreeNode(item);

        if (item.inputs != null && !item.inputs.isEmpty()) {
            for (RecipeBookmarkItem<?> input : item.inputs) {
                RecipeBookmarkItem<?> provider = findProviderInGroup(input, group);

                if (provider != null && provider != item) {
                    if (visitedNodes != null) {
                        visitedNodes.add(provider);
                    }
                    node.children.add(buildTree(provider, group, visitedNodes));
                } else {
                    node.children.add(buildTree(input, group, visitedNodes));
                }
            }
        }
        return node;
    }

    private static RecipeBookmarkItem<?> findProviderInGroup(RecipeBookmarkItem<?> ingredientItem,
        RecipeBookmarkGroup group) {
        if (group == null || group.getItems() == null || ingredientItem == null) return null;

        for (BookmarkItem<?> bItem : group.getItems()) {
            if (bItem instanceof RecipeBookmarkItem<?>recipe) {
                if (isSameIngredient(recipe.getIngredient(), ingredientItem.getIngredient())) {
                    return recipe;
                }
            }
        }
        return null;
    }

    private static <T> boolean isSameIngredient(T a, T b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        IIngredientType<Object> typeA = Internal.getIngredientRegistry()
            .getIngredientType(a);
        IIngredientType<Object> typeB = Internal.getIngredientRegistry()
            .getIngredientType(b);

        if (typeB == null || typeA != typeB) return false;
        IIngredientHelper<Object> helper = Internal.getIngredientRegistry()
            .getIngredientHelper(typeA);
        if (helper != null) {
            String idA = helper.getUniqueId(a);
            String idB = helper.getUniqueId(b);
            return Objects.equals(idA, idB);
        }

        return Objects.equals(a, b);
    }

    public void shiftX(int deltaX) {
        if (deltaX == 0) return;
        this.x += deltaX;
        for (RecipeTreeNode child : children) {
            child.shiftX(deltaX);
        }
    }

    public void getRightContour(List<Integer> contour, int currentLevel) {
        int rightEdge = this.x + this.width;
        if (currentLevel < contour.size()) {
            contour.set(currentLevel, Math.max(contour.get(currentLevel), rightEdge));
        } else {
            contour.add(rightEdge);
        }

        for (RecipeTreeNode child : children) {
            child.getRightContour(contour, currentLevel + 1);
        }
    }

    public void getLeftContour(List<Integer> contour, int currentLevel) {
        int leftEdge = this.x;
        if (currentLevel < contour.size()) {
            contour.set(currentLevel, Math.min(contour.get(currentLevel), leftEdge));
        } else {
            contour.add(leftEdge);
        }

        for (RecipeTreeNode child : children) {
            child.getLeftContour(contour, currentLevel + 1);
        }
    }

    public void layout(int startX, int xPadding) {
        int padding = xPadding + X_PADDING;

        if (children.isEmpty()) {
            this.x = startX;
            return;
        }

        RecipeTreeNode firstChild = children.getFirst();
        firstChild.layout(startX, xPadding);

        List<Integer> leftContour = new ArrayList<>();
        List<Integer> rightContour = new ArrayList<>();

        for (int i = 1; i < children.size(); i++) {
            RecipeTreeNode prevChild = children.get(i - 1);
            RecipeTreeNode currentChild = children.get(i);

            int nextStartX = prevChild.x + prevChild.width + padding;
            currentChild.layout(nextStartX, xPadding);

            leftContour.clear();
            rightContour.clear();

            prevChild.getRightContour(leftContour, 0);
            currentChild.getLeftContour(rightContour, 0);

            int maxOverlap = 0;
            int minLevels = Math.min(leftContour.size(), rightContour.size());

            for (int level = 0; level < minLevels; level++) {
                int overlap = (leftContour.get(level) + padding) - rightContour.get(level);
                if (overlap > maxOverlap) {
                    maxOverlap = overlap;
                }
            }

            if (maxOverlap > 0) {
                currentChild.shiftX(maxOverlap);
            }
        }

        RecipeTreeNode lastChild = children.getLast();
        int firstChildCenter = firstChild.x + (firstChild.width / 2);
        int lastChildCenter = lastChild.x + (lastChild.width / 2);

        int desiredX = ((firstChildCenter + lastChildCenter) / 2) - (this.width / 2);

        if (desiredX < startX) {
            int shift = startX - desiredX;
            this.x = startX;
            for (RecipeTreeNode child : children) {
                child.shiftX(shift);
            }
        } else {
            this.x = desiredX;
        }
    }

    public void updateYPosition(int currentY) {
        this.y = currentY;
        for (RecipeTreeNode child : children) {
            child.updateYPosition(currentY + this.height + Y_PADDING);
        }
    }

    public int getMaxX() {
        int maxX = this.x + this.width;
        for (RecipeTreeNode child : children) {
            maxX = Math.max(maxX, child.getMaxX());
        }
        return maxX;
    }
}
