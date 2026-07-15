package ruiseki.jfmuy.api;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.inventory.Container;

import ruiseki.jfmuy.api.gui.IRecipeLayoutDrawable;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JFMUYManager.
 * Get the instance from {@link IJFMUYRuntime#getRecipeRegistry()}.
 */
public interface IRecipeRegistry {

    /**
     * Returns an unmodifiable list of all Recipe Categories
     */
    List<IRecipeCategory> getRecipeCategories();

    /**
     * Returns an unmodifiable list of Recipe Categories
     */
    List<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids);

    /**
     * Returns the recipe category for the given UID.
     * Returns null if the recipe category does not exist.
     */
    @Nullable
    IRecipeCategory getRecipeCategory(String recipeCategoryUid);

    /**
     * Returns a new focus.
     */
    <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);

    /**
     * Returns a list of Recipe Categories for the focus.
     */
    <V> List<IRecipeCategory> getRecipeCategories(IFocus<V> focus);

    /**
     * Returns a list of Recipe Wrappers in the recipeCategory that have the focus.
     */
    <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus);

    /**
     * Returns a list of Recipe Wrappers in recipeCategory.
     */
    <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory);

    /**
     * Returns the {@link IRecipeWrapper} for this recipe.
     *
     * @param recipe            the recipe to get a wrapper for.
     * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
     *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
     * @return the {@link IRecipeWrapper} for this recipe. returns null if the recipe cannot be handled by JFMUY or its
     *         addons.
     */
    @Nullable
    IRecipeWrapper getRecipeWrapper(Object recipe, String recipeCategoryUid);

    /**
     * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
     * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
     * These are registered with {@link IModRegistry#addRecipeCatalyst(Object, String...)}.
     */
    List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory);

    /**
     * Returns the recipe transfer handler for the given container and category, if one exists.
     *
     * @param container      The container to transfer items in.
     * @param recipeCategory The type of recipe that the recipe transfer handler acts on.
     * @see IRecipeTransferRegistry
     */
    @Nullable
    IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory);

    /**
     * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
     * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
     *
     * @param recipeCategory the recipe category that the recipe belongs to
     * @param recipeWrapper  the specific recipe wrapper to draw.
     * @param focus          the focus of the recipe layout.
     */
    @Nullable
    <T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory,
        T recipeWrapper, IFocus<?> focus);

    /**
     * Hides a recipe so that it will not be displayed.
     * This can be used by mods that create recipe progression.
     *
     * @param recipe            the recipe to hide.
     *                          Get an instance using {@link #getRecipeWrapper(Object, String)}
     *                          or {@link #getRecipeWrappers(IRecipeCategory)}
     * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
     *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
     * @see #unhideRecipe(IRecipeWrapper, String)
     */
    void hideRecipe(IRecipeWrapper recipe, String recipeCategoryUid);

    /**
     * Unhides a recipe that was hidden by {@link #hideRecipe(IRecipeWrapper, String)}
     * This can be used by mods that create recipe progression.
     *
     * @param recipe            the recipe to unhide.
     *                          Get an instance using {@link #getRecipeWrapper(Object, String)}
     *                          or {@link #getRecipeWrappers(IRecipeCategory)}
     * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
     *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
     * @see #hideRecipe(IRecipeWrapper, String)
     */
    void unhideRecipe(IRecipeWrapper recipe, String recipeCategoryUid);

    /**
     * Hide an entire recipe category of recipes from JFMUY.
     * This can be used by mods that create recipe progression.
     *
     * @param recipeCategoryUid the unique ID for the recipe category
     * @see #unhideRecipeCategory(String)
     */
    void hideRecipeCategory(String recipeCategoryUid);

    /**
     * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(String)}.
     * This can be used by mods that create recipe progression.
     *
     * @param recipeCategoryUid the unique ID for the recipe category
     * @see #hideRecipeCategory(String)
     */
    void unhideRecipeCategory(String recipeCategoryUid);
}
