package ruiseki.jfmuy.plugins.modularui;

import org.jetbrains.annotations.ApiStatus;

import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;

/**
 * An interface to handle recipe transfers.
 * Implement this on {@link com.cleanroommc.modularui.screen.ModularScreen}.
 * No further registration needed.
 */
@ApiStatus.Experimental
public interface RecipeViewerRecipeTransferHandler {

    /**
     * Transfers a recipe viewer recipe.
     *
     * @param recipeLayout recipe layout
     * @param maxTransfer  true if shift is being held
     * @param simulate     if the transfer is simulated
     * @return a transfer error or null if successful
     */
    IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate);
}
