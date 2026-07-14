package ruiseki.jfmuy.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.network.packets.PacketRequestCheatPermission;
import ruiseki.jfmuy.util.Translator;

public class JFMUYModConfigGui extends GuiConfig {

    public JFMUYModConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(), Reference.MOD_ID, false, false, getTitle(parent));
    }

    /**
     * Don't return to a RecipesGui, it will not be valid after configs are changed.
     */
    private static GuiScreen getParent(GuiScreen parent) {
        if (parent instanceof RecipesGui) {
            GuiScreen parentScreen = ((RecipesGui) parent).getParentScreen();
            if (parentScreen != null) {
                return parentScreen;
            } else {
                return new GuiInventory(parent.mc.thePlayer);
            }
        }
        return parent;
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> configElements = new ArrayList<IConfigElement>();

        if (Minecraft.getMinecraft().theWorld != null) {
            Configuration worldConfig = Config.getWorldConfig();
            if (worldConfig != null) {
                ConfigCategory categoryWorldConfig = worldConfig.getCategory(SessionData.getWorldUid());
                configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
            }
        }

        LocalizedConfiguration config = Config.getConfig();
        if (config != null) {
            ConfigCategory categoryAdvanced = config.getCategory(Config.CATEGORY_ADVANCED);
            configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());

            ConfigCategory categorySearch = config.getCategory(Config.CATEGORY_SEARCH);
            configElements.add(new ConfigElement(categorySearch));
        }

        return configElements;
    }

    private static String getTitle(GuiScreen parent) {
        if (parent instanceof GuiModList) {
            LocalizedConfiguration config = Config.getConfig();
            if (config != null) {
                return GuiConfig.getAbridgedConfigPath(config.toString());
            }
        }
        return Translator.translateToLocal("config.jfmuy.title")
            .replace("%MODNAME", Reference.MOD_NAME);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if (Config.isCheatItemsEnabled() && SessionData.isJfmuyOnServer()) {
            JFMUY.getProxy()
                .sendPacketToServer(new PacketRequestCheatPermission());
        }
    }
}
