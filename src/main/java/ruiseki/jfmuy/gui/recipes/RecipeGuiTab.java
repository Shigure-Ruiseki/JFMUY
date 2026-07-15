package ruiseki.jfmuy.gui.recipes;

import java.util.List;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.client.config.HoverChecker;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.input.IMouseHandler;

public abstract class RecipeGuiTab implements IMouseHandler {

    public static final int TAB_HEIGHT = 24;
    public static final int TAB_WIDTH = 24;

    protected final int x;
    protected final int y;
    private final HoverChecker hoverChecker;

    public RecipeGuiTab(int x, int y) {
        this.x = x;
        this.y = y;
        this.hoverChecker = new HoverChecker(y, y + TAB_HEIGHT, x, x + TAB_WIDTH, 0);
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return hoverChecker.checkHover(mouseX, mouseY);
    }

    @Override
    public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
        return false;
    }

    public abstract boolean isSelected(IRecipeCategory selectedCategory);

    public void draw(Minecraft minecraft, boolean selected, int mouseX, int mouseY) {
        GuiHelper guiHelper = Internal.getHelpers()
            .getGuiHelper();
        IDrawable tab = selected ? guiHelper.getTabSelected() : guiHelper.getTabUnselected();

        tab.draw(minecraft, x, y);
    }

    public abstract List<String> getTooltip();
}
