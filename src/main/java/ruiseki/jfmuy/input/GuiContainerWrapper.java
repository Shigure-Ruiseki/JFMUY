package ruiseki.jfmuy.input;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import cpw.mods.fml.relauncher.ReflectionHelper;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.RecipesGui;

public class GuiContainerWrapper implements IShowsRecipeFocuses, IKeyable {

    private final GuiContainer guiContainer;
    private final RecipesGui recipesGui;

    public GuiContainerWrapper(GuiContainer guiContainer, RecipesGui recipesGui) {
        this.guiContainer = guiContainer;
        this.recipesGui = recipesGui;
    }

    @Nullable
    @Override
    public Focus getFocusUnderMouse(int mouseX, int mouseY) {
        if (!isOpen()) {
            return null;
        }
        Slot slotUnderMouse = null;
        try {
            Method method = ReflectionHelper.findMethod(
                GuiContainer.class,
                guiContainer,
                new String[] { "getSlotAtPosition", "func_146975_c" },
                int.class,
                int.class);
            slotUnderMouse = (Slot) method.invoke(guiContainer, mouseX, mouseY);
        } catch (Exception ignored) {}
        if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
            return new Focus(slotUnderMouse.getStack());
        }
        return null;
    }

    @Override
    public boolean hasKeyboardFocus() {
        return false;
    }

    @Override
    public void setKeyboardFocus(boolean keyboardFocus) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onKeyPressed(int keyCode) {
        return false;
    }

    @Override
    public void open() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        return (guiContainer == Minecraft.getMinecraft().currentScreen) && !recipesGui.isOpen();
    }
}
