package ruiseki.jfmuy.api;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * IJFMUYHelpers provides helpers and tools for addon mods.
 * Available to IModPlugins
 */
public interface IJFMUYHelpers {

    /**
     * Helps with the implementation of GUIs.
     */
    @Nonnull
    IGuiHelper getGuiHelper();

    /**
     * Helps with getting itemStacks from recipes.
     */
    @Nonnull
    IStackHelper getStackHelper();

    /**
     * Used to stop JFMUY from displaying a specific item in the item list.
     */
    @Nonnull
    IItemBlacklist getItemBlacklist();

    /**
     * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes
     * correctly.
     */
    ISubtypeRegistry getSubtypeRegistry();

    /**
     * Helps with the implementation of Recipe Transfer Handlers
     */
    @Nonnull
    IRecipeTransferHandlerHelper recipeTransferHandlerHelper();

    /**
     * Reload JFMUY at runtime.
     * Used by mods that add and remove items or recipes like MineTweaker's /mt reload.
     */
    void reload();
}
