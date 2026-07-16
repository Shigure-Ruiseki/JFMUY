package ruiseki.jfmuy.gui.overlay.bookmarks;

import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.elements.GuiIconButton;
import ruiseki.jfmuy.gui.elements.GuiLabelButton;
import ruiseki.jfmuy.input.IPaged;

public class BookmarkPageNavigation {

    private final IPaged paged;
    private final GuiButton nextButton;
    private final GuiButton backButton;
    private final GuiLabelButton pageLabel;
    private final boolean hideOnSinglePage;

    public BookmarkPageNavigation(IPaged paged, boolean hideOnSinglePage) {
        this.paged = paged;
        GuiHelper guiHelper = Internal.getHelpers()
            .getGuiHelper();
        this.nextButton = new GuiIconButton(0, guiHelper.getArrowNext(), (mc, mouseX, mouseY) -> paged.nextPage());
        this.backButton = new GuiIconButton(
            1,
            guiHelper.getArrowPrevious(),
            (mc, mouseX, mouseY) -> paged.previousPage());
        this.pageLabel = new GuiLabelButton(2, "", null);
        this.hideOnSinglePage = hideOnSinglePage;
    }

    public void updateBounds(Rectangle area) {
        int buttonSize = area.height;
        this.nextButton.xPosition = area.x + area.width - buttonSize;
        this.nextButton.yPosition = area.y;
        this.nextButton.width = this.nextButton.height = buttonSize;
        this.backButton.xPosition = area.x;
        this.backButton.yPosition = area.y;
        this.backButton.width = this.backButton.height = buttonSize;
        int pagePadding = 8;
        this.pageLabel.xPosition = area.x + buttonSize + pagePadding;
        this.pageLabel.yPosition = area.y;
        this.pageLabel.width = area.width - buttonSize * 2 - pagePadding * 2;
        this.pageLabel.height = buttonSize;
    }

    public void updatePageState() {
        int pageNum = this.paged.getPageNumber();
        int pageCount = this.paged.getPageCount();
        this.pageLabel.displayString = (pageNum + 1) + "/" + pageCount;
    }

    public void draw(Minecraft minecraft, int mouseX, int mouseY) {
        if (!hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious()) {
            nextButton.drawButton(minecraft, mouseX, mouseY);
            backButton.drawButton(minecraft, mouseX, mouseY);
            pageLabel.drawButton(minecraft, mouseX, mouseY);
        }
    }

    public boolean isMouseOver() {
        return nextButton.func_146115_a() || backButton.func_146115_a() || pageLabel.func_146115_a();
    }

    public boolean handleMouseClickedButtons(int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getMinecraft();
        return nextButton.mousePressed(minecraft, mouseX, mouseY) || backButton.mousePressed(minecraft, mouseX, mouseY)
            || pageLabel.mousePressed(minecraft, mouseX, mouseY);
    }
}
