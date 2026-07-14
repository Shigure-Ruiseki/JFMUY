package ruiseki.jfmuy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.util.Log;

public class CommonProxy {

    public void preInit(@NotNull FMLPreInitializationEvent event) {

    }

    public void init(@NotNull FMLInitializationEvent event) {

    }

    public void postInit(@NotNull FMLPostInitializationEvent event) {

    }

    public void restartJFMUY() {

    }

    public void resetItemFilter() {

    }

    public void sendPacketToServer(PacketJFMUY packet) {
        Log.error("Tried to send packet to the server from the server: {}", packet);
    }

    public void sendPacketToPlayer(PacketJFMUY packet, EntityPlayer entityplayer) {
        if (!(entityplayer instanceof EntityPlayerMP) || (entityplayer instanceof FakePlayer)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) entityplayer;
        JFMUY.getPacketHandler()
            .sendPacket(packet.getPacket(), player);
    }
}
