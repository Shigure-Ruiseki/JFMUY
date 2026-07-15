package ruiseki.jfmuy.api;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * The IItemListOverlay is JFMUY's gui that displays all the ingredients next to an open container gui.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJFMUYRuntime#getIngredientListOverlay()}.
 */
public interface IIngredientListOverlay {

    /**
     * @return the ingredient that's currently under the mouse, or null if there is none.
     */
    @Nullable
    Object getIngredientUnderMouse();

    /**
     * @return true if the text box is focused by the player.
     */
    boolean hasKeyboardFocus();

    /**
     * @return a list containing all currently visible ingredients. If JFMUY is hidden, the list will be empty.
     */
    ImmutableList<Object> getVisibleIngredients();
}
