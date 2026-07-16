package ruiseki.jfmuy.bookmarks;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
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

            if (ingredient.getDisplayAmount() != 0L) {
                String text = String.valueOf(ingredient.getDisplayAmount());
                drawScaledText(text, new Rectangle(xPosition + 1, yPosition + 1, 12, 14), 1f, 0xFFFFFFFF, true, true);
            }
            if (ingredient instanceof RecipeBookmarkItem
                && ((RecipeBookmarkItem<?>) ingredient).selfOutputAmount != 0L) {
                String text = "x" + ((RecipeBookmarkItem<?>) ingredient).selfOutputAmount;
                drawScaledText(text, new Rectangle(xPosition + 1, yPosition + 1, 12, 14), 1f, 0xBBBBBBBB, true, false);
            }
        }
    }

    private static void drawScaledText(String text, Rectangle rect, float scale, int color, boolean shadow,
        boolean isAmountText) {
        Minecraft mc = Minecraft.getMinecraft();
        @SuppressWarnings("ConstantConditions")
        float screenScale = mc.currentScreen.width * 1f / mc.displayWidth;
        float textScale = Math
            .max(screenScale, Math.max(scale, 1f) * (mc.fontRenderer.getUnicodeFlag() ? 0.75f : 0.5f));

        GlStateManager.disableDepth();
        {
            final int width = mc.fontRenderer.getStringWidth(text);
            final int multiplier = isAmountText ? 1 : 0;
            final double offsetX = Math.ceil(rect.getX() + (rect.getWidth() - (width / 2f) * textScale) * multiplier);
            final double offsetY = Math
                .ceil(rect.getY() + (rect.getHeight() - (mc.fontRenderer.FONT_HEIGHT / 2f) * textScale) * multiplier);

            GL11.glTranslated(offsetX, offsetY, 0);
            GL11.glScaled(textScale, textScale, 1);
            mc.fontRenderer.drawString(text, 0, 0, color, shadow);
            GL11.glScaled(1 / textScale, 1 / textScale, 1);
            GL11.glTranslated(-1 * offsetX, -1 * offsetY, 0);
        }
        GlStateManager.enableDepth();
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
