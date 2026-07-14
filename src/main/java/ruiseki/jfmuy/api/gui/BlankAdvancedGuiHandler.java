package ruiseki.jfmuy.api.gui;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.Nullable;

public abstract class BlankAdvancedGuiHandler<T extends GuiContainer> implements IAdvancedGuiHandler<T> {

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(T guiContainer) {
        return null;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
        return null;
    }
}
