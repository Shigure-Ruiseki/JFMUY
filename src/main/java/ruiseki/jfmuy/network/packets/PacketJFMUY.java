package ruiseki.jfmuy.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.Unpooled;
import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketHandler;
import ruiseki.jfmuy.util.Log;

public abstract class PacketJFMUY {

    private final IPacketId id = getPacketId();

    public final FMLProxyPacket getPacket() {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

        packetBuffer.writeByte(id.ordinal());
        try {
            writePacketData(packetBuffer);
        } catch (IOException e) {
            Log.error("Error creating packet", e);
        }

        return new FMLProxyPacket(packetBuffer, PacketHandler.CHANNEL_ID);
    }

    public abstract IPacketId getPacketId();

    public abstract void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException;

    public abstract void writePacketData(PacketBuffer buf) throws IOException;
}
