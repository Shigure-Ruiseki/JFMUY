package ruiseki.jfmuy.api.recipe.transfer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.gui.IRecipeLayout;

/**
 * A reason that a recipe transfer couldn't happen.
 * <p>
 * Recipe transfer errors can be created with {@link IRecipeTransferHandlerHelper} or you can implement your own.
 * These errors are returned from
 * {@link IRecipeTransferHandler#transferRecipe(Container, IRecipeLayout, EntityPlayer, boolean, boolean)}.
 */
public interface IRecipeTransferError {

    enum Type {

        /**
         * Errors where the Transfer handler is broken or does not work.
         * These errors will hide the recipe transfer button, and do not display anything to the user.
         */
        INTERNAL(false),

        /**
         * Errors that the player can fix. Missing items, inventory full, etc.
         * Something informative will be shown to the player.
         */
        USER_FACING(false),

        /**
         * Errors that still allow the usage of the recipe transfer button.
         * Hovering over the button will display the error, however the button is active and can be used.
         */
        COSMETIC(true);

        public final boolean allowsTransfer;

        Type(boolean allowsTransfer) {
            this.allowsTransfer = allowsTransfer;
        }
    }

    Type getType();

    /**
     * Return the ARGB color of the additional button highlight for {@link Type#COSMETIC}.
     * For example, return 0 to disable the colored highlight. Default color is orange.
     */
    default int getButtonHighlightColor() {
        return 0x80FFA500;
    }

    /**
     * Called on {@link Type#USER_FACING} errors.
     */
    void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY);

    /**
     * A reason that a recipe transfer couldn't happen, as a string.
     * <p>
     * Nullable as it is a later addition to the API.
     *
     * @return reason of why the error has occurred
     */
    @Nullable
    default String getSimpleReason() {
        return null;
    }
}
