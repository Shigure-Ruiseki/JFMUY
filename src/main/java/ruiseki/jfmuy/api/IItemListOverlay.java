package ruiseki.jfmuy.api;

import java.util.Collection;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * The IItemListOverlay is JFMUY's gui that displays all the items next to an open container gui.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJFMUYRuntime#getItemListOverlay()}.
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
    void setFilterText(String filterText);

    /**
     * @return the current search filter string for the item list
     */
    String getFilterText();

    /**
     * @return a list containing all stacks that match the current filter.
     *         For the list of all ItemStacks known to JFMUY, see {@link IItemRegistry#getItemList()}.
     */
    ImmutableList<ItemStack> getFilteredStacks();

    /**
     * @return a list containing all currently visible stacks. If JFMUY is hidden, the list will be empty.
     */
    ImmutableList<ItemStack> getVisibleStacks();

    /**
     * Tells JFMUY which stacks to highlight
     */
    void highlightStacks(Collection<ItemStack> stacks);
}
