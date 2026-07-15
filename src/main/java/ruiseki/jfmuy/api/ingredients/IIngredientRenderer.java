package ruiseki.jfmuy.api.ingredients;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import ruiseki.jfmuy.api.recipe.IIngredientType;

/**
 * Renders a type of ingredient in JFMUY's item list and recipes.
 * <p>
 * If you have a new type of ingredient to add to JFMUY, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 */
public interface IIngredientRenderer<T> {

    /**
     * Renders an ingredient at a specific location.
     *
     * @param minecraft  The minecraft instance.
     * @param xPosition  The x position to render the ingredient.
     * @param yPosition  The y position to render the ingredient.
     * @param ingredient the ingredient to render. May be null, some renderers (like fluid tanks) will render a
     *                   background even if there is no ingredient.
     */
    void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable T ingredient);

    /**
     * Get the tooltip text for this ingredient. JFMUY renders the tooltip based on this.
     *
     * @param minecraft   The minecraft instance.
     * @param ingredient  The ingredient to get the tooltip for.
     * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
     * @return The tooltip text for the ingredient.
     */
    default List<String> getTooltip(Minecraft minecraft, T ingredient, boolean tooltipFlag) {
        // you should override this method. this default method is to keep old JFMUY plugins from crashing.
        return getTooltip(minecraft, ingredient, true);
    }

    /**
     * Get the tooltip font renderer for this ingredient. JFMUY renders the tooltip based on this.
     *
     * @param minecraft  The minecraft instance.
     * @param ingredient The ingredient to get the tooltip for.
     * @return The font renderer for the ingredient.
     */
    default FontRenderer getFontRenderer(Minecraft minecraft, T ingredient) {
        return minecraft.fontRenderer;
    }
}
