package ruiseki.jfmuy.api.recipe.transfer;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.gui.IRecipeLayout;

public class RecipeTransferErrorInternal implements IRecipeTransferError {

    public static final RecipeTransferErrorInternal INSTANCE = new RecipeTransferErrorInternal();

    private RecipeTransferErrorInternal() {

    }

    @Override
    public Type getType() {
        return Type.INTERNAL;
    }

    @Override
    public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
        int recipeY) {
        // don't show anything
    }
}
