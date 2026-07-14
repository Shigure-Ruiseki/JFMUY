package ruiseki.jfmuy.api.recipe;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.api.ingredients.IIngredients;

/**
 * An {@link IRecipeWrapper} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeWrapper implements IRecipeWrapper {

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

    }

    @Override
    public void getIngredients(IIngredients ingredients) {

    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
