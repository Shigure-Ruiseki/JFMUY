package ruiseki.jfmuy.bookmarks;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.util.CountUtil;
import ruiseki.okcore.client.renderer.GlStateManager;

@SuppressWarnings("rawtypes")
public class BookmarkItemRender implements IIngredientRenderer<BookmarkItem> {

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable BookmarkItem ingredient) {
        if (ingredient != null) {
            IngredientRegistry registry = Internal.getIngredientRegistry();
            IIngredientType<Object> ingredientType = registry.getIngredientType(ingredient.ingredient);
            registry.getIngredientRenderer(ingredientType)
                .render(minecraft, xPosition, yPosition, ingredient.ingredient);

            FontRenderer fontRenderer = getFontRenderer(minecraft, ingredient);
            if (ingredient instanceof RecipeBookmarkItem<?>recipeBookmarkItem) {
                if (recipeBookmarkItem.selfOutputAmount > 1L) {
                    CountUtil.renderStringAsCount(
                        fontRenderer,
                        'x' + CountUtil.minifyCountString(recipeBookmarkItem.selfOutputAmount),
                        xPosition,
                        yPosition,
                        0xBBBBBBBB,
                        true,
                        true);
                }
            } else if (ingredient.getDisplayAmount() > 1L) {
                CountUtil.renderCountString(fontRenderer, ingredient.getDisplayAmount(), xPosition, yPosition, true);
            }
        }
        // We need no lighting and plain color for continued ingredient rendering
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, BookmarkItem ingredient) {
        return getIngredientRenderer(ingredient.ingredient).getFontRenderer(minecraft, ingredient.ingredient);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, BookmarkItem ingredient, boolean advanced) {
        return getIngredientRenderer(ingredient.ingredient).getTooltip(minecraft, ingredient.ingredient, advanced);
    }

    private static <E> IIngredientRenderer<E> getIngredientRenderer(E ingredient) {
        return Internal.getIngredientRegistry()
            .getIngredientRenderer(ingredient);
    }
}
