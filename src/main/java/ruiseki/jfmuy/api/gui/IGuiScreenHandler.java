package ruiseki.jfmuy.api.gui;

import java.util.function.Function;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;

/**
 * Creates {@link IGuiProperties} from a {@link GuiScreen} so JFMUY can draw next to it.
 * By default, JFMUY already handles this for all {@link GuiContainer}.
 * Register a {@link IGuiScreenHandler} with JFMUY by using
 * {@link IModRegistry#addGuiScreenHandler(Class, IGuiScreenHandler)}
 */
@FunctionalInterface
public interface IGuiScreenHandler<T extends GuiScreen> extends Function<T, IGuiProperties> {

    @Override
    @Nullable
    IGuiProperties apply(T guiScreen);

    /**
     * Return anything under the mouse that JFMUY could not normally detect, used for JFMUY recipe lookups.
     * <p>
     * This is useful for screens that don't have normal slots (which is how JFMUY normally detects items under the
     * mouse).
     * <p>
     * This can also be used to let JFMUY look up liquids in tanks directly, by returning a FluidStack.
     * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
     *
     * @param mouseX the current X position of the mouse in screen coordinates.
     * @param mouseY the current Y position of the mouse in screen coordinates.
     */
    @Nullable
    default Object getIngredientUnderMouse(T guiScreen, int mouseX, int mouseY) {
        return null;
    }
}
