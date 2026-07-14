package ruiseki.jfmuy;

import org.jetbrains.annotations.NotNull;

import ruiseki.jfmuy.util.StackHelper;
import ruiseki.jfmuy.util.color.ColorNamer;

/** For JFMUY internal use only, these are normally accessed from the API. */
public class Internal {

    @NotNull
    private static JFMUYHelpers helpers = new JFMUYHelpers();
    private static JFMUYRuntime runtime;
    private static ItemRegistry itemRegistry;
    private static ColorNamer colorNamer;

    private Internal() {

    }

    @NotNull
    public static JFMUYHelpers getHelpers() {
        return helpers;
    }

    public static void setHelpers(@NotNull JFMUYHelpers helpers) {
        Internal.helpers = helpers;
    }

    @NotNull
    public static StackHelper getStackHelper() {
        return helpers.getStackHelper();
    }

    public static JFMUYRuntime getRuntime() {
        return runtime;
    }

    public static void setRuntime(JFMUYRuntime runtime) {
        if (Internal.runtime != null) {
            Internal.runtime.close();
        }
        Internal.runtime = runtime;
    }

    public static ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public static void setItemRegistry(ItemRegistry itemRegistry) {
        Internal.itemRegistry = itemRegistry;
    }

    public static ColorNamer getColorNamer() {
        return colorNamer;
    }

    public static void setColorNamer(ColorNamer colorNamer) {
        Internal.colorNamer = colorNamer;
    }
}
