package ruiseki.jfmuy.api.recipe;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IRecipeLayout;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe)
 * and handles setting up the GUI for its recipe category.
 */
public interface IRecipeCategory<T extends IRecipeWrapper> {

    /**
     * Returns a unique ID for this recipe category.
     * Referenced from recipes to identify which recipe category they belong to.
     */
    @NotNull
    String getUid();

    /**
     * Returns the localized name for this recipe type.
     * Drawn at the top of the recipe GUI pages for this category.
     * Called every frame, so make sure to store it in a field.
     */
    @NotNull
    String getTitle();

    /**
     * Returns the drawable background for a single recipe in this category.
     * Called multiple times per frame, so make sure to store it in a field.
     */
    @NotNull
    IDrawable getBackground();

    /**
     * Optionally draw anything else that might be necessary, icons or extra slots.
     */
    void drawExtras(@NotNull Minecraft minecraft);

    /**
     * Optionally draw animations like progress bars. These animations can be disabled in the config.
     */
    void drawAnimations(@NotNull Minecraft minecraft);

    /**
     * Set the IRecipeLayout properties from the IRecipeWrapper.
     */
    void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull T recipeWrapper);

}
