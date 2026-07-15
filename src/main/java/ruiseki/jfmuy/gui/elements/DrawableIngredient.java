package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.okcore.client.renderer.GlStateManager;

public class DrawableIngredient<V> implements IDrawable {

    private final V ingredient;
    private final IIngredientRenderer<V> ingredientRenderer;

    public DrawableIngredient(V ingredient, IIngredientRenderer<V> ingredientRenderer) {
        this.ingredient = ingredient;
        this.ingredientRenderer = ingredientRenderer;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        GlStateManager.enableDepth();
        this.ingredientRenderer.render(minecraft, xOffset, yOffset, ingredient);
        GlStateManager.enableAlpha();
        GlStateManager.disableDepth();
    }
}
