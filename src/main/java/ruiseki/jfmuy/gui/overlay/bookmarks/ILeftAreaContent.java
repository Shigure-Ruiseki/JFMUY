package ruiseki.jfmuy.gui.overlay.bookmarks;

import java.awt.Rectangle;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

import ruiseki.jfmuy.gui.ghost.IGhostIngredientDragSource;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;

public interface ILeftAreaContent extends IShowsRecipeFocuses, IGhostIngredientDragSource {

    void drawScreen(Minecraft minecraft, int mouseX, int mouseY);

    void drawOnForeground(GuiContainer gui, int mouseX, int mouseY);

    void drawTooltips(Minecraft minecraft, int mouseX, int mouseY);

    void updateBounds(Rectangle area, Set<Rectangle> guiExclusionAreas);

    boolean handleMouseScrolled(int mouseX, int mouseY, int dWheel);

    boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton);

    boolean onKeyPressed(char typedChar, int eventKey);
}
