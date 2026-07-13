package ruiseki.jfmuy.gui;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.HoverChecker;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.ItemFilter;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.JFMUYModConfigGui;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackFast;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackFastList;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackGroup;
import ruiseki.jfmuy.gui.ingredients.ItemStackRenderer;
import ruiseki.jfmuy.input.GuiTextFieldFilter;
import ruiseki.jfmuy.input.IKeyable;
import ruiseki.jfmuy.input.IMouseHandler;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;
import ruiseki.jfmuy.network.packets.PacketDeletePlayerItem;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.util.ItemStackElement;
import ruiseki.jfmuy.util.MathUtil;
import ruiseki.jfmuy.util.Translator;

public class ItemListOverlay implements IShowsRecipeFocuses, IMouseHandler, IKeyable {

    private static final int borderPadding = 4;
    private static final int searchHeight = 16;
    private static final int buttonPaddingX = 14;
    private static final int buttonPaddingY = 8;

    private static final int itemStackPadding = 1;
    private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
    private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
    private static int pageNum = 0;

    private final ItemFilter itemFilter;

    private int buttonHeight;
    private final GuiItemStackFastList guiItemStacks = new GuiItemStackFastList();
    private GuiButton nextButton;
    private GuiButton backButton;
    private GuiButton configButton;
    private IDrawable configButtonIcon;
    private HoverChecker configButtonHoverChecker;
    private GuiTextFieldFilter searchField;
    private int pageCount;

    private String pageNumDisplayString;
    private int pageNumDisplayX;
    private int pageNumDisplayY;

    private GuiItemStackFast hovered = null;

    // properties of the gui we're beside
    private int guiLeft;
    private int guiXSize;
    private int screenWidth;
    private int screenHeight;

    private boolean open = false;
    private boolean enabled = true;

    public ItemListOverlay(ItemFilter itemFilter) {
        this.itemFilter = itemFilter;
    }

    public void initGui(@Nonnull GuiContainer guiContainer) {
        this.guiLeft = guiContainer.guiLeft;
        this.guiXSize = guiContainer.xSize;
        this.screenWidth = guiContainer.width;
        this.screenHeight = guiContainer.height;

        final int columns = getColumns();
        if (columns < 4) {
            close();
            return;
        }

        String next = ">";
        String back = "<";

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final int nextButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(next);
        final int backButtonWidth = buttonPaddingX + fontRenderer.getStringWidth(back);
        buttonHeight = buttonPaddingY + fontRenderer.FONT_HEIGHT;

        final int rows = getRows();
        final int xSize = columns * itemStackWidth;
        final int xEmptySpace = screenWidth - guiLeft - guiXSize - xSize;

        final int leftEdge = guiLeft + guiXSize + (xEmptySpace / 2);
        final int rightEdge = leftEdge + xSize;

        final int yItemButtonSpace = getItemButtonYSpace();
        final int itemButtonsHeight = rows * itemStackHeight;

        final int buttonStartY = buttonHeight + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
        createItemButtons(leftEdge, buttonStartY, columns, rows);

        nextButton = new GuiButtonExt(
            0,
            rightEdge - nextButtonWidth,
            borderPadding,
            nextButtonWidth,
            buttonHeight,
            next);
        backButton = new GuiButtonExt(1, leftEdge, borderPadding, backButtonWidth, buttonHeight, back);

        int configButtonSize = searchHeight + 4;
        int configButtonX = rightEdge - configButtonSize + 1;
        int configButtonY = screenHeight - configButtonSize - borderPadding;
        configButton = new GuiButtonExt(2, configButtonX, configButtonY, configButtonSize, configButtonSize, null);
        ResourceLocation configButtonIconLocation = new ResourceLocation(
            Reference.MOD_ID,
            Reference.TEXTURE_GUI_PATH + "recipeBackground.png");
        configButtonIcon = Internal.getHelpers()
            .getGuiHelper()
            .createDrawable(configButtonIconLocation, 0, 166, 16, 16);
        configButtonHoverChecker = new HoverChecker(configButton, 0);

        int searchFieldY = screenHeight - searchHeight - borderPadding - 2;
        int searchFieldWidth = rightEdge - leftEdge - configButtonSize - 1;
        searchField = new GuiTextFieldFilter(0, fontRenderer, leftEdge, searchFieldY, searchFieldWidth, searchHeight);
        setKeyboardFocus(false);
        searchField.setItemFilter(itemFilter);

        updateLayout();

        open();
    }

    public void updateGui(@Nonnull GuiContainer guiContainer) {
        if (this.guiLeft != guiContainer.guiLeft || this.guiXSize != guiContainer.xSize
            || this.screenWidth != guiContainer.width
            || this.screenHeight != guiContainer.height) {
            initGui(guiContainer);
        }
    }

    private void createItemButtons(final int xStart, final int yStart, final int columnCount, final int rowCount) {
        guiItemStacks.clear();

        for (int row = 0; row < rowCount; row++) {
            int y = yStart + (row * itemStackHeight);
            for (int column = 0; column < columnCount; column++) {
                int x = xStart + (column * itemStackWidth);
                guiItemStacks.add(new GuiItemStackFast(x, y, itemStackPadding));
            }
        }
    }

    private void updateLayout() {
        updatePageCount();
        if (pageNum >= getPageCount()) {
            pageNum = 0;
        }
        int i = pageNum * getCountPerPage();

        ImmutableList<ItemStackElement> itemList = itemFilter.getItemList();
        guiItemStacks.set(i, itemList);

        FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRenderer;

        pageNumDisplayString = (getPageNum() + 1) + "/" + getPageCount();
        int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
        pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2
            - (pageDisplayWidth / 2);
        pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

        searchField.update();
    }

    private void nextPage() {
        if (pageNum == getPageCount() - 1) {
            setPageNum(0);
        } else {
            setPageNum(pageNum + 1);
        }
    }

    private void previousPage() {
        if (pageNum == 0) {
            setPageNum(getPageCount() - 1);
        } else {
            setPageNum(pageNum - 1);
        }
    }

    public void drawScreen(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
        if (!isOpen()) {
            return;
        }

        GL11.glDisable(GL11.GL_LIGHTING);

        minecraft.fontRenderer
            .drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
        searchField.drawTextBox();

        nextButton.drawButton(minecraft, mouseX, mouseY);
        backButton.drawButton(minecraft, mouseX, mouseY);
        configButton.drawButton(minecraft, mouseX, mouseY);
        configButtonIcon.draw(minecraft, configButton.xPosition + 2, configButton.yPosition + 2);
        GL11.glDisable(GL11.GL_BLEND);

        boolean mouseOver = isMouseOver(mouseX, mouseY);

        if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
            hovered = guiItemStacks.render(null, minecraft, false, mouseX, mouseY);

            String deleteItem = Translator.translateToLocal("jfmuy.tooltip.delete.item");
            TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
        } else {
            hovered = guiItemStacks.render(hovered, minecraft, mouseOver, mouseX, mouseY);
        }

        if (configButtonHoverChecker.checkHover(mouseX, mouseY)) {
            String configString = Translator.translateToLocal("jfmuy.tooltip.config");
            TooltipRenderer.drawHoveringText(minecraft, configString, mouseX, mouseY);
        }
    }

    private boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
        if (Config.isDeleteItemsInCheatModeActive()) {
            EntityPlayer player = minecraft.thePlayer;
            if (player.inventory.getItemStack() != null) {
                return true;
            }
        }
        return false;
    }

    public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
        if (hovered != null) {
            ItemStackRenderer.enableGuiItemRender();
            hovered.drawHovered(minecraft, mouseX, mouseY);
            ItemStackRenderer.disableGuiItemRender();

            hovered = null;
        }
    }

    public void handleTick() {
        if (searchField != null) {
            searchField.updateCursorCounter();
        }
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return isOpen() && (mouseX >= guiLeft + guiXSize);
    }

    @Override
    @Nullable
    public Focus getFocusUnderMouse(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return null;
        }

        Focus focus = guiItemStacks.getFocusUnderMouse(mouseX, mouseY);
        if (focus != null) {
            setKeyboardFocus(false);
        }
        return focus;
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isMouseOver(mouseX, mouseY)) {
            setKeyboardFocus(false);
            return false;
        }

        if (Config.isDeleteItemsInCheatModeActive()) {
            Minecraft minecraft = Minecraft.getMinecraft();
            EntityPlayerSP player = minecraft.thePlayer;
            ItemStack itemStack = player.inventory.getItemStack();
            if (itemStack != null) {
                player.inventory.setItemStack(null);
                PacketJFMUY packet = new PacketDeletePlayerItem(itemStack);
                JFMUY.getProxy()
                    .sendPacketToServer(packet);
                return true;
            }
        }

        boolean buttonClicked = handleMouseClickedButtons(mouseX, mouseY);
        if (buttonClicked) {
            setKeyboardFocus(false);
            return true;
        }

        return handleMouseClickedSearch(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (scrollDelta < 0) {
            nextPage();
            return true;
        } else if (scrollDelta > 0) {
            previousPage();
            return true;
        }
        return false;
    }

    private boolean handleMouseClickedButtons(int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
            nextPage();
            return true;
        } else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
            previousPage();
            return true;
        } else if (configButton.mousePressed(minecraft, mouseX, mouseY)) {
            close();
            GuiScreen configScreen = new JFMUYModConfigGui(minecraft.currentScreen);
            minecraft.displayGuiScreen(configScreen);
            return true;
        }
        return false;
    }

    private boolean handleMouseClickedSearch(int mouseX, int mouseY, int mouseButton) {
        boolean searchClicked = searchField.isMouseOver(mouseX, mouseY);
        setKeyboardFocus(searchClicked);
        if (searchClicked && searchField.handleMouseClicked(mouseX, mouseY, mouseButton)) {
            updateLayout();
        }
        return searchClicked;
    }

    @Override
    public boolean hasKeyboardFocus() {
        return searchField != null && searchField.isFocused();
    }

    @Override
    public void setKeyboardFocus(boolean keyboardFocus) {
        if (searchField != null) {
            searchField.setFocused(keyboardFocus);
        }
    }

    @Override
    public boolean onKeyPressed(int keyCode) {
        return handleSearchKeyTyped(Keyboard.getEventCharacter(), keyCode);
    }

    public boolean handleSearchKeyTyped(char character, int keyCode) {
        if (!hasKeyboardFocus()) {
            return false;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            setKeyboardFocus(false);
            return true;
        }
        boolean success = searchField.textboxKeyTyped(character, keyCode);
        if (success) {
            updateLayout();
        }
        return true;
    }

    private int getItemButtonXSpace() {
        return screenWidth - (guiLeft + guiXSize + (2 * borderPadding));
    }

    private int getItemButtonYSpace() {
        return screenHeight - (buttonHeight + searchHeight + 2 + (4 * borderPadding));
    }

    private int getColumns() {
        return getItemButtonXSpace() / itemStackWidth;
    }

    private int getRows() {
        return getItemButtonYSpace() / itemStackHeight;
    }

    private int getCountPerPage() {
        return getColumns() * getRows();
    }

    private void updatePageCount() {
        int count = itemFilter.size();
        pageCount = MathUtil.divideCeil(count, getCountPerPage());
        if (pageCount == 0) {
            pageCount = 1;
        }
    }

    private int getPageCount() {
        return pageCount;
    }

    private int getPageNum() {
        return pageNum;
    }

    private void setPageNum(int pageNum) {
        if (ItemListOverlay.pageNum == pageNum) {
            return;
        }
        ItemListOverlay.pageNum = pageNum;
        updateLayout();
    }

    @Override
    public void open() {
        open = true;
        setKeyboardFocus(false);
    }

    @Override
    public void close() {
        open = false;
        setKeyboardFocus(false);
    }

    @Override
    public boolean isOpen() {
        return open && enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }
}
