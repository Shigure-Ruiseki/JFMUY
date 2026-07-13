package ruiseki.jfmuy;

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.debug.DebugItem;
import ruiseki.jfmuy.network.PacketHandler;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    guiFactory = Reference.GUI_FACTORY,
    dependencies = "required-after:Forge@[10.13.0.0,);")
public class JFMUY {

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    private static CommonProxy proxy;
    private static PacketHandler packetHandler;

    public static PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public static CommonProxy getProxy() {
        return proxy;
    }

    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> modList, Side side) {
        boolean jfmuyOnServer = modList.containsKey(Reference.MOD_ID);
        if (side == Side.SERVER) {
            Config.setJfmuyOnServer(jfmuyOnServer);
        } else if (side == Side.CLIENT && jfmuyOnServer) {
            // Integrated server / LAN: recipe transfer needs this on the client too
            Config.setJfmuyOnServer(true);
        }

        return true;
    }

    @Mod.EventHandler
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        packetHandler = new PacketHandler();
        Internal.setHelpers(new JFMUYHelpers());
        proxy.preInit(event);

        if (Config.isDebugModeEnabled()) {
            String name = "jfmuyDebug";
            Item debugItem = new DebugItem(name);
            debugItem.setUnlocalizedName(name);
            GameRegistry.registerItem(debugItem, name);
        }
    }

    @Mod.EventHandler
    public void init(@Nonnull FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void startNEI(@Nonnull FMLModIdMappingEvent event) {
        // FMLModIdMappingEvent is delivered on the server thread during integrated load.
        // Item/recipe registration must run on the client thread (GUI + client registries).
        if (FMLCommonHandler.instance()
            .getSide() == Side.CLIENT) {
            proxy.startNEI();
            return;
        }

        proxy.scheduleStartNEI();
    }
}
