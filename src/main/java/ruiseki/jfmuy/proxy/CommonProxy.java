package ruiseki.jfmuy.proxy;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.JFMUYHandler;
import ruiseki.jfmuy.network.PacketCheatPermission;
import ruiseki.jfmuy.network.PacketCraftUpdate;
import ruiseki.jfmuy.network.PacketDeletePlayerItem;
import ruiseki.jfmuy.network.PacketGiveItemStack;
import ruiseki.jfmuy.network.PacketRecipeTransfer;
import ruiseki.jfmuy.network.PacketRequestCheatPermission;
import ruiseki.jfmuy.network.PacketSetHotbarItemStack;
import ruiseki.jfmuy.plugins.nei.NEIKeyEventHandler;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return JFMUY._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);
        packetHandler.register(PacketCheatPermission.class);
        packetHandler.register(PacketCraftUpdate.class);
        packetHandler.register(PacketDeletePlayerItem.class);
        packetHandler.register(PacketGiveItemStack.class);
        packetHandler.register(PacketRecipeTransfer.class);
        packetHandler.register(PacketRequestCheatPermission.class);
        packetHandler.register(PacketSetHotbarItemStack.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(JFMUYHandler.INSTANCE);
        if (Loader.isModLoaded("NotEnoughItems")) {
            FMLCommonHandler.instance()
                .bus()
                .register(new NEIKeyEventHandler());
        }
    }
}
