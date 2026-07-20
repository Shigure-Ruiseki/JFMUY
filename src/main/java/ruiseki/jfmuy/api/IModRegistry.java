package ruiseki.jfmuy.api;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;
import ruiseki.jfmuy.api.gui.IGlobalGuiHandler;
import ruiseki.jfmuy.api.gui.IGuiScreenHandler;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeRegistryPlugin;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IRecipeWrapperFactory;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;

/**
 * Entry point for the JFMUY API, functions for registering recipes are available from here.
 * The IModRegistry instance is passed to your mod plugin in {@link IModPlugin#register(IModRegistry)}.
 */
public interface IModRegistry {

    /**
     * Get helpers and tools for implementing JFMUY plugins.
     */
    IJFMUYHelpers getJFMUYHelpers();

    /**
     * Get useful functions relating to recipe ingredients.
     */
    IIngredientRegistry getIngredientRegistry();

    /**
     * Add the recipes provided by your plugin.
     * Handle them with {@link #handleRecipes(Class, IRecipeWrapperFactory, String)}.
     * Recipes added here that already implement {@link IRecipeWrapper} do not need to add a handler.
     */
    void addRecipes(Collection<?> recipes, String recipeCategoryUid);

    /**
     * Add a handler for recipes provided by your plugin.
     * Recipes that already implement {@link IRecipeWrapper} do not need to add a handler here.
     *
     * @param recipeClass          the recipe class being handled.
     * @param recipeWrapperFactory turns recipes into recipe wrappers.
     * @param recipeCategoryUid    a unique category id. For vanilla category IDs, see {@link VanillaRecipeCategoryUid}.
     */
    <T> void handleRecipes(Class<T> recipeClass, IRecipeWrapperFactory<T> recipeWrapperFactory,
        String recipeCategoryUid);

    /**
     * Add a clickable area on a gui to jump to specific categories of recipes in JFMUY.
     *
     * @param guiContainerClass  the gui class for JFMUY to detect.
     * @param xPos               left x position of the clickable area, relative to the left edge of the gui.
     * @param yPos               top y position of the clickable area, relative to the top edge of the gui.
     * @param width              the width of the clickable area.
     * @param height             the height of the clickable area.
     * @param recipeCategoryUids the recipe categories that JFMUY should display.
     */
    void addRecipeClickArea(Class<? extends GuiContainer> guiContainerClass, int xPos, int yPos, int width, int height,
        String... recipeCategoryUids);

    /**
     * Add an association between an ingredient and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel
     * Recipes)
     * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
     *
     * @param catalystIngredient the ingredient that can craft recipes (like a furnace or crafting table)
     * @param recipeCategoryUids the recipe categories handled by the ingredient
     */
    void addRecipeCatalyst(Object catalystIngredient, String... recipeCategoryUids);

    /**
     * Queues a copy operation of all registered recipe catalyst from one category to another.
     * <p>
     * The actual cloning and registration process is deferred until the final recipe registry is built.
     * This ensures that catalyst registered late by other mods are also successfully copied.
     *
     * @param fromRecipeCategoryUid the source recipe category UID to copy catalyst from
     * @param toRecipeCategoryUid   the target recipe category UID to apply the copied catalyst to
     */
    void copyRecipeCatalyst(String fromRecipeCategoryUid, String toRecipeCategoryUid);

    /**
     * Add a handler to give JFMUY extra information about how to layout the item list next to a specific type of
     * GuiContainer.
     * Used for guis with tabs on the side that would normally intersect with JFMUY's item list.
     */
    void addAdvancedGuiHandlers(IAdvancedGuiHandler<?>... advancedGuiHandlers);

    /**
     * Add a handler to give JFMUY extra information about how to layout the item list.
     * Used for guis that display next to GUIs and would normally intersect with JFMUY.
     */
    void addGlobalGuiHandlers(IGlobalGuiHandler... globalGuiHandlers);

    /**
     * Add a handler to let JFMUY draw next to a specific class (or subclass) of {@link GuiScreen}.
     * By default, JFMUY can only draw next to {@link GuiContainer}.
     */
    <T extends GuiScreen> void addGuiScreenHandler(Class<T> guiClass, IGuiScreenHandler<T> handler);

    /**
     * Lets mods accept ghost ingredients from JFMUY.
     * These ingredients are dragged from the ingredient list on to your gui, and are useful
     * for setting recipes or anything else that does not need the real ingredient to exist.
     */
    <T extends GuiScreen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler);

    /**
     * Add an info page for an ingredient.
     * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
     *
     * @param ingredient      the ingredient to describe
     * @param ingredientType  the type of the ingredient
     * @param descriptionKeys Localization keys for info text.
     *                        New lines can be added with "\n" or by giving multiple descriptionKeys.
     *                        Long lines are wrapped automatically.
     *                        Very long entries will span multiple pages automatically.
     */
    <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, String... descriptionKeys);

    /**
     * Add an info page for multiple ingredients together.
     * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
     *
     * @param ingredients     the ingredients to describe
     * @param ingredientType  the type of the ingredients
     * @param descriptionKeys Localization keys for info text.
     *                        New lines can be added with "\n" or by giving multiple descriptionKeys.
     *                        Long lines are wrapped automatically.
     *                        Very long entries will span multiple pages automatically.
     */
    <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionKeys);

    /**
     * Get the registry for setting up recipe transfer.
     */
    IRecipeTransferRegistry getRecipeTransferRegistry();

    /**
     * Register your own Recipe Registry Plugin here.
     *
     * @see IRecipeRegistryPlugin
     */
    void addRecipeRegistryPlugin(IRecipeRegistryPlugin recipeRegistryPlugin);
}
