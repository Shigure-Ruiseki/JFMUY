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
            IIngredientType<Object> ingredientType = registry.getIngredientType(ingredient.getIngredient());
            registry.getIngredientRenderer(ingredientType)
                .render(minecraft, xPosition, yPosition, ingredient.getIngredient());

            FontRenderer fontRenderer = getFontRenderer(minecraft, ingredient);
            long displayAmount = ingredient.getDisplayAmount();
            if (displayAmount > 1L) {
                if (ingredient instanceof RecipeBookmarkItem
                    && ((RecipeBookmarkItem<?>) ingredient).isExplicitlyRequested()) {
                    CountUtil.renderStringAsCount(
                        fontRenderer,
                        'x' + CountUtil.minifyCountString(displayAmount),
                        xPosition,
                        yPosition,
                        0xBBBBBBBB,
                        true,
                        true);
                } else {
                    CountUtil.renderCountString(fontRenderer, displayAmount, xPosition, yPosition, true);
                }
            }
        }
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, BookmarkItem ingredient) {
        return getIngredientRenderer(ingredient.getIngredient()).getFontRenderer(minecraft, ingredient.getIngredient());
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, BookmarkItem ingredient, boolean advanced) {
        return getIngredientRenderer(ingredient.getIngredient())
            .getTooltip(minecraft, ingredient.getIngredient(), advanced);
    }

    private static <E> IIngredientRenderer<E> getIngredientRenderer(E ingredient) {
        return Internal.getIngredientRegistry()
            .getIngredientRenderer(ingredient);
    }
}
