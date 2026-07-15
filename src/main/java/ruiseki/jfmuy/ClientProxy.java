package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.config.ServerInfo;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.textures.Textures;
import ruiseki.jfmuy.input.MouseHelper;
import ruiseki.jfmuy.network.PacketHandler;
import ruiseki.jfmuy.network.PacketHandlerClient;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;
import ruiseki.jfmuy.plugins.vanilla.VanillaPlugin;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.startup.AnnotatedInstanceUtil;
import ruiseki.jfmuy.startup.JFMUYStarter;
import ruiseki.jfmuy.startup.PlayerJoinedWorldEvent;
import ruiseki.jfmuy.util.Log;

public class ClientProxy extends CommonProxy {

    private List<IModPlugin> plugins = new ArrayList<>();
    private final JFMUYStarter starter = new JFMUYStarter();
    @Nullable
    private Textures textures;

    private static void initVersionChecker() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString("curseProjectName", "jfmuy");
        compound.setString("curseFilenameParser", "jfmuy-[].jar");
        FMLInterModComms.sendRuntimeMessage(Reference.MOD_ID, "VersionChecker", "addCurseCheck", compound);
    }

    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        PacketHandlerClient packetHandler = new PacketHandlerClient();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketHandler.CHANNEL_ID);
        channel.register(packetHandler);

        Config.preInit(event);
        initVersionChecker();

        ASMDataTable asmDataTable = event.getAsmData();
        this.plugins = AnnotatedInstanceUtil.getModPlugins(asmDataTable);

        IModPlugin vanillaPlugin = getVanillaPlugin(this.plugins);
        if (vanillaPlugin != null) {
            this.plugins.remove(vanillaPlugin);
            this.plugins.add(0, vanillaPlugin);
        }

        IModPlugin jeiInternalPlugin = getJFMUYInternalPlugin(this.plugins);
        if (jeiInternalPlugin != null) {
            this.plugins.remove(jeiInternalPlugin);
            this.plugins.add(jeiInternalPlugin);
        }

        MinecraftForge.EVENT_BUS.register(this);
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
        MinecraftForge.EVENT_BUS.register(MouseHelper.INSTANCE);
    }

    @Override
    public void postInit(@Nonnull FMLPostInitializationEvent event) {
        // Reload when resources change
        Minecraft minecraft = Minecraft.getMinecraft();
        IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft
            .getResourceManager();
        reloadableResourceManager.registerReloadListener(resourceManager -> {
            if (this.starter.hasStarted()) {
                if (Config.isDebugModeEnabled()) {
                    Log.get()
                        .info("Restarting JEI.", new RuntimeException("Stack trace for debugging"));
                } else {
                    Log.get()
                        .info("Restarting JEI.");
                }
                this.starter.start(this.plugins);
            }
        });
        this.starter.start(plugins);
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal && !event.connectionType.equals("MODDED")) {
            ServerInfo.onConnectedToServer(false);
        }
        NetworkManager networkManager = event.manager;
        Config.syncWorldConfig(networkManager);
        MinecraftForge.EVENT_BUS.post(new PlayerJoinedWorldEvent());
    }

    private static void reloadItemList() {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime != null) {
            IngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
            ingredientListOverlay.rebuildItemFilter();
        }
    }

    @Override
    public void sendPacketToServer(PacketJFMUY packet) {
        NetHandlerPlayClient netHandler = FMLClientHandler.instance()
            .getClient()
            .getNetHandler();
        if (netHandler != null && ServerInfo.isJFMUYOnServer()) {
            netHandler.addToSendQueue(packet.getPacket());
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (Reference.MOD_ID.equals(eventArgs.modID)) {
            if (Config.syncAllConfig()) {
                reloadItemList();
            }
        } else {
            if (starter.hasStarted()) {
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
}
