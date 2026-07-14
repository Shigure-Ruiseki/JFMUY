package ruiseki.jfmuy.config;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import ruiseki.jfmuy.Reference;

public class KeyBindings {

    private static final String categoryName = Reference.MOD_ID + " (" + Reference.MOD_NAME + ')';

    public static final KeyBinding toggleOverlay = new KeyBinding(
        "key.jfmuy.toggleOverlay",
        Keyboard.KEY_O,
        categoryName);
    public static final KeyBinding focusSearch = new KeyBinding("key.jfmuy.focusSearch", Keyboard.KEY_F, categoryName);
    public static final KeyBinding showRecipe = new KeyBinding("key.jfmuy.showRecipe", Keyboard.KEY_R, categoryName);
    public static final KeyBinding showUses = new KeyBinding("key.jfmuy.showUses", Keyboard.KEY_U, categoryName);
    public static final KeyBinding recipeBack = new KeyBinding("key.jfmuy.recipeBack", Keyboard.KEY_BACK, categoryName);
    public static final KeyBinding toggleCheatMode = new KeyBinding(
        "key.jfmuy.toggleCheatMode",
        Keyboard.KEY_NONE,
        categoryName);

    public static void init() {
        ClientRegistry.registerKeyBinding(toggleOverlay);
        ClientRegistry.registerKeyBinding(focusSearch);
        ClientRegistry.registerKeyBinding(showRecipe);
        ClientRegistry.registerKeyBinding(showUses);
        ClientRegistry.registerKeyBinding(recipeBack);
        ClientRegistry.registerKeyBinding(toggleCheatMode);
    }
}
