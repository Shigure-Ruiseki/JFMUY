package ruiseki.jfmuy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiEditArrayEntries;
import cpw.mods.fml.client.config.IConfigElement;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.gui.overlay.collapsible.GuiCollapsibleGroups;
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

    private static void addCollapsibleElements(List<IConfigElement> configElements, LocalizedConfiguration config) {
        // Show collapsibleGroupsEnabled inline (aligned like other boolean properties)
        ConfigCategory catCollapsible = config.getCategory(Config.CATEGORY_COLLAPSIBLE);
        for (Object element : new ConfigElement(catCollapsible).getChildElements()) {
            if (element instanceof IConfigElement<?>configElement) {
                if (configElement.showInGui()) {
                    configElements.add(configElement);
                }
            }
        }
        // "Manage Groups" navigation entry — opens GuiCollapsibleGroups in the same visual row style
        configElements.add(new ManageGroupsConfigElement());
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> configElements = new ArrayList<>();

        LocalizedConfiguration config = Config.getConfig();

        if (Minecraft.getMinecraft().theWorld != null) {
            Configuration worldConfig = Config.getWorldConfig();
            if (worldConfig != null) {
                NetworkManager networkManager = FMLClientHandler.instance()
                    .getClientToServerNetworkManager();
                ConfigCategory categoryWorldConfig = worldConfig.getCategory(ServerInfo.getWorldUid(networkManager));
                List<IConfigElement> worldElements = new ConfigElement(categoryWorldConfig).getChildElements();

                // Find the "Hide Ingredients Mode" entry and insert Collapsible Groups submenu immediately after it
                int insertAt = worldElements.size();
                for (int i = 0; i < worldElements.size(); i++) {
                    if ("config.jfmuy.mode.editEnabled".equals(
                        worldElements.get(i)
                            .getLanguageKey())) {
                        insertAt = i + 1;
                        break;
                    }
                }
                configElements.addAll(worldElements.subList(0, insertAt));
                if (config != null) {
                    addCollapsibleElements(configElements, config);
                }
                configElements.addAll(worldElements.subList(insertAt, worldElements.size()));
            }
        }

        if (config != null) {
            ConfigCategory categoryAdvanced = config.getCategory(Config.CATEGORY_ADVANCED);
            configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());

            ConfigCategory categoryRendering = config.getCategory(Config.CATEGORY_RENDERING);
            configElements.addAll(new ConfigElement(categoryRendering).getChildElements());

            ConfigCategory categoryMisc = config.getCategory(Config.CATEGORY_MISC);
            configElements.addAll(new ConfigElement(categoryMisc).getChildElements());

            // If we never had a world config section (world == null), show Collapsible Groups here instead
            if (Minecraft.getMinecraft().thePlayer == null) {
                addCollapsibleElements(configElements, config);
            }

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

    // -------------------------------------------------------------------------
    // "Manage Groups" config list entry
    // -------------------------------------------------------------------------

    /**
     * A CategoryEntry that opens GuiCollapsibleGroups instead of a standard GuiConfig subcategory.
     * Constructor signature must match (GuiConfig, GuiConfigEntries, IConfigElement).
     */
    public static class ManageGroupsEntry extends GuiConfigEntries.ButtonEntry {

        public ManageGroupsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
            IConfigElement configElement) {
            super(
                owningScreen,
                owningEntryList,
                configElement,
                new GuiButtonExt(
                    0,
                    owningEntryList.controlX,
                    0,
                    owningEntryList.controlWidth,
                    18,
                    Translator.toLowercaseWithLocale("jfmuy.gui.collapsible.title")));
        }

        @Override
        public void updateValueButtonText() {}

        @Override
        public void valueButtonPressed(int slotIndex) {
            this.mc.displayGuiScreen(new GuiCollapsibleGroups(this.owningScreen));
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public void setToDefault() {}

        @Override
        public boolean isChanged() {
            return false;
        }

        @Override
        public void undoChanges() {}

        @Override
        public boolean saveConfigElement() {
            return false;
        }

        @Override
        public Object getCurrentValue() {
            return "";
        }

        @Override
        public Object[] getCurrentValues() {
            return new Object[] { "" };
        }
    }

    /**
     * A minimal IConfigElement that represents a category-type navigation entry
     * pointing to GuiCollapsibleGroups via ManageGroupsEntry.
     */
    public static class ManageGroupsConfigElement implements IConfigElement {

        @Override
        public boolean isProperty() {
            return false;
        }

        @Override
        public Class<? extends GuiConfigEntries.IConfigEntry> getConfigEntryClass() {
            return ManageGroupsEntry.class;
        }

        @Override
        public Class<? extends GuiEditArrayEntries.IArrayEntry> getArrayEntryClass() {
            return null;
        }

        @Override
        public String getName() {
            return "manageGroups";
        }

        @Override
        public String getQualifiedName() {
            return "manageGroups";
        }

        @Override
        public String getLanguageKey() {
            return "jfmuy.gui.collapsible.title";
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public List<IConfigElement> getChildElements() {
            return Collections.emptyList();
        }

        @Override
        public ConfigGuiType getType() {
            return ConfigGuiType.CONFIG_CATEGORY;
        }

        @Override
        public boolean isList() {
            return false;
        }

        @Override
        public boolean isListLengthFixed() {
            return false;
        }

        @Override
        public int getMaxListLength() {
            return -1;
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public Object getDefault() {
            return null;
        }

        @Override
        public Object[] getDefaults() {
            return null;
        }

        @Override
        public void setToDefault() {}

        @Override
        public boolean requiresWorldRestart() {
            return false;
        }

        @Override
        public boolean showInGui() {
            return true;
        }

        @Override
        public boolean requiresMcRestart() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object[] getList() {
            return null;
        }

        @Override
        public void set(Object value) {}

        @Override
        public void set(Object[] aVal) {}

        @Override
        public String[] getValidValues() {
            return null;
        }

        @Override
        public Object getMinValue() {
            return null;
        }

        @Override
        public Object getMaxValue() {
            return null;
        }

        @Override
        public Pattern getValidationPattern() {
            return null;
        }
    }
}
