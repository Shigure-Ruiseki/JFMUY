package ruiseki.jfmuy.api;

import javax.annotation.Nonnull;

public interface IJFMUYRuntime {

    @Nonnull
    IRecipeRegistry getRecipeRegistry();

    @Nonnull
    IItemListOverlay getItemListOverlay();

    /**
     * @since JEI 3.2.12
     */
    @Nonnull
    IRecipesGui getRecipesGui();
}
