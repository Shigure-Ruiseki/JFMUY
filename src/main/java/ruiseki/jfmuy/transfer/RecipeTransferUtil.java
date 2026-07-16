package ruiseki.jfmuy.transfer;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.RecipeTransferErrorInternal;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.util.Log;

public class RecipeTransferUtil {

    private RecipeTransferUtil() {}

    @Nullable
    public static IRecipeTransferError getTransferRecipeError(Container container, RecipeLayout recipeLayout,
        EntityPlayer player) {
        return transferRecipe(container, recipeLayout, player, false, false);
    }

    public static boolean transferRecipe(Container container, RecipeLayout recipeLayout, EntityPlayer player,
        boolean maxTransfer) {
        IRecipeTransferError error = transferRecipe(container, recipeLayout, player, maxTransfer, true);
        return allowsTransfer(error);
    }

    @Nullable
    private static IRecipeTransferError transferRecipe(Container container, RecipeLayout recipeLayout,
        EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        final JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) {
            return RecipeTransferErrorInternal.INSTANCE;
        }

        final RecipeRegistry recipeRegistry = runtime.getRecipeRegistry();
        final IRecipeTransferHandler transferHandler = recipeRegistry
            .getRecipeTransferHandler(container, recipeLayout.getRecipeCategory());
        if (transferHandler == null) {
            if (doTransfer) {
                Log.get()
                    .error("No Recipe Transfer handler for container {}", container.getClass());
            }
            return RecipeTransferErrorInternal.INSTANCE;
        }

        try {
            // noinspection unchecked
            return transferHandler.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
        } catch (RuntimeException e) {
            Log.get()
                .error(
                    "Recipe transfer handler '{}' for container '{}' and recipe category '{}' threw an error: ",
                    transferHandler.getClass(),
                    container.getClass(),
                    recipeLayout.getRecipeCategory()
                        .getUid(),
                    e);
            return RecipeTransferErrorInternal.INSTANCE;
        }
    }

    public static boolean allowsTransfer(@Nullable IRecipeTransferError error) {
        return error == null || error.getType() == IRecipeTransferError.Type.COSMETIC;
    }
}
