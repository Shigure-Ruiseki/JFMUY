package ruiseki.jfmuy.config;

import javax.annotation.Nonnull;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import ruiseki.jfmuy.Reference;

public class KeyBindings {

    private static final String categoryName = Reference.MOD_ID + " (" + Reference.MOD_NAME + ')';

    @Nonnull
    public static final KeyBinding toggleOverlay = new KeyBinding(
        "key.jfmuy.toggleOverlay",
        Keyboard.KEY_O,
        categoryName);
    @Nonnull
    public static final KeyBinding showRecipe = new KeyBinding("key.jfmuy.showRecipe", Keyboard.KEY_R, categoryName);
    @Nonnull
    public static final KeyBinding showUses = new KeyBinding("key.jfmuy.showUses", Keyboard.KEY_U, categoryName);

    public static void init() {
        ClientRegistry.registerKeyBinding(toggleOverlay);
        ClientRegistry.registerKeyBinding(showRecipe);
        ClientRegistry.registerKeyBinding(showUses);
    }
}
