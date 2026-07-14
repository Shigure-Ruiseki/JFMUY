package ruiseki.jfmuy;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Joiner;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.util.color.ColorNamer;

public class IngredientInformation {

    @Nullable
    private static Language previousLanguage;

    private static Map<Object, String> tooltipCache = new HashMap<Object, String>();
    private static Map<Object, String> colorCache = new HashMap<>();

    public static <T> String getTooltipString(T ingredient, IIngredientRenderer<T> ingredientRenderer, String modId,
        String modName, String displayName) {
        String tooltipString = tooltipCache.get(ingredient);
        if (tooltipString == null) {
            List<String> tooltip = ingredientRenderer.getTooltip(Minecraft.getMinecraft(), ingredient);
            tooltipString = Joiner.on(' ')
                .join(tooltip)
                .toLowerCase();
            tooltipString = removeChatFormatting(tooltipString);
            tooltipString = tooltipString.replace(modId, "");
            tooltipString = tooltipString.replace(modName, "");
            tooltipString = tooltipString.replace(displayName, "");
            tooltipCache.put(ingredient, tooltipString);
        }

        return tooltipString;
    }

    private static String removeChatFormatting(String string) {
        String withoutFormattingCodes = EnumChatFormatting.getTextWithoutFormattingCodes(string);
        return (withoutFormattingCodes == null) ? string : withoutFormattingCodes;
    }

    public static <V> String getColorString(V ingredient, IIngredientHelper<V> ingredientHelper) {
        String colorString = colorCache.get(ingredient);
        if (colorString == null) {
            Iterable<Color> colors = ingredientHelper.getColors(ingredient);
            ColorNamer colorNamer = Internal.getColorNamer();
            Collection<String> colorNames = colorNamer.getColorNames(colors);
            colorString = Joiner.on(' ')
                .join(colorNames)
                .toLowerCase();
            colorCache.put(ingredient, colorString);
        }
        return colorString;
    }

    public static void onStart(boolean resourceReload) {
        Language language = Minecraft.getMinecraft()
            .getLanguageManager()
            .getCurrentLanguage();
        if (previousLanguage != null && !previousLanguage.equals(language)) {
            tooltipCache.clear();
        }
        previousLanguage = language;

        if (resourceReload) {
            colorCache.clear();
        }
    }
}
