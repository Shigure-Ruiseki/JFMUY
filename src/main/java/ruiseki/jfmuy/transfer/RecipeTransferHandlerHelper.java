package ruiseki.jfmuy.transfer;

import java.util.Collection;

import javax.annotation.Nullable;

import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.util.Log;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {

    @Override
    public IRecipeTransferError createInternalError() {
        return RecipeTransferErrorInternal.instance;
    }

    @Override
    public IRecipeTransferError createUserErrorWithTooltip(@Nullable String tooltipMessage) {
        if (tooltipMessage == null) {
            Log.error("Null tooltipMessage", new NullPointerException());
            return RecipeTransferErrorInternal.instance;
        }
        return new RecipeTransferErrorTooltip(tooltipMessage);
    }

    @Override
    public IRecipeTransferError createUserErrorForSlots(@Nullable String tooltipMessage,
        @Nullable Collection<Integer> missingItemSlots) {
        if (tooltipMessage == null) {
            Log.error("Null tooltipMessage", new NullPointerException());
            return RecipeTransferErrorInternal.instance;
        }
        if (missingItemSlots == null) {
            Log.error("Null missingItemSlots", new NullPointerException());
            return RecipeTransferErrorInternal.instance;
        }
        if (missingItemSlots.isEmpty()) {
            Log.error("Empty missingItemSlots", new IllegalArgumentException());
            return RecipeTransferErrorInternal.instance;
        }

        return new RecipeTransferErrorSlots(tooltipMessage, missingItemSlots);
    }
}
