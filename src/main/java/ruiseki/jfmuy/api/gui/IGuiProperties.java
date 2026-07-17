package ruiseki.jfmuy.api.gui;

import net.minecraft.client.gui.GuiScreen;

/**
 * Defines the properties of a gui so that JFMUY can draw next to it.
 */
public interface IGuiProperties {

    Class<? extends GuiScreen> getGuiClass();

    int getGuiLeft();

    int getGuiTop();

    int getGuiXSize();

    int getGuiYSize();

    int getScreenWidth();

    int getScreenHeight();
}
