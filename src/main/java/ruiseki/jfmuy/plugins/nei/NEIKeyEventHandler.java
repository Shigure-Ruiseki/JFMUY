package ruiseki.jfmuy.plugins.nei;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import codechicken.nei.KeyManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.jfmuy.config.Config;

public class NEIKeyEventHandler {

    private static final String[] NEI_KEYS = new String[] {
        // GUI Keys
        "gui.enchant", "gui.potion", "gui.prev", "gui.next", "gui.hide", "gui.search", "gui.overlay", "gui.craft_items",
        "gui.getprevioussearch", "gui.getnextsearch", "gui.next_tooltip",

        // Recipe Keys
        "recipe.recipe", "recipe.usage", "recipe.back", "recipe.prev_machine", "recipe.next_machine",
        "recipe.prev_recipe", "recipe.next_recipe",

        // Bookmark Keys
        "bookmark.add", "bookmark.favorite", "bookmark.favorite_item", "bookmark.remove_recipe", "bookmark.pull_items",
        "bookmark.chat_link", "bookmark.hide",

        // Item Zoom Keys
        "itemzoom.toggle", "itemzoom.hold", "itemzoom.zoom_in", "itemzoom.zoom_out",

        // World / Utility Keys
        "world.chunkoverlay", "world.moboverlay", "world.highlight_tips", "world.dawn", "world.noon", "world.dusk",
        "world.midnight", "world.rain", "world.heal", "world.creative",

        // Copy Utility Keys
        "copy.name", "copy.oredict", "copy.identifier" };

    private final Map<String, Integer> originalKeyCodes = new HashMap<>();
    private boolean isBlocked = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        boolean shouldBlock = Config.isOverlayEnabled();

        if (shouldBlock && !isBlocked) {
            isBlocked = true;
            for (String keyId : NEI_KEYS) {
                KeyBinding binding = KeyManager.getKeyBinding(keyId);
                if (binding != null) {
                    int keyCode = binding.getKeyCode();
                    if (keyCode != Keyboard.KEY_NONE) {
                        originalKeyCodes.putIfAbsent(keyId, keyCode);
                        binding.setKeyCode(Keyboard.KEY_NONE);
                    }
                }
            }
        } else if (!shouldBlock && isBlocked) {
            isBlocked = false;
            for (Map.Entry<String, Integer> entry : originalKeyCodes.entrySet()) {
                KeyBinding binding = KeyManager.getKeyBinding(entry.getKey());
                if (binding != null) {
                    binding.setKeyCode(entry.getValue());
                }
            }
            originalKeyCodes.clear();
        }
    }
}
