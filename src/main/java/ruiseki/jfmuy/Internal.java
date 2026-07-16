package ruiseki.jfmuy;

import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import ruiseki.jfmuy.color.ColorNamer;
import ruiseki.jfmuy.gui.GuiEventHandler;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.input.InputHandler;
import ruiseki.jfmuy.runtime.JFMUYHelpers;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.startup.StackHelper;

/** For JFMUY internal use only, these are normally accessed from the API. */
public final class Internal {

    @Nullable
    private static StackHelper stackHelper;
    @Nullable
    private static JFMUYHelpers helpers;
    @Nullable
    private static JFMUYRuntime runtime;
    @Nullable
    private static IngredientRegistry ingredientRegistry;
    @Nullable
    private static ColorNamer colorNamer;
    @Nullable
    private static IngredientFilter ingredientFilter;
    @Nullable
    private static GuiEventHandler guiEventHandler;
    @Nullable
    private static InputHandler inputHandler;

    private Internal() {

    }

    public static StackHelper getStackHelper() {
        Preconditions.checkState(stackHelper != null, "StackHelper has not been created yet.");
        return stackHelper;
    }

    public static void setStackHelper(StackHelper stackHelper) {
        Internal.stackHelper = stackHelper;
    }

    public static JFMUYHelpers getHelpers() {
        Preconditions.checkState(helpers != null, "JMFUYHelpers has not been created yet.");
        return helpers;
    }

    public static void setHelpers(JFMUYHelpers helpers) {
        Internal.helpers = helpers;
    }

    @Nullable
    public static JFMUYRuntime getRuntime() {
        return runtime;
    }

    public static void setRuntime(JFMUYRuntime runtime) {
        JFMUYRuntime jfmuyRuntime = Internal.runtime;
        if (jfmuyRuntime != null) {
            jfmuyRuntime.close();
        }
        Internal.runtime = runtime;
    }

    public static IngredientRegistry getIngredientRegistry() {
        Preconditions.checkState(ingredientRegistry != null, "Ingredient Registry has not been created yet.");
        return ingredientRegistry;
    }

    public static void setIngredientRegistry(IngredientRegistry ingredientRegistry) {
        Internal.ingredientRegistry = ingredientRegistry;
    }

    public static ColorNamer getColorNamer() {
        Preconditions.checkState(colorNamer != null, "Color Namer has not been created yet.");
        return colorNamer;
    }

    public static void setColorNamer(ColorNamer colorNamer) {
        Internal.colorNamer = colorNamer;
    }

    public static IngredientFilter getIngredientFilter() {
        Preconditions.checkState(ingredientFilter != null, "Ingredient Filter has not been created yet.");
        return ingredientFilter;
    }

    public static void setIngredientFilter(IngredientFilter ingredientFilter) {
        if (Internal.ingredientFilter != null) {
            MinecraftForge.EVENT_BUS.unregister(Internal.ingredientFilter);
        }
        Internal.ingredientFilter = ingredientFilter;
        MinecraftForge.EVENT_BUS.register(ingredientFilter);
    }

    public static void setGuiEventHandler(GuiEventHandler guiEventHandler) {
        if (Internal.guiEventHandler != null) {
            MinecraftForge.EVENT_BUS.unregister(Internal.guiEventHandler);
        }

        Internal.guiEventHandler = guiEventHandler;
        MinecraftForge.EVENT_BUS.register(guiEventHandler);
    }

    public static void setInputHandler(InputHandler inputHandler) {
        if (Internal.inputHandler != null) {
            MinecraftForge.EVENT_BUS.unregister(Internal.inputHandler);
        }

        Internal.inputHandler = inputHandler;
        MinecraftForge.EVENT_BUS.register(inputHandler);
    }
}
