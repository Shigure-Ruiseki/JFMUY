package ruiseki.jfmuy.api.recipe.transfer;

import net.minecraft.inventory.Container;

import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;

/**
 * Register recipe transfer handlers here to give JFMUY the information it needs to transfer recipes into the crafting
 * area.
 * Get the instance from {@link IModRegistry#getRecipeTransferRegistry()}.
 * <p>
 * To get registered recipe transfer handlers at runtime, see
 * {@link IRecipeRegistry#getRecipeTransferHandler(Container, IRecipeCategory)}
 */
public interface IRecipeTransferRegistry {

    /**
     * Basic method for adding a recipe transfer handler.
     *
     * @param containerClass     the class of the container that this recipe transfer handler is for
     * @param recipeCategoryUid  the recipe categories that this container can use
     * @param recipeSlotStart    the first slot for recipe inputs
     * @param recipeSlotCount    the number of slots for recipe inputs
     * @param inventorySlotStart the first slot of the available inventory (usually player inventory)
     * @param inventorySlotCount the number of slots of the available inventory
     */
    <C extends Container> void addRecipeTransferHandler(Class<C> containerClass, String recipeCategoryUid,
        int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);

    /**
     * More advanced method for adding a recipe transfer handler including an output slot for autocrafting.
     *
     * @param containerClass     the class of the container that this recipe transfer handler is for
     * @param recipeCategoryUid  the recipe categories that this container can use
     * @param recipeSlotStart    the first slot for recipe inputs
     * @param recipeSlotCount    the number of slots for recipe inputs
     * @param inventorySlotStart the first slot of the available inventory (usually player inventory)
     * @param inventorySlotCount the number of slots of the available inventory
     * @param outputSlot         the output slot that resulting items may be taken from for autocrafting
     */
    default <C extends Container> void addRecipeTransferHandlerWithOutput(Class<C> containerClass,
        String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart,
        int inventorySlotCount, int outputSlot) {
        addRecipeTransferHandler(
            containerClass,
            recipeCategoryUid,
            recipeSlotStart,
            recipeSlotCount,
            inventorySlotStart,
            inventorySlotCount);
    }

    /**
     * Helper method for adding autocrafting support for mods with transfer handler already registered but
     * with no output slot specified. Providing a negative value for {@code outputSlot} will block any other
     * overrides for specified class and uid.
     * <p>
     * This method exists only for adding autocrafting support for outdated mods that added recipe transfer
     * support based on JEI instead of HEI. Mods should use {@link
     * #addRecipeTransferHandlerWithOutput(Class, String, int, int, int, int, int)} when possible
     * <p>
     * Will not work if recipe transfer handler for such containerClass+recipeCategoryUid does not exist or
     * the handler is not registered through {@link #addRecipeTransferHandler(Class, String, int, int, int,
     * int) basic method}
     *
     * @param containerClass    the class of the container that this recipe transfer handler is for
     * @param recipeCategoryUid the recipe categories that this container can use
     * @param outputSlot        the output slot that resulting items may be taken from for autocrafting.
     *                          Negative value will block any other possible overrides
     * @since HEI ?
     */
    default <C extends Container> void overrideOutputSlot(Class<C> containerClass, String recipeCategoryUid,
        int outputSlot) {}

    /**
     * Advanced method for adding a recipe transfer handler.
     * <p>
     * Use this when recipe slots or inventory slots are spread out in different number ranges or if the container
     * supports autocrafting.
     */
    <C extends Container> void addRecipeTransferHandler(IRecipeTransferInfo<C> recipeTransferInfo);

    /**
     * Complete control over recipe transfer.
     * Use this when the container has a non-standard inventory or crafting area.
     */
    void addRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler, String recipeCategoryUid);

    /**
     * Add a universal handler that can handle any category of recipe.
     * Useful for mods with recipe pattern encoding, for automated recipe systems.
     */
    void addUniversalRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler);

    /**
     * Queues a copy operation of all registered recipe transfer handlers from one category to another.
     * <p>
     * The actual cloning and registration process is deferred until the final recipe registry is built.
     * This ensures that handlers registered late by other mods are also successfully copied.
     *
     * @param fromRecipeCategoryUid the source recipe category UID to copy handlers from
     * @param toRecipeCategoryUid   the target recipe category UID to apply the copied handlers to
     */
    void copyRecipeTransferHandlers(String fromRecipeCategoryUid, String toRecipeCategoryUid);
}
