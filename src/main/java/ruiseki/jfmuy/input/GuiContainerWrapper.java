package ruiseki.jfmuy.input;

import java.awt.Rectangle;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.gui.GuiScreenHelper;

public class GuiContainerWrapper implements IShowsRecipeFocuses {

    private final GuiScreenHelper guiScreenHelper;

    public GuiContainerWrapper(GuiScreenHelper guiScreenHelper) {
        this.guiScreenHelper = guiScreenHelper;
    }

    @Nullable
    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (guiScreen == null) {
            return null;
        }
        if (guiScreen instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) guiScreen;
            Slot slotUnderMouse = guiContainer.theSlot;
            if (slotUnderMouse != null) {
                ItemStack stack = slotUnderMouse.getStack();
                if (stack != null) {
                    Rectangle slotArea = new Rectangle(
                        guiContainer.guiLeft + slotUnderMouse.xDisplayPosition,
                        guiContainer.guiTop + slotUnderMouse.yDisplayPosition,
                        16,
                        16);
                    return ClickedIngredient.create(stack, slotArea);
                }
            }
        }
        return guiScreenHelper.getPluginsIngredientUnderMouse(guiScreen, mouseX, mouseY);
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
