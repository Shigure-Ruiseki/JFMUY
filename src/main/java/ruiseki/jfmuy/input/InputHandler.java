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
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.IngredientBlacklistType;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.LeftAreaDispatcher;
import ruiseki.jfmuy.gui.recipes.RecipeClickableArea;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.util.ReflectionUtil;
import ruiseki.okcore.event.input.KeyboardInputEvent;
import ruiseki.okcore.event.input.MouseInputEvent;

public class InputHandler {

    private final RecipeRegistry recipeRegistry;
    private final IIngredientRegistry ingredientRegistry;
    private final IngredientFilter ingredientFilter;
    private final RecipesGui recipesGui;
    private final IngredientListOverlay ingredientListOverlay;
    private final GuiScreenHelper guiScreenHelper;
    private final LeftAreaDispatcher leftAreaDispatcher;
    private final BookmarkList bookmarkList;
    private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();
    private final IntSet clickHandled = new IntArraySet();

    public InputHandler(JFMUYRuntime runtime, IngredientRegistry ingredientRegistry,
        IngredientListOverlay ingredientListOverlay, GuiScreenHelper guiScreenHelper,
        LeftAreaDispatcher leftAreaDispatcher, BookmarkList bookmarkList) {
        this.recipeRegistry = runtime.getRecipeRegistry();
        this.ingredientRegistry = ingredientRegistry;
        this.ingredientFilter = runtime.getIngredientFilter();
        this.recipesGui = runtime.getRecipesGui();
        this.ingredientListOverlay = ingredientListOverlay;
        this.guiScreenHelper = guiScreenHelper;
        this.leftAreaDispatcher = leftAreaDispatcher;
        this.bookmarkList = bookmarkList;

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
        GuiScreen guiScreen = event.gui;
        if (guiScreenHelper.getGuiProperties(guiScreen) != null && hasKeyboardFocus()) {
            handleKeyEvent();
            event.setCanceled(true);
        }
    }

    /**
     * Without focus, use Post
     */
    @SubscribeEvent
    public void onGuiKeyboardEvent(KeyboardInputEvent.Post event) {
        GuiScreen guiScreen = event.gui;
        if (guiScreenHelper.getGuiProperties(guiScreen) != null && !hasKeyboardFocus() && handleKeyEvent()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseEvent(MouseInputEvent.Pre event) {
        GuiScreen guiScreen = event.gui;
        if (guiScreenHelper.getGuiProperties(guiScreen) == null) {
            return;
        }
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
            } else if (clickHandled.contains(eventButton)) {
                clickHandled.remove(eventButton);
                cancelEvent = true;
            }
        } else if (Mouse.getEventDWheel() != 0) {
            cancelEvent = handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
        }
        return cancelEvent;
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
        return ingredientListOverlay.hasKeyboardFocus();
    }

    private boolean handleKeyEvent() {
        char typedChar = Keyboard.getEventCharacter();
        int eventKey = Keyboard.getEventKey();

        return ((eventKey == 0 && typedChar >= 32) || Keyboard.getEventKeyState())
            && handleKeyDown(typedChar, eventKey);
    }

    private boolean handleKeyDown(char typedChar, int eventKey) {
        if (ingredientListOverlay.hasKeyboardFocus()) {
            if (KeyBindings.isInventoryCloseKey(eventKey) || KeyBindings.isEnterKey(eventKey)) {
                ingredientListOverlay.setKeyboardFocus(false);
                return true;
            } else if (ingredientListOverlay.onKeyPressed(typedChar, eventKey)) {
                return true;
            }
        }

        if (handleGlobalKeybinds(eventKey)) {
            return true;
        }

        if (!isContainerTextFieldFocused() && !ingredientListOverlay.hasKeyboardFocus()) {
            if (handleFocusKeybinds(eventKey)) {
                return true;
            }
            return ingredientListOverlay.onKeyPressed(typedChar, eventKey);
        }

        return false;
    }

    private boolean handleGlobalKeybinds(int eventKey) {
        if (KeyBindings.toggleOverlay.getKeyCode() == eventKey) {
            Config.toggleOverlayEnabled();
            return false;
        }
        if (KeyBindings.toggleBookmarkOverlay.getKeyCode() == eventKey) {
            Config.toggleBookmarkEnabled();
            return false;
        }
        return ingredientListOverlay.onGlobalKeyPressed(eventKey);
    }

    private boolean handleFocusKeybinds(int eventKey) {
        final boolean showRecipe = KeyBindings.showRecipe.getKeyCode() == eventKey;
        final boolean showUses = KeyBindings.showUses.getKeyCode() == eventKey;
        final boolean bookmark = KeyBindings.bookmark.getKeyCode() == eventKey;
        if (showRecipe || showUses || bookmark) {
            IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseHelper.getX(), MouseHelper.getY());
            if (clicked != null) {
                if (bookmark) {
                    if (bookmarkList.remove(clicked.getValue())) {
                        if (bookmarkList.isEmpty() && Config.isBookmarkOverlayEnabled()) {
                            Config.toggleBookmarkEnabled();
                        }
                        return true;
                    } else {
                        if (!Config.isBookmarkOverlayEnabled()) {
                            Config.toggleBookmarkEnabled();
                        }
                        return bookmarkList.add(clicked.getValue());
                    }
                } else {
                    IFocus.Mode mode = showRecipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT;
                    recipesGui.show(new Focus<Object>(mode, clicked.getValue()));
                    clicked.onClickHandled();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isContainerTextFieldFocused() {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui == null) {
            return false;
        }
        GuiTextField textField = ReflectionUtil.getFieldWithClass(gui, GuiTextField.class);
        return textField != null && textField.getVisible() && textField.isFocused();
    }

}
