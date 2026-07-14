package ruiseki.jfmuy.api;

import org.jetbrains.annotations.NotNull;

public interface IJFMUYRuntime {

    @NotNull
    IRecipeRegistry getRecipeRegistry();

    @NotNull
    IItemListOverlay getItemListOverlay();

    @NotNull
    IRecipesGui getRecipesGui();
}
