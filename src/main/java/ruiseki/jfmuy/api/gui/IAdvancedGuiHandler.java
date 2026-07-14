package ruiseki.jfmuy.api.gui;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows mods to change how JFMUY is displayed next to their gui.
 */
public interface IAdvancedGuiHandler<T extends GuiContainer> {

    /**
     * @return the class that this IAdvancedGuiHandler handles.
     */
    @NotNull
    Class<T> getGuiContainerClass();

    /**
     * Give JFMUY information about extra space that the GuiContainer takes up.
     * Used for moving JFMUY out of the way of extra things like gui tabs.
     *
     * @return the space that the gui takes up besides the normal rectangle defined by GuiContainer.
     */
    @Nullable
    List<Rectangle> getGuiExtraAreas(T guiContainer);
}
