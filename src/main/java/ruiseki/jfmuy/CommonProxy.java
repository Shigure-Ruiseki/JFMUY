package ruiseki.jfmuy;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.util.Log;

public class CommonProxy {

    public void preInit(@Nonnull FMLPreInitializationEvent event) {

    }

    public void init(@Nonnull FMLInitializationEvent event) {

    }

    public void startNEI() {

    }

    /** Runs {@link #startNEI()} on the client thread after registry remapping (integrated server). */
    public void scheduleStartNEI() {

    }

    public void restartNEI() {

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
