package ruiseki.jfmuy.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;

import org.apache.commons.lang3.text.WordUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModList {

    private final Map<String, String> modNamesForIds = new HashMap<>();

    public ModList() {
        Map<String, ModContainer> modMap = Loader.instance()
            .getIndexedModList();
        for (Map.Entry<String, ModContainer> modEntry : modMap.entrySet()) {
            String lowercaseId = modEntry.getKey()
                .toLowerCase(Locale.ENGLISH);
            String modName = modEntry.getValue()
                .getName();
            modNamesForIds.put(lowercaseId, modName);
        }
    }

    @Nonnull
    public String getModNameForItem(@Nonnull Item item) {
        GameRegistry.UniqueIdentifier uniqueIdentifier = GameRegistry.findUniqueIdentifierFor(item);
        if (uniqueIdentifier == null) {
            return "";
        }
        String modId = uniqueIdentifier.modId;
        String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
        String modName = modNamesForIds.get(lowercaseModId);
        if (modName == null) {
            modName = WordUtils.capitalize(modId);
            modNamesForIds.put(lowercaseModId, modName);
        }
        return modName;
    }
}
