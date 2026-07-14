package ruiseki.jfmuy.network.packets;

import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.Unpooled;
import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketHandler;

public abstract class PacketJFMUY {

    private final IPacketId id = getPacketId();

    public final FMLProxyPacket getPacket() {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

        packetBuffer.writeByte(id.ordinal());
        writePacketData(packetBuffer);

        return new FMLProxyPacket(packetBuffer, PacketHandler.CHANNEL_ID);
    }

    public abstract IPacketId getPacketId();

    public abstract void writePacketData(PacketBuffer buf);
}
