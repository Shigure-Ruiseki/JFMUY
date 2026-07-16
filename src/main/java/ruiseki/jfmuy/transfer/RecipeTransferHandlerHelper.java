package ruiseki.jfmuy.transfer;

import java.util.Collection;

import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandlerHelper;
import ruiseki.jfmuy.api.recipe.transfer.RecipeTransferErrorInternal;
import ruiseki.jfmuy.util.ErrorUtil;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {

    @Override
    public IRecipeTransferError createInternalError() {
        return RecipeTransferErrorInternal.INSTANCE;
    }

    @Override
    public IRecipeTransferError createUserErrorWithTooltip(String tooltipMessage) {
        ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");

        return new RecipeTransferErrorTooltip(tooltipMessage);
    }

    @Override
    public IRecipeTransferError createUserErrorForSlots(String tooltipMessage, Collection<Integer> missingItemSlots) {
        ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
        ErrorUtil.checkNotEmpty(missingItemSlots, "missingItemSlots");

        return new RecipeTransferErrorSlots(tooltipMessage, missingItemSlots);
    }
}
