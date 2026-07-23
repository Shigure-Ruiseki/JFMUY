package ruiseki.jfmuy.config;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.registry.ClientRegistry;
import ruiseki.jfmuy.Reference;
import ruiseki.okcore.client.key.KeyBindingOK;
import ruiseki.okcore.client.key.KeyConflictContext;
import ruiseki.okcore.client.key.KeyModifier;

public class KeyBindings {

    private static final String categoryName = Reference.MOD_ID + " (" + Reference.MOD_NAME + ')';

    public static final KeyBindingOK toggleOverlay;
    public static final KeyBindingOK focusSearch;
    public static final KeyBindingOK toggleCheatMode;
    public static final KeyBindingOK toggleEditMode;
    public static final KeyBindingOK showRecipe;
    public static final KeyBindingOK showUses;
    public static final KeyBindingOK showRecipeTree;
    public static final KeyBindingOK recipeBack;
    public static final KeyBindingOK previousPage;
    public static final KeyBindingOK nextPage;
    public static final KeyBindingOK previousCategory;
    public static final KeyBindingOK nextCategory;
    public static final KeyBindingOK bookmark;
    public static final KeyBindingOK bookmarkToTop;
    public static final KeyBindingOK recipeBookmark;
    public static final KeyBindingOK toggleBookmarkOverlay;
    public static final KeyBindingOK crafting;
    private static final List<KeyBindingOK> allBindings;

    static {
        allBindings = ImmutableList.of(
            toggleOverlay = new KeyBindingOK(
                "key.jfmuy.toggleOverlay",
                KeyConflictContext.GUI,
                KeyModifier.CONTROL,
                Keyboard.KEY_O,
                categoryName),
            focusSearch = new KeyBindingOK(
                "key.jfmuy.focusSearch",
                KeyConflictContext.GUI,
                KeyModifier.CONTROL,
                Keyboard.KEY_F,
                categoryName),
            toggleCheatMode = new KeyBindingOK(
                "key.jfmuy.toggleCheatMode",
                KeyConflictContext.GUI,
                Keyboard.KEY_NONE,
                categoryName),
            toggleEditMode = new KeyBindingOK(
                "key.jfmuy.toggleEditMode",
                KeyConflictContext.GUI,
                Keyboard.KEY_NONE,
                categoryName),
            showRecipe = new KeyBindingOK("key.jfmuy.showRecipe", KeyConflictContext.GUI, Keyboard.KEY_R, categoryName),
            showUses = new KeyBindingOK("key.jfmuy.showUses", KeyConflictContext.GUI, Keyboard.KEY_U, categoryName),
            showRecipeTree = new KeyBindingOK(
                "key.jfmuy.showRecipeTree",
                KeyConflictContext.GUI,
                KeyModifier.SHIFT,
                Keyboard.KEY_T,
                categoryName),
            recipeBack = new KeyBindingOK(
                "key.jfmuy.recipeBack",
                KeyConflictContext.GUI,
                Keyboard.KEY_BACK,
                categoryName),
            previousPage = new KeyBindingOK(
                "key.jfmuy.previousCategory",
                KeyConflictContext.GUI,
                Keyboard.KEY_PRIOR,
                categoryName),
            nextPage = new KeyBindingOK(
                "key.jfmuy.nextCategory",
                KeyConflictContext.GUI,
                Keyboard.KEY_NEXT,
                categoryName),
            previousCategory = new KeyBindingOK(
                "key.jfmuy.previousCategory",
                KeyConflictContext.GUI,
                KeyModifier.SHIFT,
                Keyboard.KEY_PRIOR,
                categoryName),
            nextCategory = new KeyBindingOK(
                "key.jfmuy.nextCategory",
                KeyConflictContext.GUI,
                KeyModifier.SHIFT,
                Keyboard.KEY_NEXT,
                categoryName),
            bookmark = new KeyBindingOK(
                "key.jfmuy.bookmark",
                KeyConflictContext.GUI,
                KeyModifier.NONE,
                Keyboard.KEY_A,
                categoryName),
            bookmarkToTop = new KeyBindingOK(
                "key.jfmuy.bookmarkToTop",
                KeyConflictContext.GUI,
                KeyModifier.SHIFT,
                Keyboard.KEY_A,
                categoryName),
            recipeBookmark = new KeyBindingOK(
                "key.jfmuy.recipeBookmark",
                KeyConflictContext.GUI,
                KeyModifier.CONTROL,
                Keyboard.KEY_A,
                categoryName),
            toggleBookmarkOverlay = new KeyBindingOK(
                "key.jfmuy.toggleBookmarkOverlay",
                KeyConflictContext.GUI,
                Keyboard.KEY_NONE,
                categoryName),
            crafting = new KeyBindingOK(
                "key.jfmuy.crafting",
                KeyConflictContext.GUI,
                KeyModifier.SHIFT,
                Keyboard.KEY_C,
                categoryName));
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

    public static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static boolean isAltDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }
}
