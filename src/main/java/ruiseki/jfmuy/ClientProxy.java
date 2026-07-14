package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.config.SessionData;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.vanilla.VanillaPlugin;
import ruiseki.jfmuy.util.AnnotatedInstanceUtil;
import ruiseki.jfmuy.util.Log;

public class ClientProxy extends CommonProxy {

    private List<IModPlugin> plugins = new ArrayList<>();
    private final JFMUYStarter starter = new JFMUYStarter();

    private static void initVersionChecker() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString("curseProjectName", "jfmuy");
        compound.setString("curseFilenameParser", "jfmuy-[].jar");
        FMLInterModComms.sendRuntimeMessage(Reference.MOD_ID, "VersionChecker", "addCurseCheck", compound);
    }

    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        super.preInit(event);
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
        try {
            this.starter.start(plugins, false);
        } catch (Exception e) {
            Log.error("Exception on load", e);
        }
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote && !SessionData.hasJoinedWorld() && Minecraft.getMinecraft().thePlayer != null) {
            SessionData.setJoinedWorld();
            Config.syncWorldConfig();
        }
    }

    @Override
    public void restartJFMUY() {
        // Log.warning(
        // "Restarting JFMUY. Warning: This feature will be removed soon, please see the JavaDocs for more
        // information.",
        // new RuntimeException());
        restartJFMUY(false);
    }

    private void restartJFMUY(final boolean resourceReload) {
        if (Thread.currentThread()
            .getName()
            .equals("Client thread")) {
            if (this.starter.hasStarted()) {
                this.starter.start(this.plugins, resourceReload);
            }
        } else {
            Log.error("A mod is trying to restart JFMUY from the wrong thread!", new RuntimeException());

            FMLCommonHandler.instance()
                .bus()
                .register(new Object() {

                    @SubscribeEvent
                    public void onClientTick(TickEvent.ClientTickEvent event) {
                        if (event.phase == TickEvent.Phase.END) {
                            restartJFMUY(resourceReload);
                            FMLCommonHandler.instance()
                                .bus()
                                .unregister(this);
                        }
                    }
                });
        }
    }

    private static void reloadItemList() {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime != null) {
            ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
            itemListOverlay.rebuildItemFilter();
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

        if (Config.syncAllConfig()) {
            reloadItemList(); // reload everything, configs can change available recipes
        }
    }
}
