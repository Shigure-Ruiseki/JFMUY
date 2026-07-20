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

    /**
     * Renders extra visual components below the standard tooltip text box and determines its dimensions.
     * <p>
     * <strong>Note on Layout Architecture:</strong> This method is invoked twice per frame by the tooltip rendering
     * pipeline:
     * <ol>
     * <li><strong>Pre-pass (Measurement):</strong> Invoked prior to drawing the tooltip frame background to measure the
     * required dynamic spatial expansion. Graphics/GL state operations are omitted or safe here.</li>
     * <li><strong>Render-pass (Drawing):</strong> Invoked immediately after the main tooltip background and text lines
     * have been successfully written, rendering the actual visual components (e.g., energy bars, ingredient
     * matrices).</li>
     * </ol>
     *
     * @param minecraft      The minecraft instance.
     * @param mouseX         The absolute X position of the mouse on the screen.
     * @param mouseY         The absolute Y position of the mouse on the screen.
     * @param fontRenderer   The font renderer associated with this ingredient.
     * @param tooltipStrings The list of text strings currently being displayed in the tooltip.
     * @param allIngredients A list containing all cycled ingredient variations (e.g., OreDict equivalents) to be drawn.
     * @return An {@link ExtraSize} object specifying the maximum width and height bounds occupied by this component.
     *         Return {@link ExtraSize#EMPTY} or {@code null} if no extra graphics are rendered.
     */
    default ExtraSize renderTooltipExtras(Minecraft minecraft, int mouseX, int mouseY, FontRenderer fontRenderer,
        List<String> tooltipStrings, List<T> allIngredients) {
        return null;
    }

    class ExtraSize {

        public final int width;
        public final int height;

        public static final ExtraSize EMPTY = new ExtraSize(0, 0);

        public ExtraSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
