package ruiseki.jfmuy.gui.ingredients;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.gui.Focus;

public interface IGuiIngredient<T> {

    void set(@Nonnull T contained, @Nonnull Focus focus);

    void set(@Nonnull Collection<T> contained, @Nonnull Focus focus);

    void clear();

    @Nullable
    Focus getFocus();

    @Nonnull
    List<T> getAllIngredients();

    boolean isInput();

    boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY);

    void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset);

    void drawHovered(@Nonnull Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY);

    void drawHighlight(@Nonnull Minecraft minecraft, Color color, int xOffset, int yOffset);
}
