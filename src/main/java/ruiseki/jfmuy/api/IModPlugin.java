package ruiseki.jfmuy.api;

import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;

/**
 * The main class to implement to create a JFMUY plugin. Everything communicated between a mod and JFMUY is through this
 * class.
 * IModPlugins must have the {@link JFMUYPlugin} annotation to get loaded by JFMUY.
 *
 * @see BlankModPlugin
 */
public interface IModPlugin {

    /**
     * If your item has subtypes that depend on NBT or capabilities, use this to help JFMUY identify those subtypes
     * correctly.
     */
    void registerItemSubtypes(ISubtypeRegistry subtypeRegistry);

    /**
     * Register special ingredients, beyond the basic ItemStack and FluidStack.
     */
    void registerIngredients(IModIngredientRegistration registry);

    /**
     * Register this mod plugin with the mod registry.
     */
    void register(IModRegistry registry);

    /**
     * Called when jei's runtime features are available, after all mods have registered.
     */
    void onRuntimeAvailable(IJFMUYRuntime jfmuyRuntime);
}
