package ruiseki.jfmuy;

import java.util.Map;

import javax.annotation.Nonnull;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import ruiseki.jfmuy.config.ServerInfo;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    guiFactory = Reference.GUI_FACTORY,
    dependencies = Reference.DEPENDENCIES)
public class JFMUY {

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    private static CommonProxy proxy;

    public static CommonProxy getProxy() {
        return proxy;
    }

    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> modList, Side side) {
        if (side == Side.SERVER) {
            boolean JFMUYOnServer = modList.containsKey(Reference.MOD_ID);
            ServerInfo.onConnectedToServer(JFMUYOnServer);
        }
        return true;
    }

    @Mod.EventHandler
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(@Nonnull FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(@Nonnull FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
