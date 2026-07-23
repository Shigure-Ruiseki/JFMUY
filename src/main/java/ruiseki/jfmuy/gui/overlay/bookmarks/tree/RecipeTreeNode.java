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
    public int width = 18;
    public int height = 18;

    public RecipeTreeNode(RecipeBookmarkItem<?> item) {
        this.item = item;
        initRecipeLayout();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initRecipeLayout() {
        if (item.isPopulated() && item.category != null && item.recipe != null) {
            this.recipeLayout = item.createLayout();

            int bgWidth = 0;
            int bgHeight = 0;

            if (item.category.getBackground() != null) {
                IDrawable bg = item.category.getBackground();
                bgWidth = bg.getWidth();
                bgHeight = bg.getHeight();
            }

            int iconWidth = (item.ingredient != null) ? 22 : 0;
            this.width = iconWidth + bgWidth;
            this.height = Math.max(18, bgHeight);

            try {
                Ingredients ingredients = new Ingredients();
                item.recipe.getIngredients(ingredients);

                IRecipeCategory category = item.category;
                IRecipeWrapper recipeWrapper = item.recipe;

                category.setRecipe(this.recipeLayout, recipeWrapper, ingredients);
            } catch (Exception ignored) {}
        } else {
            this.width = 18;
            this.height = 18;
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

            if (i == 0) {
                firstChildCenterY = currentChildY + child.height / 2;
            }
            if (i == children.size() - 1) {
                lastChildCenterY = currentChildY + child.height / 2;
            }

            currentChildY = child.layout(depth + 1, currentChildY, xPadding, yPadding);
        }

        int childrenCenterY = (firstChildCenterY + lastChildCenterY) / 2;
        this.y = childrenCenterY - (this.height / 2);

        return Math.max(startY + getSubtreeHeight(yPadding) + yPadding, currentChildY);
    }

    public void updateXPosition(int currentX, int xSpacing) {
        this.x = currentX;
        for (RecipeTreeNode child : children) {
            child.updateXPosition(currentX + this.width + xSpacing, xSpacing);
        }
    }
}
