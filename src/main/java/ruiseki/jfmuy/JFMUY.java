package ruiseki.jfmuy;

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import ruiseki.jfmuy.config.ServerInfo;
import ruiseki.okcore.helper.MinecraftHelpers;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ICommonProxy;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    guiFactory = Reference.GUI_FACTORY,
    dependencies = Reference.DEPENDENCIES)
public class JFMUY extends ModBase {

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static ICommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static JFMUY instance;

    public JFMUY() {
        super(Reference.MOD_ID, Reference.MOD_NAME);
        putGenericReference(REFKEY_MOD_VERSION, Reference.VERSION);
        putGenericReference(REFKEY_VERSION_CHECKER_URL, Reference.UPDATE_URL);
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
    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        super.preInit(event);
        if (MinecraftHelpers.isClientSide()) {
            JFMUYHandler.INSTANCE.preInit(event);
        }
    }

    @Mod.EventHandler
    @Override
    public void init(@Nonnull FMLInitializationEvent event) {
        super.init(event);
        if (MinecraftHelpers.isClientSide()) {
            JFMUYHandler.INSTANCE.init(event);
        }
    }

    @Mod.EventHandler
    @Override
    public void postInit(@Nonnull FMLPostInitializationEvent event) {
        super.postInit(event);
        if (MinecraftHelpers.isClientSide()) {
            JFMUYHandler.INSTANCE.postInit(event);
        }
        if (Reference.DEOBFUSCATED) {
            Internal.getIngredientFilter()
                .logStatistics();
        }
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return null;
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }
}
