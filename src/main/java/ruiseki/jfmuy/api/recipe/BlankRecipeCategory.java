package ruiseki.jfmuy.api.recipe;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

public abstract class BlankRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {

    }

    @Override
    public void drawAnimations(@Nonnull Minecraft minecraft) {

    }
}
