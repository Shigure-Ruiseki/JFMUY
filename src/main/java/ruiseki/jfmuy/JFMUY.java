package ruiseki.jfmuy;

import com.gtnewhorizon.gtnhlib.config.ConfigException;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import ruiseki.jfmuy.config.ModConfig;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    dependencies = Reference.DEPENDENCIES,
    guiFactory = Reference.GUI_FACTORY)
public class JFMUY {

    static {
        try {
            ModConfig.registerConfig();
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static CommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static JFMUY instance;

    public JFMUY() {

    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {

    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {

    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {

    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {

    }
}
