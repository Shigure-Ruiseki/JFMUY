package ruiseki.jfmuy.input;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import cpw.mods.fml.relauncher.ReflectionHelper;
import ruiseki.jfmuy.gui.Focus;

public class GuiContainerWrapper implements IShowsRecipeFocuses {

    @Nullable
    @Override
    public Focus getFocusUnderMouse(int mouseX, int mouseY) {
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (!(guiScreen instanceof GuiContainer)) {
            return null;
        }
        GuiContainer guiContainer = (GuiContainer) guiScreen;

        try {
            Slot slotUnderMouse = ReflectionHelper
                .getPrivateValue(GuiContainer.class, guiContainer, "theSlot", "field_147006_u");

            if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
                return new Focus(slotUnderMouse.getStack());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
