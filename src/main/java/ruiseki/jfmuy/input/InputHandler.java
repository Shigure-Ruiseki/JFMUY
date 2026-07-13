package ruiseki.jfmuy.input;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import ruiseki.jfmuy.GuiEventHandler;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.gui.RecipesGui;
import ruiseki.jfmuy.util.Commands;
import ruiseki.jfmuy.util.MouseHelper;

public class InputHandler {

    private final RecipesGui recipesGui;
    private final ItemListOverlay itemListOverlay;
    private final MouseHelper mouseHelper;

    private final List<IMouseHandler> mouseHandlers = new ArrayList<>();
    private final List<IKeyable> keyables = new ArrayList<>();
    private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();

    private boolean clickHandled = false;

    public InputHandler(RecipesGui recipesGui, ItemListOverlay itemListOverlay, GuiContainer guiContainer) {
        this.recipesGui = recipesGui;
        this.itemListOverlay = itemListOverlay;

        this.mouseHelper = new MouseHelper();

        List<ICloseable> objects = new ArrayList<>();
        objects.add(recipesGui);
        objects.add(itemListOverlay);
        objects.add(new GuiContainerWrapper(guiContainer, recipesGui));

        for (Object gui : objects) {
            if (gui instanceof IMouseHandler) {
                mouseHandlers.add((IMouseHandler) gui);
            }
            if (gui instanceof IKeyable) {
                keyables.add((IKeyable) gui);
            }
            if (gui instanceof IShowsRecipeFocuses) {
                showsRecipeFocuses.add((IShowsRecipeFocuses) gui);
            }
        }
    }

    public boolean handleForgeMouseEvent(net.minecraftforge.client.event.MouseEvent event, int mouseX, int mouseY) {
        if (event.button >= 0) {
            if (event.buttonstate) {
                if (!clickHandled) {
                    boolean handled = handleMouseClick(event.button, mouseX, mouseY);
                    clickHandled = handled;
                    return handled;
                }
            } else if (clickHandled) {
                clickHandled = false;
                return true;
            }
        } else if (event.dwheel != 0) {
            return handleMouseScroll(event.dwheel, mouseX, mouseY);
        }
        return false;
    }

    /**
     * Handles a mouse click using the same coordinates as
     * {@link net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent}.
     * Used on 1.7.10 where draw-time mouse position can differ from queued mouse events.
     */
    public boolean onMouseClicked(int mouseButton, int mouseX, int mouseY) {
        if (clickHandled) {
            return true;
        }
        boolean handled = handleMouseClick(mouseButton, mouseX, mouseY);
        clickHandled = handled;
        return handled;
    }

    public void onMouseReleased(int mouseButton) {
        if (clickHandled) {
            clickHandled = false;
        }
    }

    public boolean onMouseScrolled(int scrollDelta, int mouseX, int mouseY) {
        return handleMouseScroll(scrollDelta, mouseX, mouseY);
    }

    public boolean shouldBlockInventoryMouseInput(int mouseX, int mouseY) {
        if (recipesGui.isOpen()) {
            return true;
        }
        return itemListOverlay.isMouseOver(mouseX, mouseY);
    }

    private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
        for (IMouseHandler scrollable : mouseHandlers) {
            if (scrollable.handleMouseScrolled(mouseX, mouseY, dWheel)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleMouseClick(int mouseButton, int mouseX, int mouseY) {
        for (IMouseHandler clickable : mouseHandlers) {
            if (clickable.handleMouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }

        Focus focus = getFocusUnderMouseForClick(mouseX, mouseY);
        if (focus != null && handleMouseClickedFocus(mouseButton, focus)) {
            return true;
        }

        return recipesGui.isOpen();
    }

    @Nullable
    private Focus getFocusUnderMouseForClick(int mouseX, int mouseY) {
        for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
            if (!(gui instanceof IMouseHandler)) {
                continue;
            }

            Focus focus = gui.getFocusUnderMouse(mouseX, mouseY);
            if (focus != null) {
                return focus;
            }
        }
        return null;
    }

    @Nullable
    private Focus getFocusUnderMouseForKey(int mouseX, int mouseY) {
        for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
            Focus focus = gui.getFocusUnderMouse(mouseX, mouseY);
            if (focus != null) {
                return focus;
            }
        }
        return null;
    }

    private boolean handleMouseClickedFocus(int mouseButton, @Nonnull Focus focus) {
        if (Config.isEditModeEnabled() && GuiScreen.isCtrlKeyDown()) {
            if (handleClickEditStack(mouseButton, focus)) {
                return true;
            }
        }

        if (Config.isCheatItemsEnabled() && focus.getStack() != null) {
            if (mouseButton == 0) {
                Commands.giveFullStack(focus.getStack());
                return true;
            } else if (mouseButton == 1) {
                Commands.giveOneFromStack(focus.getStack());
                return true;
            }
        }

        if (mouseButton == 0) {
            recipesGui.showRecipes(focus);
            return true;
        } else if (mouseButton == 1) {
            recipesGui.showUses(focus);
            return true;
        }

        return false;
    }

    private boolean handleClickEditStack(int mouseButton, @Nonnull Focus focus) {
        ItemStack itemStack = focus.getStack();
        if (itemStack == null) {
            return false;
        }

        boolean wildcard;
        if (mouseButton == 0) {
            wildcard = false;
        } else if (mouseButton == 1) {
            wildcard = true;
        } else {
            return false;
        }

        if (Config.isItemOnConfigBlacklist(focus.getStack(), wildcard)) {
            Config.removeItemFromConfigBlacklist(focus.getStack(), wildcard);
        } else {
            Config.addItemToConfigBlacklist(focus.getStack(), wildcard);
        }
        return true;
    }

    public boolean handleKeyEvent() {
        boolean cancelEvent = false;
        if (Keyboard.getEventKeyState()) {
            int eventKey = Keyboard.getEventKey();
            cancelEvent = handleKeyDown(eventKey);
        }
        return cancelEvent;
    }

    /**
     * Called from {@link GuiEventHandler} during client tick start, before Minecraft drains the keyboard queue.
     */
    public void handleGuiKeyboardEarly() {
        if (itemListOverlay.hasKeyboardFocus()) {
            while (Keyboard.next()) {
                if (Keyboard.getEventKeyState()) {
                    itemListOverlay.handleSearchKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
                }
            }
            return;
        }

        if (!recipesGui.isOpen()) {
            return;
        }

        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                handleKeyDown(Keyboard.getEventKey());
            }
        }
    }

    private boolean handleKeyDown(int eventKey) {
        for (IKeyable keyable : keyables) {
            if (keyable.isOpen() && keyable.hasKeyboardFocus()) {
                if (isInventoryCloseKey(eventKey)) {
                    keyable.setKeyboardFocus(false);
                    return true;
                } else if (keyable.onKeyPressed(eventKey)) {
                    return true;
                }
            }
        }

        if (isInventoryCloseKey(eventKey) || isInventoryToggleKey(eventKey)) {
            if (recipesGui.isOpen()) {
                recipesGui.close();
                return true;
            }
        }

        if (eventKey == KeyBindings.showRecipe.getKeyCode()) {
            Focus focus = getFocusUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
            if (focus != null) {
                recipesGui.showRecipes(focus);
                return true;
            }
        } else if (eventKey == KeyBindings.showUses.getKeyCode()) {
            Focus focus = getFocusUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
            if (focus != null) {
                recipesGui.showUses(focus);
                return true;
            }
        } else if (eventKey == KeyBindings.toggleOverlay.getKeyCode() && GuiScreen.isCtrlKeyDown()) {
            itemListOverlay.toggleEnabled();
            return false;
        } else if (eventKey == Keyboard.KEY_F && GuiScreen.isCtrlKeyDown()) {
            itemListOverlay.setKeyboardFocus(true);
            return true;
        } else if (eventKey == Keyboard.KEY_BACK) {
            if (recipesGui.isOpen()) {
                recipesGui.back();
                return true;
            }
        }

        for (IKeyable keyable : keyables) {
            if (keyable.isOpen() && keyable.onKeyPressed(eventKey)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInventoryToggleKey(int keyCode) {
        return keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();
    }

    private boolean isInventoryCloseKey(int keyCode) {
        return keyCode == Keyboard.KEY_ESCAPE;
    }

}
