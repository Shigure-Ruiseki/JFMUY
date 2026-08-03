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
    public static final KeyBindingOK bookmarkNewGroup;
    public static final KeyBindingOK bookmarkNewGroupToTop;
    public static final KeyBindingOK toggleBookmarkOverlay;
    public static final KeyBindingOK crafting;
    public static final KeyBindingOK moveGroupUp;
    public static final KeyBindingOK moveGroupDown;

    private static final List<KeyBindingOK> allBindings;

    static {
        allBindings = ImmutableList.of(
            toggleOverlay = new KeyBindingOK(
                "key.jfmuy.toggleOverlay",
                KeyConflictContext.GUI,
                Keyboard.KEY_O,
                categoryName),
            focusSearch = new KeyBindingOK(
                "key.jfmuy.focusSearch",
                KeyConflictContext.GUI,
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
                Keyboard.KEY_T,
                categoryName),
            recipeBack = new KeyBindingOK(
                "key.jfmuy.recipeBack",
                KeyConflictContext.GUI,
                Keyboard.KEY_BACK,
                categoryName),
            previousPage = new KeyBindingOK(
                "key.jfmuy.previousPage",
                KeyConflictContext.GUI,
                Keyboard.KEY_PRIOR,
                categoryName),
            nextPage = new KeyBindingOK(
                "key.jfmuy.nextPage",
                KeyConflictContext.GUI,
                Keyboard.KEY_NEXT,
                categoryName),
            previousCategory = new KeyBindingOK(
                "key.jfmuy.previousCategory",
                KeyConflictContext.GUI,
                Keyboard.KEY_PRIOR,
                categoryName),
            nextCategory = new KeyBindingOK(
                "key.jfmuy.nextCategory",
                KeyConflictContext.GUI,
                Keyboard.KEY_NEXT,
                categoryName),
            bookmark = new KeyBindingOK("key.jfmuy.bookmark", KeyConflictContext.GUI, Keyboard.KEY_A, categoryName),
            bookmarkToTop = new KeyBindingOK(
                "key.jfmuy.bookmarkToTop",
                KeyConflictContext.GUI,
                Keyboard.KEY_NONE,
                categoryName),
            bookmarkNewGroup = new KeyBindingOK(
                "key.jfmuy.bookmarkNewGroup",
                KeyConflictContext.GUI,
                Keyboard.KEY_A,
                categoryName),
            bookmarkNewGroupToTop = new KeyBindingOK(
                "key.jfmuy.bookmarkNewGroupToTop",
                KeyConflictContext.GUI,
                Keyboard.KEY_NONE,
                categoryName),
            toggleBookmarkOverlay = new KeyBindingOK(
                "key.jfmuy.toggleBookmarkOverlay",
                KeyConflictContext.GUI,
                Keyboard.KEY_NONE,
                categoryName),
            crafting = new KeyBindingOK("key.jfmuy.crafting", KeyConflictContext.GUI, Keyboard.KEY_C, categoryName),
            moveGroupUp = new KeyBindingOK(
                "key.jfmuy.moveGroupUp",
                KeyConflictContext.GUI,
                Keyboard.KEY_UP,
                categoryName),
            moveGroupDown = new KeyBindingOK(
                "key.jfmuy.moveGroupDown",
                KeyConflictContext.GUI,
                Keyboard.KEY_DOWN,
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
