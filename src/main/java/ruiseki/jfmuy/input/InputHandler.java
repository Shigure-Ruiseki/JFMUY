package ruiseki.jfmuy.input;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.transfer.IAutocraftingHandler;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.IngredientBlacklistType;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.ghost.GhostIngredientDragManager;
import ruiseki.jfmuy.gui.ghost.IGhostIngredientDragSource;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.LeftAreaDispatcher;
import ruiseki.jfmuy.gui.recipes.RecipeClickableArea;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.ingredients.group.CollapsedGroupIngredient;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.util.ReflectionUtil;
import ruiseki.okcore.client.key.KeyBindingOK;
import ruiseki.okcore.event.input.KeyboardInputEvent;
import ruiseki.okcore.event.input.MouseInputEvent;

public class InputHandler {

    private enum KeyBind {

        SHOW_RECIPE(KeyBindings.showRecipe),
        SHOW_USES(KeyBindings.showUses),
        BOOKMARK(KeyBindings.bookmark),
        BOOKMARK_TO_TOP(KeyBindings.bookmarkToTop),
        RECIPE_BOOKMARK(KeyBindings.recipeBookmark);

        private KeyBindingOK keyBind;

        KeyBind(KeyBindingOK keyBind) {
            this.keyBind = keyBind;
        }

        public boolean tryMatch(int keyCode) {
            return keyBind.isActiveAndMatches(keyCode);
        }
    }

    private final RecipeRegistry recipeRegistry;
    private final IIngredientRegistry ingredientRegistry;
    private final IngredientFilter ingredientFilter;
    private final RecipesGui recipesGui;
    private final IngredientListOverlay ingredientListOverlay;
    private final LeftAreaDispatcher leftAreaDispatcher;
    private final BookmarkList bookmarkList;
    private final IAutocraftingHandler autocraftingHandler;
    private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();
    private final IntSet clickHandled = new IntArraySet();
    private final GhostIngredientDragManager ghostIngredientDragManager;

    public InputHandler(JFMUYRuntime runtime, IngredientRegistry ingredientRegistry,
        IngredientListOverlay ingredientListOverlay, GuiScreenHelper guiScreenHelper,
        LeftAreaDispatcher leftAreaDispatcher, BookmarkList bookmarkList,
        GhostIngredientDragManager ghostIngredientDragManager) {
        this.recipeRegistry = runtime.getRecipeRegistry();
        this.ingredientRegistry = ingredientRegistry;
        this.ingredientFilter = runtime.getIngredientFilter();
        this.recipesGui = runtime.getRecipesGui();
        this.ingredientListOverlay = ingredientListOverlay;
        this.leftAreaDispatcher = leftAreaDispatcher;
        this.bookmarkList = bookmarkList;
        this.autocraftingHandler = runtime.getAutocraftingHandler();
        this.ghostIngredientDragManager = ghostIngredientDragManager;

        this.showsRecipeFocuses.add(recipesGui);
        this.showsRecipeFocuses.add(ingredientListOverlay);
        this.showsRecipeFocuses.add(leftAreaDispatcher);
        this.showsRecipeFocuses.add(new GuiContainerWrapper(guiScreenHelper));
    }

    /**
     * When we have keyboard focus, use Pre
     */
    @SubscribeEvent
    public void onGuiKeyboardEvent(KeyboardInputEvent.Pre event) {
        if (hasKeyboardFocus() && handleKeyEvent()) {
            event.setCanceled(true);
        }
    }

    /**
     * Without focus, use Post
     */
    @SubscribeEvent
    public void onGuiKeyboardEvent(KeyboardInputEvent.Post event) {
        if (hasKeyboardFocus()) return;
        else if (handleKeyEvent()) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onGuiMouseEvent(MouseInputEvent.Pre event) {
        GuiScreen guiScreen = event.gui;
        Minecraft minecraft = guiScreen.mc;
        if (minecraft != null) {
            int x = Mouse.getEventX() * guiScreen.width / minecraft.displayWidth;
            int y = guiScreen.height - Mouse.getEventY() * guiScreen.height / minecraft.displayHeight - 1;
            if (handleMouseEvent(guiScreen, x, y)) {
                event.setCanceled(true);
            }
        }
    }

    public boolean handleMouseEvent(GuiScreen guiScreen, int mouseX, int mouseY) {
        boolean cancelEvent = false;
        final int eventButton = Mouse.getEventButton();
        if (eventButton > -1) {
            if (Mouse.getEventButtonState()) {
                if (!clickHandled.contains(eventButton)) {
                    cancelEvent = handleMouseClick(guiScreen, eventButton, mouseX, mouseY);
                    if (cancelEvent) {
                        clickHandled.add(eventButton);
                    }
                }
            } else {
                cancelEvent = handleMouseRelease(guiScreen, mouseX, mouseY) || clickHandled.remove(eventButton);
            }
        } else if (Mouse.getEventDWheel() != 0) {
            cancelEvent = handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
        }
        return cancelEvent;
    }

    private boolean handleMouseRelease(GuiScreen guiScreen, int mouseX, int mouseY) {
        final int eventButton = Mouse.getEventButton();
        if (leftAreaDispatcher.handleMouseReleased(mouseX, mouseY, eventButton)) {
            return true;
        }

        return false;
    }

    private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
        return ingredientListOverlay.handleMouseScrolled(mouseX, mouseY, dWheel)
            || leftAreaDispatcher.handleMouseScrolled(mouseX, mouseY, dWheel);
    }

    private boolean handleMouseClick(GuiScreen guiScreen, int mouseButton, int mouseX, int mouseY) {
        IClickedIngredient<?> clicked = getFocusUnderMouseForClick(mouseX, mouseY);
        if (Config.isEditModeEnabled() && clicked != null && handleClickEdit(clicked)) {
            return true;
        }

        if (ingredientListOverlay.handleMouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (leftAreaDispatcher.handleMouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        IIngredientListElement<?> listElement = getElementUnderMouse();
        if (this.ghostIngredientDragManager
            .handleMouseClicked(guiScreen.mc, guiScreen, clicked, listElement, mouseX, mouseY)) {
            return true;
        }

        if (clicked != null && (Config.mouseClickToSeeRecipe() && handleMouseClickedFocus(mouseButton, clicked))) {
            return true;
        }
        if (handleFocusKeybinds(mouseButton - 100)) {
            return true;
        }

        if (guiScreen instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) guiScreen;
            RecipeClickableArea clickableArea = recipeRegistry
                .getRecipeClickableArea(guiContainer, mouseX - guiContainer.guiLeft, mouseY - guiContainer.guiTop);
            if (clickableArea != null) {
                List<String> recipeCategoryUids = clickableArea.getRecipeCategoryUids();
                recipesGui.showCategories(recipeCategoryUids);
                return true;
            }
        }

        return handleGlobalKeybinds(mouseButton - 100);
    }

    @Nullable
    private IClickedIngredient<?> getFocusUnderMouseForClick(int mouseX, int mouseY) {
        for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
            if (gui.canSetFocusWithMouse()) {
                IClickedIngredient<?> clicked = gui.getIngredientUnderMouse(mouseX, mouseY);
                if (clicked != null) {
                    return clicked;
                }
            }
        }
        return null;
    }

    @Nullable
    public IIngredientListElement<?> getElementUnderMouse() {
        for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
            if (!(gui instanceof IGhostIngredientDragSource)) {
                continue;
            }
            IIngredientListElement<?> element = ((IGhostIngredientDragSource) gui).getElementUnderMouse();
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    @Nullable
    private IClickedIngredient<?> getIngredientUnderMouseForKey(int mouseX, int mouseY) {
        for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
            IClickedIngredient<?> clicked = gui.getIngredientUnderMouse(mouseX, mouseY);
            if (clicked != null) {
                return clicked;
            }
        }
        return null;
    }

    private <V> boolean handleMouseClickedFocus(int mouseButton, IClickedIngredient<V> clicked) {
        if (mouseButton == 0) {
            IFocus<?> focus = new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue());
            recipesGui.show(focus);
            clicked.onClickHandled();
            return true;
        } else if (mouseButton == 1) {
            IFocus<?> focus = new Focus<>(IFocus.Mode.INPUT, clicked.getValue());
            recipesGui.show(focus);
            clicked.onClickHandled();
            return true;
        }

        return false;
    }

    private <V> boolean handleClickEdit(IClickedIngredient<V> clicked) {
        V ingredient = clicked.getValue();
        IngredientBlacklistType blacklistType = GuiScreen.isCtrlKeyDown() ? IngredientBlacklistType.WILDCARD
            : IngredientBlacklistType.ITEM;

        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

        if (Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
            Config.removeIngredientFromConfigBlacklist(
                ingredientFilter,
                ingredientRegistry,
                ingredient,
                blacklistType,
                ingredientHelper);
        } else {
            Config.addIngredientToConfigBlacklist(
                ingredientFilter,
                ingredientRegistry,
                ingredient,
                blacklistType,
                ingredientHelper);
        }
        clicked.onClickHandled();
        return true;
    }

    private boolean hasKeyboardFocus() {
        return ingredientListOverlay.hasKeyboardFocus() || autocraftingHandler.isActive();
    }

    private boolean handleKeyEvent() {
        char typedChar = Keyboard.getEventCharacter();
        int eventKey = Keyboard.getEventKey();

        return ((eventKey == 0 && typedChar >= 32) || Keyboard.getEventKeyState())
            && handleKeyDown(typedChar, eventKey);
    }

    private boolean handleKeyDown(char typedChar, int eventKey) {
        if (autocraftingHandler.isActive()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                autocraftingHandler.stop();
                return true;
            }
        }

        if (ghostIngredientDragManager.handleKeyDown(eventKey)) {
            return true;
        }

        if (ingredientListOverlay.hasKeyboardFocus()) {
            if (KeyBindings.isInventoryCloseKey(eventKey) || KeyBindings.isEnterKey(eventKey)) {
                ingredientListOverlay.setKeyboardFocus(false);
                return true;
            } else if (ingredientListOverlay.onKeyPressed(typedChar, eventKey)) {
                return true;
            }
        }

        if (leftAreaDispatcher.onKeyPressed(typedChar, eventKey)) {
            return true;
        }

        if (handleGlobalKeybinds(eventKey)) {
            return true;
        }

        if (!isContainerTextFieldFocused()) {
            if (KeyBindings.toggleOverlay.isActiveAndMatches(eventKey)) {
                Config.toggleOverlayEnabled();
                return true;
            }
            if (KeyBindings.toggleBookmarkOverlay.isActiveAndMatches(eventKey)) {
                Config.toggleBookmarkEnabled();
                return true;
            }
            if (handleFocusKeybinds(eventKey)) {
                return true;
            }
            return ingredientListOverlay.onKeyPressed(typedChar, eventKey);
        }

        return false;
    }

    private boolean handleGlobalKeybinds(int eventKey) {
        return ingredientListOverlay.onGlobalKeyPressed(eventKey);
    }

    private boolean handleFocusKeybinds(int eventKey) {
        KeyBind pressedKey = null;
        for (KeyBind keyBind : KeyBind.values()) {
            if (keyBind.tryMatch(eventKey)) {
                pressedKey = keyBind;
                break;
            }
        }

        if (pressedKey == null) {
            return false;
        }

        switch (pressedKey) {
            case BOOKMARK:
                return addBookmark(false);
            case RECIPE_BOOKMARK:
                return addBookmark(true);
            case BOOKMARK_TO_TOP:
                return handleBookmarkExtra();
            case SHOW_RECIPE:
                return showRecipeOrUses(IFocus.Mode.OUTPUT);
            case SHOW_USES:
                return showRecipeOrUses(IFocus.Mode.INPUT);
        }

        return false;
    }

    private boolean addBookmark(boolean isRecipe) {
        int mouseX = MouseHelper.getX();
        int mouseY = MouseHelper.getY();
        IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(mouseX, mouseY);
        if (clicked == null) {
            return false;
        }

        Object value = clicked.getValue();

        if (value instanceof BookmarkItem) {
            boolean removed = bookmarkList.remove(value);
            if (removed && bookmarkList.isEmpty() && Config.isBookmarkOverlayEnabled()) {
                Config.toggleBookmarkEnabled();
            }
            return removed;

        }

        if (!Config.isBookmarkOverlayEnabled()) {
            Config.toggleBookmarkEnabled();
        }

        if (isRecipe) {
            RecipeLayout layout = recipesGui.getRecipeLayout(mouseX, mouseY);
            if (layout == null) {
                return false;
            }

            return layout.addToBookmarks();
        }

        // Don't allow bookmarking collapsed groups directly — only individual items.
        if (value instanceof CollapsedGroupIngredient) {
            return false;
        }

        return bookmarkList.add(new BookmarkItem<>(value));
    }

    private boolean showRecipeOrUses(IFocus.Mode mode) {
        IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseHelper.getX(), MouseHelper.getY());
        if (clicked == null) {
            return false;
        }

        Object value = clicked.getValue();
        recipesGui
            .show(new Focus<>(mode, value instanceof BookmarkItem ? ((BookmarkItem<?>) value).ingredient : value));
        clicked.onClickHandled();
        return true;
    }

    private boolean isContainerTextFieldFocused() {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui == null) {
            return false;
        }
        GuiTextField textField = ReflectionUtil.getFieldWithClass(gui, GuiTextField.class);
        return textField != null && textField.getVisible() && textField.isFocused();
    }

    private boolean handleBookmarkExtra() {
        IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseHelper.getX(), MouseHelper.getY());
        if (clicked != null) {
            if (!Config.isBookmarkOverlayEnabled()) Config.toggleBookmarkEnabled();

            return bookmarkList.add(new BookmarkItem<>(clicked.getValue()), true);
        }

        return false;
    }
}
