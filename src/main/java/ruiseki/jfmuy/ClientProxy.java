package ruiseki.jfmuy;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.util.AnnotatedInstanceUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.ModRegistry;

public class ClientProxy extends CommonProxy {

    private static boolean started = false;
    @Nullable
    private ItemFilter itemFilter;
    private GuiEventHandler guiEventHandler;
    private List<IModPlugin> plugins;

    private static void initVersionChecker() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString("curseProjectName", "just-enough-items-jei");
        compound.setString("curseFilenameParser", "jfmuy-" + MinecraftForge.MC_VERSION + "-[].jar");
        FMLInterModComms.sendRuntimeMessage(Reference.MOD_ID, "VersionChecker", "addCurseCheck", compound);
    }

    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        Config.preInit(event);
        initVersionChecker();

        ASMDataTable asmDataTable = event.getAsmData();
        this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                plugin.onJFMUYHelpersAvailable(Internal.getHelpers());
            } catch (AbstractMethodError ignored) {
                // older plugins don't have this method
            } catch (Exception e) {
                Log.error("Mod plugin failed: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }
    }

    @Override
    public void init(@Nonnull FMLInitializationEvent event) {
        KeyBindings.init();
        FMLCommonHandler.instance()
            .bus()
            .register(this);

        guiEventHandler = new GuiEventHandler();
        MinecraftForge.EVENT_BUS.register(guiEventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(guiEventHandler);
    }

    @Override
    public void scheduleStartNEI() {
        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }
        // TODO: addScheduledTask
        // minecraft.addScheduledTask(new Runnable() {
        // @Override
        // public void run() {
        // JFMUY.getProxy().startNEI();
        // }
        // });
    }

    @Override
    public void startNEI() {
        if (started && Internal.getRecipeRegistry() != null) {
            return;
        }

        started = true;
        ItemRegistry itemRegistry = new ItemRegistry();
        Internal.setItemRegistry(itemRegistry);

        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                plugin.onItemRegistryAvailable(itemRegistry);
            } catch (AbstractMethodError ignored) {
                // older plugins don't have this method
            } catch (Exception e) {
                Log.error("Mod plugin failed: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }

        ModRegistry modRegistry = new ModRegistry();

        iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                plugin.register(modRegistry);
                Log.info(
                    "Registered plugin: {}",
                    plugin.getClass()
                        .getName());
            } catch (Exception e) {
                Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }

        RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry();
        Internal.setRecipeRegistry(recipeRegistry);

        iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                plugin.onRecipeRegistryAvailable(recipeRegistry);
            } catch (AbstractMethodError ignored) {
                // older plugins don't have this method
            } catch (Exception e) {
                Log.error("Mod plugin failed: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }

        itemFilter = new ItemFilter(itemRegistry);
        ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
        guiEventHandler.setItemListOverlay(itemListOverlay);
    }

    @Override
    public void restartNEI() {
        if (!started) {
            return;
        }
        started = false;
        startNEI();
    }

    @Override
    public void resetItemFilter() {
        if (itemFilter != null) {
            itemFilter.reset();
        }
    }

    @Override
    public void sendPacketToServer(PacketJFMUY packet) {
        FMLProxyPacket proxyPacket = packet.getPacket();
        proxyPacket.setTarget(Side.SERVER);
        JFMUY.getPacketHandler()
            .sendToServer(proxyPacket);
    }

    // subscribe to event with low priority so that addon mods that use the config can do their stuff first
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (!Reference.MOD_ID.equals(eventArgs.modID)) {
            return;
        }

        if (Config.syncConfig()) {
            restartNEI(); // reload everything, configs can change available recipes
        }
    }
}
