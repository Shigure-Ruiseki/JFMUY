package ruiseki.jfmuy.config;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.registry.ClientRegistry;
import ruiseki.jfmuy.Reference;

public class KeyBindings {

    private static final String categoryName = Reference.MOD_ID + " (" + Reference.MOD_NAME + ')';

    public static final KeyBinding toggleOverlay;
    public static final KeyBinding focusSearch;
    public static final KeyBinding toggleCheatMode;
    public static final KeyBinding toggleEditMode;
    public static final KeyBinding showRecipe;
    public static final KeyBinding showUses;
    public static final KeyBinding recipeBack;
    public static final KeyBinding previousPage;
    public static final KeyBinding nextPage;
    public static final KeyBinding bookmark;
    public static final KeyBinding toggleBookmarkOverlay;
    public static final KeyBinding crafting;
    private static final List<KeyBinding> allBindings;

    static {
        allBindings = ImmutableList.of(
            toggleOverlay = new KeyBinding("key.jfmuy.toggleOverlay", Keyboard.KEY_O, categoryName),
            focusSearch = new KeyBinding("key.jfmuy.focusSearch", Keyboard.KEY_F, categoryName),
            toggleCheatMode = new KeyBinding("key.jfmuy.toggleCheatMode", Keyboard.KEY_NONE, categoryName),
            toggleEditMode = new KeyBinding("key.jfmuy.toggleEditMode", Keyboard.KEY_NONE, categoryName),
            showRecipe = new KeyBinding("key.jfmuy.showRecipe", Keyboard.KEY_R, categoryName),
            showUses = new KeyBinding("key.jfmuy.showUses", Keyboard.KEY_U, categoryName),
            recipeBack = new KeyBinding("key.jfmuy.recipeBack", Keyboard.KEY_BACK, categoryName),
            previousPage = new KeyBinding("key.jfmuy.previousPage", Keyboard.KEY_PRIOR, categoryName),
            nextPage = new KeyBinding("key.jfmuy.nextPage", Keyboard.KEY_NEXT, categoryName),
            bookmark = new KeyBinding("key.jfmuy.bookmark", Keyboard.KEY_A, categoryName),
            toggleBookmarkOverlay = new KeyBinding("key.jfmuy.toggleBookmarkOverlay", Keyboard.KEY_NONE, categoryName),
            crafting = new KeyBinding("key.jfmuy.crafting", Keyboard.KEY_C, categoryName));
    }

    public static void init() {
        for (KeyBinding binding : allBindings) {
            ClientRegistry.registerKeyBinding(binding);
        }
    }

    public static boolean isInventoryToggleKey(int keyCode) {
        return Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() == keyCode;
    }

    public static boolean isInventoryCloseKey(int keyCode) {
        return keyCode == Keyboard.KEY_ESCAPE;
    }

    public static boolean isEnterKey(int keyCode) {
        return keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER;
    }
}
