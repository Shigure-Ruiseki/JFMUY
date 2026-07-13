package ruiseki.jfmuy.api;

/**
 * The main class for a plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the @NEIPlugin annotation to get loaded by JEI.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {

    /**
     * Called when the IJFMUYHelpers is available.
     * IModPlugins should store IJFMUYHelpers here if they need it.
     */
    void onJFMUYHelpersAvailable(IJFMUYHelpers jfmuyHelpers);

    /**
     * Called when the IItemRegistry is available, before register.
     */
    void onItemRegistryAvailable(IItemRegistry itemRegistry);

    /**
     * Register this mod plugin with the mod registry.
     * Called just before the game launches.
     * Will be called again if config
     */
    void register(IModRegistry registry);

    /**
     * Called when the IRecipeRegistry is available, after all mods have registered.
     */
    void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry);
}
