package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.text.WordUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.config.Config;

public class ModIdUtil {

    private final Map<String, String> modNamesForIds = new HashMap<>();

    public ModIdUtil() {
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

    @Deprecated
    public String getModNameForItem(Item item) {
        ResourceLocation itemResourceLocation = new ResourceLocation(
            GameData.getItemRegistry()
                .getNameForObject(item));
        if (itemResourceLocation == null) {
            String stackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
            throw new NullPointerException("item.getRegistryName() returned null for: " + stackInfo);
        }
        String modId = itemResourceLocation.getResourceDomain();
        return getModNameForModId(modId);
    }

    public String getModNameForModId(String modId) {
        String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
        String modName = modNamesForIds.get(lowercaseModId);
        if (modName == null) {
            modName = WordUtils.capitalize(modId);
            modNamesForIds.put(lowercaseModId, modName);
        }
        return modName;
    }

    public <T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper) {
        String modId = ingredientHelper.getModId(ingredient);
        return getModNameForModId(modId);
    }

    public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient,
        IIngredientHelper<T> ingredientHelper) {
        String modNameFormat = Config.getModNameFormat();
        if (modNameFormat.isEmpty()) {
            return tooltip;
        }

        String modName = getModNameForIngredient(ingredient, ingredientHelper);
        if (tooltip.size() > 1) {
            String lastTooltipLine = tooltip.get(tooltip.size() - 1);
            lastTooltipLine = EnumChatFormatting.getTextWithoutFormattingCodes(lastTooltipLine);
            if (modName.equals(lastTooltipLine)) {
                return tooltip;
            }
        }

        List<String> tooltipCopy = new ArrayList<>(tooltip);
        tooltipCopy.add(modNameFormat + modName);
        return tooltipCopy;
    }
}
