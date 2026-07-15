package ruiseki.jfmuy;

import net.minecraft.entity.player.EntityPlayerMP;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import ruiseki.jfmuy.network.PacketHandler;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.util.Log;

public class CommonProxy {

    @Nullable
    protected FMLEventChannel channel;

    public void preInit(@NotNull FMLPreInitializationEvent event) {
        PacketHandler packetHandler = new PacketHandler();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketHandler.CHANNEL_ID);
        channel.register(packetHandler);
    }

    public void init(@NotNull FMLInitializationEvent event) {

    }

    public void postInit(@NotNull FMLPostInitializationEvent event) {

    }

    public void restartJFMUY() {

    }

    public void sendPacketToServer(PacketJFMUY packet) {
        Log.get()
            .error("Tried to send packet to the server from the server: {}", packet);
    }

    public void sendPacketToClient(PacketJFMUY packet, EntityPlayerMP player) {
        if (channel != null) {
            channel.sendTo(packet.getPacket(), player);
        }
    }
}
