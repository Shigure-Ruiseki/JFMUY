package ruiseki.jfmuy.gui.overlay.bookmarks.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.ingredients.Ingredients;

public class RecipeTreeNode {

    public final RecipeBookmarkItem<?> item;
    public final List<RecipeTreeNode> children = new ArrayList<>();

    public IRecipeLayout recipeLayout;

    public int x;
    public int y;

    public static final int HEADER_WIDTH = 62;
    public static final int HEADER_HEIGHT = 20;

    public int width = HEADER_WIDTH;
    public int height = HEADER_HEIGHT;

    public boolean showRecipePreview = false;

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

    public void recalculateSize() {
        int bgWidth = 0;
        int bgHeight = 0;

        if (item.category != null && item.category.getBackground() != null) {
            IDrawable bg = item.category.getBackground();
            bgWidth = bg.getWidth();
            bgHeight = bg.getHeight();
        }

        if (showRecipePreview && recipeLayout != null && item.category != null) {
            this.width = Math.max(HEADER_WIDTH, bgWidth);
            this.height = HEADER_HEIGHT + 2 + Math.max(18, bgHeight);
        } else {
            this.width = HEADER_WIDTH;
            this.height = HEADER_HEIGHT;
        }
    }

    public void toggleRecipePreview() {
        this.showRecipePreview = !this.showRecipePreview;
        recalculateSize();
    }

    public void setRecipePreview(boolean show) {
        this.showRecipePreview = show;
        recalculateSize();
    }

    public void setRecipePreviewRecursive(boolean show) {
        if (this.recipeLayout != null && this.item != null && this.item.category != null) {
            setRecipePreview(show);
        } else {
            setRecipePreview(false);
        }

        for (RecipeTreeNode child : children) {
            child.setRecipePreviewRecursive(show);
        }
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
                if (isSameIngredient(recipe.ingredient, ingredientItem.ingredient)) {
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

    public int getSubtreeHeight(int yPadding) {
        if (children.isEmpty()) {
            return this.height;
        }

        int totalChildrenHeight = 0;
        for (int i = 0; i < children.size(); i++) {
            totalChildrenHeight += children.get(i)
                .getSubtreeHeight(yPadding);
            if (i < children.size() - 1) {
                totalChildrenHeight += yPadding;
            }
        }

        return Math.max(this.height, totalChildrenHeight);
    }

    public int layout(int depth, int startY, int xPadding, int yPadding) {
        if (children.isEmpty()) {
            this.y = startY;
            return startY + this.height + yPadding;
        }

        int currentChildY = startY;
        int firstChildCenterY = 0;
        int lastChildCenterY = 0;

        for (int i = 0; i < children.size(); i++) {
            RecipeTreeNode child = children.get(i);
            int subTreeHeight = child.getSubtreeHeight(yPadding);

            child.layout(depth + 1, currentChildY, xPadding, yPadding);

            int childCenterY = currentChildY + (subTreeHeight / 2);
            if (i == 0) {
                firstChildCenterY = childCenterY;
            }
            if (i == children.size() - 1) {
                lastChildCenterY = childCenterY;
            }

            currentChildY += subTreeHeight + yPadding;
        }

        int childrenCenterY = (firstChildCenterY + lastChildCenterY) / 2;
        this.y = childrenCenterY - (this.height / 2);

        if (this.y < startY) {
            this.y = startY;
        }

        return Math.max(this.y + this.height + yPadding, currentChildY);
    }

    public void updateXPosition(int currentX, int xSpacing) {
        this.x = currentX;
        for (RecipeTreeNode child : children) {
            child.updateXPosition(currentX + this.width + xSpacing, xSpacing);
        }
    }
}
