package ruiseki.jfmuy.api;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The IItemListOverlay is JFMUY's gui that displays all the items next to an open container gui.
 * Use this interface to get information from and interact with it.
 */
public interface IItemListOverlay {

    /**
     * @return the stack that's currently under the mouse, or null if there is none
     */
    @Nullable
    ItemStack getStackUnderMouse();

    /**
     * Set the search filter string for the item list.
     */
    void setFilterText(@NotNull String filterText);

    /**
     * @return the current search filter string for the item list
     */
    @NotNull
    String getFilterText();
}
