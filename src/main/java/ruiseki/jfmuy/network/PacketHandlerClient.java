package ruiseki.jfmuy.network;

import java.io.IOException;
import java.util.EnumMap;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.jfmuy.network.packets.IPacketJFMUYHandler;
import ruiseki.jfmuy.network.packets.PacketCheatPermission;
import ruiseki.jfmuy.util.Log;

@SideOnly(Side.CLIENT)
public class PacketHandlerClient extends PacketHandler {

    public final EnumMap<PacketIdClient, IPacketJFMUYHandler> clientHandlers = new EnumMap<>(PacketIdClient.class);

    public PacketHandlerClient() {
        clientHandlers.put(PacketIdClient.CHEAT_PERMISSION, PacketCheatPermission::readPacketData);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        PacketBuffer packetBuffer = new PacketBuffer(event.packet.payload());
        Minecraft minecraft = Minecraft.getMinecraft();

        try {
            byte packetIdOrdinal = packetBuffer.readByte();
            PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
            IPacketJFMUYHandler packetHandler = clientHandlers.get(packetId);
            checkThreadAndEnqueue(packetHandler, packetBuffer, minecraft.thePlayer);
        } catch (Exception ex) {
            Log.get()
                .error("Packet error", ex);
        }
    }

    private static void checkThreadAndEnqueue(final IPacketJFMUYHandler packet, final PacketBuffer packetBuffer,
        final EntityPlayer player) {
        try {
            packet.readPacketData(packetBuffer, player);
        } catch (IOException e) {
            Log.get()
                .error("Network Error", e);
        }
    }
}
