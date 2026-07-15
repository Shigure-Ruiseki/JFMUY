package ruiseki.jfmuy.gui.elements;

import net.minecraft.client.Minecraft;

public interface IMouseClickedButtonCallback {

    boolean mousePressed(Minecraft mc, int mouseX, int mouseY);
}
