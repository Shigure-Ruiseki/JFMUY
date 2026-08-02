package ruiseki.jfmuy.plugins.nei;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiAdapter;
import ruiseki.jfmuy.config.Config;

public class NEIScreenHandler extends INEIGuiAdapter {

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        if (Config.isOverlayEnabled()) {
            currentVisibility.showNEI = false;
            currentVisibility.showWidgets = false;
            currentVisibility.showItemSection = false;
            currentVisibility.showSearchSection = false;
            currentVisibility.showUtilityButtons = false;
            currentVisibility.showBookmarkPanel = false;
            currentVisibility.showSubsetDropdown = false;
            currentVisibility.showItemPanel = false;
            currentVisibility.enableDeleteMode = false;
            currentVisibility.showStateButtons = false;
        }

        return currentVisibility;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return Config.isOverlayEnabled();
    }
}
