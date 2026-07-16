package ruiseki.jfmuy.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.network.PacketRequestCheatPermission;
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
                Minecraft minecraft = parent.mc;
                if (minecraft != null) {
                    EntityPlayerSP player = minecraft.thePlayer;
                    if (player != null) {
                        return new GuiInventory(player);
                    }
                }
            }
        }
        return parent;
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> configElements = new ArrayList<>();

        if (Minecraft.getMinecraft().theWorld != null) {
            Configuration worldConfig = Config.getWorldConfig();
            if (worldConfig != null) {
                NetworkManager networkManager = FMLClientHandler.instance()
                    .getClientToServerNetworkManager();
                ConfigCategory categoryWorldConfig = worldConfig.getCategory(ServerInfo.getWorldUid(networkManager));
                configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
            }
        }

        LocalizedConfiguration config = Config.getConfig();
        if (config != null) {
            ConfigCategory categoryAdvanced = config.getCategory(Config.CATEGORY_ADVANCED);
            configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());

            ConfigCategory categoryRendering = config.getCategory(Config.CATEGORY_RENDERING);
            configElements.addAll(new ConfigElement(categoryRendering).getChildElements());

            ConfigCategory categoryMisc = config.getCategory(Config.CATEGORY_MISC);
            configElements.addAll(new ConfigElement(categoryMisc).getChildElements());

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

        if (Config.isCheatItemsEnabled() && ServerInfo.isJFMUYOnServer()) {
            JFMUY.instance.getPacketHandler()
                .sendToServer(new PacketRequestCheatPermission());
        }
    }
}
