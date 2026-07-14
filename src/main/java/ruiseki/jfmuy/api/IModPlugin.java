package ruiseki.jfmuy.api;

import javax.annotation.Nonnull;

/**
 * The main class for a plugin. Everything communicated between a mod and JFMUY is through this class.
 * IModPlugins must have the @JFMUYPlugin annotation to get loaded by JFMUY.
 * This class must not import anything that could be missing at runtime (i.e. code from any other mod).
 */
public interface IModPlugin {

    /**
     * Register this mod plugin with the mod registry.
     * Called just before the game launches.
     * Will be called again if config
     */
    void register(@Nonnull IModRegistry registry);

    /**
     * Called when jfmuy's runtime features are available, after all mods have registered.
     */
    void onRuntimeAvailable(@Nonnull IJFMUYRuntime jeiRuntime);
}
