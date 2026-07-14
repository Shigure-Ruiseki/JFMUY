package ruiseki.jfmuy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.config.SessionData;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.gui.RecipesGui;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.vanilla.VanillaPlugin;
import ruiseki.jfmuy.util.AnnotatedInstanceUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.ModRegistry;

public class ClientProxy extends CommonProxy {

    @Nullable
    private ItemFilter itemFilter;
    private List<IModPlugin> plugins;

    private static void initVersionChecker() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString("curseProjectName", "jfmuy");
        compound.setString("curseFilenameParser", "jfmuy-[].jar");
        FMLInterModComms.sendRuntimeMessage(Reference.MOD_ID, "VersionChecker", "addCurseCheck", compound);
    }

    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        Config.preInit(event);
        initVersionChecker();

        ASMDataTable asmDataTable = event.getAsmData();
        this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

        IModPlugin vanillaPlugin = getVanillaPlugin(this.plugins);
        if (vanillaPlugin != null) {
            this.plugins.remove(vanillaPlugin);
            this.plugins.add(0, vanillaPlugin);
        }

        IModPlugin jfmuyInternalPlugin = getJFMUYInternalPlugin(this.plugins);
        if (jfmuyInternalPlugin != null) {
            this.plugins.remove(jfmuyInternalPlugin);
            this.plugins.add(jfmuyInternalPlugin);
        }
    }

    @Nullable
    private IModPlugin getVanillaPlugin(@Nonnull List<IModPlugin> modPlugins) {
        for (IModPlugin modPlugin : modPlugins) {
            if (modPlugin instanceof VanillaPlugin) {
                return modPlugin;
            }
        }
        return null;
    }

    @Nullable
    private IModPlugin getJFMUYInternalPlugin(@Nonnull List<IModPlugin> modPlugins) {
        for (IModPlugin modPlugin : modPlugins) {
            if (modPlugin instanceof JFMUYInternalPlugin) {
                return modPlugin;
            }
        }
        return null;
    }

    @Override

    public void init(@Nonnull FMLInitializationEvent event) {
        KeyBindings.init();
        MinecraftForge.EVENT_BUS.register(this);

        GuiEventHandler guiEventHandler = new GuiEventHandler();
        MinecraftForge.EVENT_BUS.register(guiEventHandler);

        fixVanillaItemHasSubtypes();
    }

    @Override
    public void postInit(@Nonnull FMLPostInitializationEvent event) {
        // Reload when resources change
        Minecraft minecraft = Minecraft.getMinecraft();
        IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft
            .getResourceManager();
        reloadableResourceManager.registerReloadListener(new IResourceManagerReloadListener() {

            @Override
            public void onResourceManagerReload(IResourceManager resourceManager) {
                restartJFMUY();
            }
        });
    }

    /** fix vanilla items that don't mark themselves as having subtypes */
    private static void fixVanillaItemHasSubtypes() {
        List<Item> items = Arrays.asList(
            Items.potionitem,
            // Items.LINGERING_POTION,
            // Items.SPLASH_POTION,
            // Items.TIPPED_ARROW,
            Items.enchanted_book);
        for (Item item : items) {
            item.setHasSubtypes(true);
        }
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
        if (!SessionData.isJfmuyStarted() && Minecraft.getMinecraft().thePlayer != null) {
            try {
                startJFMUY();
            } catch (Throwable e) {
                Minecraft.getMinecraft()
                    .displayCrashReport(new CrashReport("JFMUY failed to start:", e));
            }
        }
    }

    private void startJFMUY() {
        SessionData.setJFMUYStarted();

        Config.startJFMUY();

        Internal.setHelpers(new JFMUYHelpers());
        Internal.getStackHelper()
            .enableUidCache();

        ItemRegistryFactory itemRegistryFactory = new ItemRegistryFactory();
        ItemRegistry itemRegistry = itemRegistryFactory.createItemRegistry();
        Internal.setItemRegistry(itemRegistry);

        ModRegistry modRegistry = new ModRegistry(Internal.getHelpers(), itemRegistry);

        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                long start_time = System.nanoTime();
                Log.info(
                    "Registering plugin: {}",
                    plugin.getClass()
                        .getName());
                plugin.register(modRegistry);
                long timeElapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start_time);
                Log.info(
                    "Registered  plugin: {} in {} seconds",
                    plugin.getClass()
                        .getName(),
                    timeElapsedSeconds);
            } catch (RuntimeException | LinkageError e) {
                Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }

        RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry();

        List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();

        itemFilter = new ItemFilter(itemRegistry);
        ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter, advancedGuiHandlers);
        RecipesGui recipesGui = new RecipesGui();

        JFMUYRuntime jfmuyRuntime = new JFMUYRuntime(recipeRegistry, itemListOverlay, recipesGui);
        Internal.setRuntime(jfmuyRuntime);

        iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                plugin.onRuntimeAvailable(jfmuyRuntime);
            } catch (RuntimeException | LinkageError e) {
                Log.error("Mod plugin failed: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }

        Internal.getStackHelper()
            .disableUidCache();
    }

    @Override
    public void restartJFMUY() {
        if (SessionData.isJfmuyStarted()) {
            startJFMUY();
        }
    }

    @Override
    public void resetItemFilter() {
        if (itemFilter != null) {
            itemFilter.reset();
        }
    }

    @Override
    public void sendPacketToServer(PacketJFMUY packet) {
        NetHandlerPlayClient netHandler = FMLClientHandler.instance()
            .getClient()
            .getNetHandler();
        if (netHandler != null) {
            netHandler.addToSendQueue(packet.getPacket());
        }
    }

    // subscribe to event with low priority so that addon mods that use the config can do their stuff first
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (!Reference.MOD_ID.equals(eventArgs.modID)) {
            return;
        }

        if (Config.syncConfig()) {
            restartJFMUY(); // reload everything, configs can change available recipes
        }
    }
}
