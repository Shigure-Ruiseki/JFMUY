package ruiseki.jfmuy.api;

import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.api.recipe.IRecipeCategoryRegistration;

/**
 * The main class to implement to create a JFMUY plugin. Everything communicated between a mod and JFMUY is through this
 * class.
 * IModPlugins must have the {@link JFMUYPlugin} annotation to get loaded by JFMUY.
 */
public interface IModPlugin {

    /**
     * If your item or fluid has subtypes that depend on NBT or capabilities, use this to help JFMUY identify those
     * subtypes correctly.
     */
    default void registerSubtypes(ISubtypeRegistry subtypeRegistry) {

    }

    /**
     * Register special ingredients, beyond the basic ItemStack and FluidStack.
     */
    default void registerIngredients(IModIngredientRegistration registry) {

    }

    /**
     * Register the categories handled by this plugin.
     * These are registered before recipes so they can be checked for validity.
     */
    default void registerCategories(IRecipeCategoryRegistration registry) {

    }

    /**
     * Register collapsible ingredient groups provided by this mod.
     * These appear in the "Manage Groups" screen tagged as "Mod" and can be toggled
     * by the user but are not editable or deletable.
     * <p>
     * Use {@link ICollapsibleGroupRegistry#newGroup(String, String)} to create a builder
     * and call its methods to define the group's members. See {@link ICollapsibleGroupRegistry}
     * for full usage examples.
     *
     * @param registry the registry used to create collapsible group builders
     */
    default void registerCollapsibleGroups(ICollapsibleGroupRegistry registry) {

    }

    /**
     * Register this mod plugin with the mod registry.
     */
    default void register(IModRegistry registry) {

    }

    /**
     * Called when JFMUY's runtime features are available, after all mods have registered.
     */
    default void onRuntimeAvailable(IJFMUYRuntime JFMUYRuntime) {

    }
}
