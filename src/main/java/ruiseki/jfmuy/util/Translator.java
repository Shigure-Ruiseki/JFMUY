package ruiseki.jfmuy.util;

import java.util.IllegalFormatException;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.LocaleUtils;
import org.jetbrains.annotations.Nullable;

public class Translator {

    @Nullable
    private static Locale locale;

    private Translator() {

    }

    public static String translateToLocal(String key) {
        if (StatCollector.canTranslate(key)) {
            return StatCollector.translateToLocal(key);
        } else {
            return StatCollector.translateToFallback(key);
        }
    }

    public static void invalidateLocale() {
        locale = null;
    }

    public static String translateToLocalFormatted(String key, Object... format) {
        String s = translateToLocal(key);
        try {
            return String.format(s, format);
        } catch (IllegalFormatException e) {
            Log.get()
                .error("Format error: {}", s, e);
            return "Format error: " + s;
        }
    }

    public static String toLowercaseWithLocale(String string) {
        return string.toLowerCase(getLocale());
    }

    @SuppressWarnings("ConstantConditions")
    private static Locale getLocale() {
        if (locale == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft != null) {
                LanguageManager languageManager = minecraft.getLanguageManager();
                if (languageManager != null) {
                    Language currentLanguage = languageManager.getCurrentLanguage();
                    if (currentLanguage != null) {
                        locale = LocaleUtils.toLocale(currentLanguage.getLanguageCode());
                        return locale;
                    }
                }
            }
            locale = Locale.getDefault();
        }
        return locale;
    }
}
