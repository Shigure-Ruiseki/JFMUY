package ruiseki.jfmuy.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.gui.RecipesGui;
import ruiseki.jfmuy.util.Translator;

public class JFMUYModConfigGui extends GuiConfig {

    public JFMUYModConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(), Reference.MOD_ID, false, false, getTitle(parent));
    }

    /** Don't return to a RecipesGui, it will not be valid after configs are changed. */
    private static GuiScreen getParent(GuiScreen parent) {
        if (parent instanceof RecipesGui) {
            return ((RecipesGui) parent).getParentScreen();
        }
        return parent;
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> configElements = new ArrayList<>();

        if (Minecraft.getMinecraft().theWorld != null) {
            Configuration worldConfig = Config.getWorldConfig();
            if (worldConfig != null) {
                ConfigCategory categoryWorldConfig = worldConfig.getCategory(SessionData.getWorldUid());
                configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
            }
        }

        ConfigCategory categoryAdvanced = Config.getConfig()
            .getCategory(Config.CATEGORY_ADVANCED);
        configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());

        ConfigCategory categorySearch = Config.getConfig()
            .getCategory(Config.CATEGORY_SEARCH);
        configElements.add(new ConfigElement(categorySearch));

        return configElements;
    }

    private static String getTitle(GuiScreen parent) {
        if (parent instanceof GuiModList) {
            return GuiConfig.getAbridgedConfigPath(
                Config.getConfig()
                    .toString());
        }
        return Translator.translateToLocal("config.jfmuy.title")
            .replace("%MODNAME", Reference.MOD_NAME);
    }
}
