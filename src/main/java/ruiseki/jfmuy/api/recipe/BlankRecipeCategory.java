package ruiseki.jfmuy.api.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.gui.IDrawable;

/**
 * An {@link IRecipeCategory} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {

    @Nullable
    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {

    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Collections.emptyList();
    }
}
