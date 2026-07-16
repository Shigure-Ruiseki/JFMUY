package ruiseki.jfmuy.gui.overlay;

import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import ruiseki.jfmuy.api.IIngredientListOverlay;
import ruiseki.jfmuy.api.gui.IGuiProperties;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.GuiProperties;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.elements.GuiIconToggleButton;
import ruiseki.jfmuy.gui.ghost.GhostIngredientDragManager;
import ruiseki.jfmuy.gui.ghost.IGhostIngredientDragSource;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.input.GuiTextFieldFilter;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.input.IMouseHandler;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;
import ruiseki.jfmuy.util.CommandUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.okcore.client.renderer.GlStateManager;

public class IngredientListOverlay
    implements IIngredientListOverlay, IMouseHandler, IShowsRecipeFocuses, IGhostIngredientDragSource {

    private static final int BORDER_PADDING = 2;
    private static final int BUTTON_SIZE = 20;
    private static final int SEARCH_HEIGHT = 20;
    private boolean hasRoom;

    private static boolean isSearchBarCentered(IGuiProperties guiProperties) {
        return Config.isCenterSearchBarEnabled()
            && guiProperties.getGuiTop() + guiProperties.getGuiYSize() + SEARCH_HEIGHT
                < guiProperties.getScreenHeight();
    }

    private final IngredientFilter ingredientFilter;
    private final GuiIconToggleButton configButton;
    private final IngredientGridWithNavigation contents;
    private final GuiScreenHelper guiScreenHelper;
    private final GuiTextFieldFilter searchField;
    @Deprecated
    private final GhostIngredientDragManager ghostIngredientDragManager; // Meant to keep compatibility with other mods.
    private Rectangle displayArea = new Rectangle();

    // properties of the gui we're beside
    @Nullable
    private IGuiProperties guiProperties;

    public IngredientListOverlay(IngredientFilter ingredientFilter, IngredientRegistry ingredientRegistry,
        GuiScreenHelper guiScreenHelper, GhostIngredientDragManager dragManager) {
        this.ingredientFilter = ingredientFilter;
        this.guiScreenHelper = guiScreenHelper;

        this.contents = new IngredientGridWithNavigation(ingredientFilter, guiScreenHelper, GridAlignment.LEFT);
        ingredientFilter.addListener(() -> onSetFilterText(Config.getFilterText()));
        this.searchField = new GuiTextFieldFilter(0, ingredientFilter);
        this.configButton = ConfigButton.create(this);
        this.ghostIngredientDragManager = dragManager;
        this.setKeyboardFocus(false);
    }

    public void rebuildItemFilter() {
        Log.get()
            .info("Updating ingredient filter...");
        long start_time = System.currentTimeMillis();
        this.ingredientFilter.modesChanged();
        Log.get()
            .info("Updated ingredient filter in {} ms", System.currentTimeMillis() - start_time);
        updateLayout(true);
    }

    public void invalidateBuffer() {
        this.contents.invalidateBuffer();
    }

    public boolean isListDisplayed() {
        return Config.isOverlayEnabled() && this.guiProperties != null && this.hasRoom;
    }

    private static Rectangle getDisplayArea(IGuiProperties guiProperties) {
        final int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + BORDER_PADDING;
        final int y = BORDER_PADDING;
        final int width = guiProperties.getScreenWidth() - x - BORDER_PADDING;
        final int height = guiProperties.getScreenHeight() - y - BORDER_PADDING;
        return new Rectangle(x, y, width, height);
    }

    public void updateScreen(@Nullable GuiScreen guiScreen, boolean forceUpdate) {
        final boolean wasDisplayed = isListDisplayed();
        IGuiProperties guiProperties = guiScreenHelper.getGuiProperties(guiScreen);
        if (guiProperties == null) {
            if (this.guiProperties != null) {
                this.guiProperties = null;
                setKeyboardFocus(false);
            }
        } else {
            if (forceUpdate || this.guiProperties == null
                || !GuiProperties.areEqual(this.guiProperties, guiProperties)) {
                this.guiProperties = guiProperties;
                this.displayArea = getDisplayArea(guiProperties);

                final boolean searchBarCentered = isSearchBarCentered(guiProperties);
                final int searchHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + BORDER_PADDING;

                Set<Rectangle> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
                Rectangle availableContentsArea = new Rectangle(
                    displayArea.x,
                    displayArea.y,
                    displayArea.width,
                    displayArea.height - searchHeight);

                hasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas, 4 * BUTTON_SIZE);

                // update area to match contents size
                Rectangle contentsArea = this.contents.getArea();
                displayArea.x = contentsArea.x;
                displayArea.width = contentsArea.width;

                if (Config.hideBottomRightCornerConfigButton()) {
                    if (searchBarCentered && isListDisplayed()) searchField.updateBounds(
                        new Rectangle(
                            guiProperties.getGuiLeft(),
                            guiProperties.getScreenHeight() - SEARCH_HEIGHT - BORDER_PADDING,
                            guiProperties.getGuiXSize() + 1,
                            SEARCH_HEIGHT));
                    else searchField.updateBounds(
                        new Rectangle(
                            displayArea.x,
                            displayArea.y + displayArea.height - SEARCH_HEIGHT - BORDER_PADDING,
                            displayArea.width + 1,
                            SEARCH_HEIGHT));
                } else {
                    if (searchBarCentered && isListDisplayed()) searchField.updateBounds(
                        new Rectangle(
                            guiProperties.getGuiLeft(),
                            guiProperties.getScreenHeight() - SEARCH_HEIGHT - BORDER_PADDING,
                            guiProperties.getGuiXSize() - BUTTON_SIZE + 1,
                            SEARCH_HEIGHT));
                    else searchField.updateBounds(
                        new Rectangle(
                            displayArea.x,
                            displayArea.y + displayArea.height - SEARCH_HEIGHT - BORDER_PADDING,
                            displayArea.width - BUTTON_SIZE + 1,
                            SEARCH_HEIGHT));
                }

                if (Config.hideBottomRightCornerConfigButton()) this.configButton.updateBounds(
                    new Rectangle(searchField.xPosition + searchField.width - 1, searchField.yPosition, 0, 0));
                else this.configButton.updateBounds(
                    new Rectangle(
                        searchField.xPosition + searchField.width - 1,
                        searchField.yPosition,
                        BUTTON_SIZE,
                        BUTTON_SIZE));

                updateLayout(false);
            }
        }
        if (wasDisplayed && !isListDisplayed()) {
            Config.saveFilterText();
        }
    }

    public void updateLayout(boolean filterChanged) {
        this.contents.updateLayout(filterChanged);
        this.searchField.update();
    }

    public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.guiProperties != null) {
            if (isListDisplayed()) {
                GlStateManager.disableLighting();
                this.searchField.drawTextBox();
                this.contents.draw(minecraft, mouseX, mouseY);
                this.configButton.draw(minecraft, mouseX, mouseY);
            } else {
                this.configButton.draw(minecraft, mouseX, mouseY);
            }
        }
    }

    public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
        if (isListDisplayed()) {
            this.configButton.drawTooltips(minecraft, mouseX, mouseY);
            this.contents.drawTooltips(minecraft, mouseX, mouseY);
        } else if (this.guiProperties != null) {
            this.configButton.drawTooltips(minecraft, mouseX, mouseY);
        }
    }

    public void drawOnForeground(Minecraft minecraft, GuiContainer gui, int mouseX, int mouseY) {

    }

    public void handleTick() {
        if (this.isListDisplayed()) {
            this.searchField.updateCursorCounter();
        }
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        if (isListDisplayed()) {
            if (Config.isCenterSearchBarEnabled() && searchField.isMouseOver(mouseX, mouseY)) {
                return true;
            }
            return displayArea.contains(mouseX, mouseY);
        } else if (this.guiProperties != null) {
            return this.configButton.isMouseOver(mouseX, mouseY);
        }
        return false;
    }

    @Override
    @Nullable
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        if (isListDisplayed()) {
            IClickedIngredient<?> clicked = this.contents.getIngredientUnderMouse(mouseX, mouseY);
            if (clicked != null) {
                clicked.setOnClickHandler(() -> setKeyboardFocus(false));
                return clicked;
            }
        }
        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return this.isListDisplayed() && this.contents.canSetFocusWithMouse();
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isListDisplayed()) {
            if (this.configButton.handleMouseClick(mouseX, mouseY)) {
                return true;
            }

            if (!isMouseOver(mouseX, mouseY)) {
                setKeyboardFocus(false);
                return false;
            }

            if (this.contents.handleMouseClicked(mouseX, mouseY, mouseButton)) {
                setKeyboardFocus(false);
                return true;
            }

            boolean searchClicked = this.searchField.isMouseOver(mouseX, mouseY);
            setKeyboardFocus(searchClicked);
            if (searchClicked) {
                final boolean updated = this.searchField.handleMouseClicked(mouseX, mouseY, mouseButton);
                if (updated) {
                    updateLayout(false);
                }
                return true;
            }

            Minecraft minecraft = Minecraft.getMinecraft();
            GuiScreen currentScreen = minecraft.currentScreen;
            if (currentScreen != null && !(currentScreen instanceof RecipesGui)
                && (mouseButton == 0 || mouseButton == 1
                    || minecraft.gameSettings.keyBindPickBlock.getKeyCode() == mouseButton - 100)) {
                IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
                if (clicked != null) {
                    if (Config.isCheatItemsEnabled()) {
                        ItemStack clickWithStack = minecraft.thePlayer.inventory.getItemStack();
                        ItemStack itemStack = clicked.replaceWithCheatItemStack(clickWithStack);
                        if (itemStack == null) {
                            itemStack = clicked.getCheatItemStack();
                        }
                        if (itemStack != null) {
                            CommandUtil.giveStack(itemStack, mouseButton);
                        }
                        clicked.onClickHandled();
                        return true;
                    }
                }
            }
        } else if (this.guiProperties != null) {
            return this.configButton.handleMouseClick(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
        return isListDisplayed() && isMouseOver(mouseX, mouseY)
            && this.contents.handleMouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean hasKeyboardFocus() {
        return isListDisplayed() && this.searchField.isFocused();
    }

    public void setKeyboardFocus(boolean keyboardFocus) {
        this.searchField.setFocused(keyboardFocus);
    }

    public boolean onGlobalKeyPressed(int eventKey) {
        if (isListDisplayed()) {
            if (KeyBindings.toggleCheatMode.getKeyCode() == eventKey) {
                Config.toggleCheatItemsEnabled();
                return true;
            }
            if (KeyBindings.toggleEditMode.getKeyCode() == eventKey) {
                Config.toggleEditModeEnabled();
                return true;
            }
            if (KeyBindings.focusSearch.getKeyCode() == eventKey) {
                setKeyboardFocus(true);
                return true;
            }
        }
        return false;
    }

    public boolean onKeyPressed(char typedChar, int eventKey) {
        if (isListDisplayed()) {
            if (hasKeyboardFocus() && searchField.textboxKeyTyped(typedChar, eventKey)) {
                boolean changed = Config.setFilterText(searchField.getText());
                if (changed) {
                    updateLayout(true);
                }
                return true;
            }
            return this.contents.onKeyPressed(typedChar, eventKey);
        }
        return false;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse() {
        if (isListDisplayed()) {
            IIngredientListElement elementUnderMouse = this.contents.getElementUnderMouse();
            if (elementUnderMouse != null) {
                return elementUnderMouse.getIngredient();
            }
        }
        return null;
    }

    public void onSetFilterText(String filterText) {
        this.searchField.setText(filterText);
        updateLayout(true);
    }

    @Override
    public ImmutableList<Object> getVisibleIngredients() {
        if (isListDisplayed()) {
            ImmutableList.Builder<Object> visibleIngredients = ImmutableList.builder();
            List<IIngredientListElement> visibleElements = this.contents.getVisibleElements();
            for (IIngredientListElement element : visibleElements) {
                Object ingredient = element.getIngredient();
                visibleIngredients.add(ingredient);
            }

            return visibleIngredients.build();
        }
        return ImmutableList.of();
    }

    @Override
    @Nullable
    public IIngredientListElement getElementUnderMouse() {
        return this.contents.getElementUnderMouse();
    }
}
