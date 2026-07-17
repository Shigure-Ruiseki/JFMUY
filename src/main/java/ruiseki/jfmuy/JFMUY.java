package ruiseki.jfmuy;

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.MinecraftServer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import ruiseki.jfmuy.command.CommandLoadBookmarks;
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

    @Override
    @Mod.EventHandler
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        super.preInit(event);
        if (MinecraftHelpers.isClientSide()) {
            JFMUYClientHandler.INSTANCE.preInit(event);
        }
    }

    @Override
    @Mod.EventHandler
    public void init(@Nonnull FMLInitializationEvent event) {
        super.init(event);
        if (MinecraftHelpers.isClientSide()) {
            JFMUYClientHandler.INSTANCE.init(event);
        }
    }

    @Override
    @Mod.EventHandler
    public void postInit(@Nonnull FMLPostInitializationEvent event) {
        super.postInit(event);
        if (MinecraftHelpers.isClientSide()) {
            JFMUYClientHandler.INSTANCE.postInit(event);
        }
    }

    @Override
    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        super.onServerStarted(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        super.onServerStopped(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        super.onServerStopping(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        super.onServerStarting(event);
    }

    @Override
    protected LiteralArgumentBuilder<ICommandSender> constructBaseCommand(MinecraftServer server) {
        LiteralArgumentBuilder<ICommandSender> builder = super.constructBaseCommand(server);
        builder.then(new CommandLoadBookmarks(this).make());
        return builder;
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
