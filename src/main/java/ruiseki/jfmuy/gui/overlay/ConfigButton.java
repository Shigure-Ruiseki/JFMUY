package ruiseki.jfmuy.gui.overlay;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.JFMUYModConfigGui;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.elements.GuiIconToggleButton;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.key.KeyBindingOK;

public class ConfigButton extends GuiIconToggleButton {

    public static ConfigButton create(IngredientListOverlay parent) {
        GuiHelper guiHelper = Internal.getHelpers()
            .getGuiHelper();
        return new ConfigButton(guiHelper.getConfigButtonIcon(), guiHelper.getConfigButtonCheatIcon(), parent);
    }

    private final IngredientListOverlay parent;

    private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, IngredientListOverlay parent) {
        super(disabledIcon, enabledIcon);
        this.parent = parent;
    }

    @Override
    protected void getTooltips(List<String> tooltip) {
        tooltip.add(Translator.translateToLocal("jfmuy.tooltip.config"));
        if (Config.isOverlayEnabled() && Config.isCollapsibleGroupsEnabled()) {
            tooltip
                .add(EnumChatFormatting.GOLD + Translator.translateToLocal("jfmuy.tooltip.config.expandCollapseAll"));
        }
        if (!Config.isOverlayEnabled()) {
            tooltip
                .add(EnumChatFormatting.GOLD + Translator.translateToLocal("jfmuy.tooltip.ingredient.list.disabled"));
            tooltip.add(
                EnumChatFormatting.GOLD + Translator.translateToLocalFormatted(
                    "jfmuy.tooltip.ingredient.list.disabled.how.to.fix",
                    KeyBindings.toggleOverlay.getDisplayName()));
        } else if (!parent.isListDisplayed()) {
            tooltip.add(EnumChatFormatting.GOLD + Translator.translateToLocal("jfmuy.tooltip.not.enough.space"));
        }
        if (Config.isCheatItemsEnabled()) {
            tooltip
                .add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.cheat.mode.button.enabled"));
            KeyBindingOK toggleCheatMode = KeyBindings.toggleCheatMode;
            if (toggleCheatMode.getKeyCode() != 0) {
                tooltip.add(
                    EnumChatFormatting.RED + Translator.translateToLocalFormatted(
                        "jfmuy.tooltip.cheat.mode.how.to.disable.hotkey",
                        toggleCheatMode.getDisplayName()));
            } else {
                tooltip.add(
                    EnumChatFormatting.RED + Translator.translateToLocalFormatted(
                        "jfmuy.tooltip.cheat.mode.how.to.disable.no.hotkey",
                        Translator.translateToLocal("key.jfmuy.ctrl")));
            }
        }
    }

    @Override
    protected boolean isIconToggledOn() {
        return Config.isCheatItemsEnabled();
    }

    @Override
    protected boolean onMouseClicked(int mouseX, int mouseY) {
        if (Config.isOverlayEnabled()) {
            if ((Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))
                && Config.isCollapsibleGroupsEnabled()
                && Internal.hasIngredientFilter()) {
                Internal.getCollapsedGroupRegistry()
                    .expandOrCloseAll();
                Internal.getIngredientFilter()
                    .notifyCollapsedStateChanged();
            } else if (Keyboard.getEventKeyState() && (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL
                || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)) {
                    Config.toggleCheatItemsEnabled();
                } else {
                    Minecraft minecraft = Minecraft.getMinecraft();
                    if (minecraft.currentScreen != null) {
                        GuiScreen configScreen = new JFMUYModConfigGui(minecraft.currentScreen);
                        parent.updateScreen(configScreen, false);
                        minecraft.displayGuiScreen(configScreen);
                    }
                }
            return true;
        }
        return false;
    }
}
