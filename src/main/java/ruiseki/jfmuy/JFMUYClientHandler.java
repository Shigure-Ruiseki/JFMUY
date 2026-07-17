package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.ServerInfo;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.input.MouseHelper;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.vanilla.VanillaPlugin;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeValidatorRegistry;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.startup.AnnotatedInstanceUtil;
import ruiseki.jfmuy.startup.JFMUYStarter;
import ruiseki.jfmuy.startup.PlayerJoinedWorldEvent;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.event.recipes.RecipesUpdatedEvent;
import ruiseki.okcore.helper.MinecraftHelpers;

@SideOnly(Side.CLIENT)
public class JFMUYClientHandler {

    public static final JFMUYClientHandler INSTANCE = new JFMUYClientHandler();

    private List<IModPlugin> plugins = new ArrayList<>();
    private final JFMUYStarter starter = new JFMUYStarter();

    private JFMUYClientHandler() {}

    public void preInit(FMLPreInitializationEvent event) {
        Config.preInit(event);

        ASMDataTable asmDataTable = event.getAsmData();
        this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

        IModPlugin vanillaPlugin = getVanillaPlugin(this.plugins);
        if (vanillaPlugin != null) {
            this.plugins.remove(vanillaPlugin);
            this.plugins.addFirst(vanillaPlugin);
        }

        IModPlugin jfmuyInternalPlugin = getJFMUYInternalPlugin(this.plugins);
        if (jfmuyInternalPlugin != null) {
            this.plugins.remove(jfmuyInternalPlugin);
            this.plugins.add(jfmuyInternalPlugin);
        }

        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(MouseHelper.INSTANCE);
    }

    @Nullable
    private IModPlugin getVanillaPlugin(List<IModPlugin> modPlugins) {
        for (IModPlugin modPlugin : modPlugins) {
            if (modPlugin instanceof VanillaPlugin) {
                return modPlugin;
            }
        }
        return null;
    }

    @Nullable
    private IModPlugin getJFMUYInternalPlugin(List<IModPlugin> modPlugins) {
        for (IModPlugin modPlugin : modPlugins) {
            if (modPlugin instanceof JFMUYInternalPlugin) {
                return modPlugin;
            }
        }
        return null;
    }

    public void init(FMLInitializationEvent event) {
        CraftingRecipeValidatorRegistry.init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (MinecraftHelpers.isClientSide()) {
            Minecraft minecraft = Minecraft.getMinecraft();
            IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft
                .getResourceManager();
            reloadableResourceManager.registerReloadListener(resourceManager -> {
                if (this.starter.hasStarted()) {
                    if (Config.isDebugModeEnabled()) {
                        Log.get()
                            .info(
                                "Reloading JFMUY ingredient filter.",
                                new RuntimeException("Stack trace for debugging"));
                    } else {
                        Log.get()
                            .info("Reloading JFMUY ingredient filter.");
                    }
                    // force search tree to reload
                    Config.needToRebuildSearchTree = true;
                    reloadItemList();
                }
                Translator.invalidateLocale();
            });
            this.starter.start(plugins);
        }
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal && !event.connectionType.equals("MODDED")) {
            ServerInfo.onConnectedToServer(false);
        }
        NetworkManager networkManager = event.manager;
        Config.syncWorldConfig(networkManager);
        MinecraftForge.EVENT_BUS.post(new PlayerJoinedWorldEvent());
        Internal.getIngredientFilter()
            .block();
    }

    private static void reloadItemList() {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime != null) {
            IngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
            ingredientListOverlay.rebuildItemFilter();
            ingredientListOverlay.invalidateBuffer();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (Reference.MOD_ID.equals(eventArgs.modID)) {
            if (Config.syncAllConfig()) {
                reloadItemList();
            }
        } else {
            if (this.starter.hasStarted()) {
                Config.checkForModNameFormatOverride();
            }
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        try {
            Config.saveFilterText();
        } catch (RuntimeException e) {
            Log.get()
                .error("Failed to save filter text.", e);
        }
    }

    @SubscribeEvent
    public void onUpdatedRecipe(RecipesUpdatedEvent event) {
        if (this.starter.hasStarted()) {
            Log.get()
                .info("Recipes updated from server. Reloading JFMUY...");
            this.starter.start(this.plugins);
        }
    }
}
