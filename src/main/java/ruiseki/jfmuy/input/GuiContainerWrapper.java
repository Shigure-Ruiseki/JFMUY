package ruiseki.jfmuy.input;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.JFMUYRuntime;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;

public class GuiContainerWrapper implements IShowsRecipeFocuses {

    @Nullable
    private static final Field slotField;

    static {
        Field field = null;
        try {
            try {
                field = GuiContainer.class.getDeclaredField("theSlot");
            } catch (NoSuchFieldException e) {
                field = GuiContainer.class.getDeclaredField("field_147006_u");
            }
            field.setAccessible(true);
        } catch (Exception e) {
            System.err.println("Failed to find theSlot field in GuiContainer! " + e.getMessage());
        }
        slotField = field;
    }

    @Nullable
    private Slot getSlotUnderMouse(GuiContainer guiContainer) {
        if (slotField == null) return null;
        try {
            return (Slot) slotField.get(guiContainer);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (!(guiScreen instanceof GuiContainer)) {
            return null;
        }
        GuiContainer guiContainer = (GuiContainer) guiScreen;

        Slot slotUnderMouse = getSlotUnderMouse(guiContainer);

        if (slotUnderMouse != null) {
            ItemStack stack = slotUnderMouse.getStack();
            if (stack != null) {
                return new ClickedIngredient<ItemStack>(stack);
            }
        }

        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime != null) {
            List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = runtime.getActiveAdvancedGuiHandlers(guiScreen);
            for (IAdvancedGuiHandler advancedGuiHandler : activeAdvancedGuiHandlers) {
                Object clicked;
                try {
                    clicked = advancedGuiHandler.getIngredientUnderMouse(guiContainer, mouseX, mouseY);
                } catch (AbstractMethodError ignored) { // legacy
                    continue;
                }
                if (clicked != null) {
                    return new ClickedIngredient<Object>(clicked);
                }
            }
        }

        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
