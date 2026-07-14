package ruiseki.jfmuy.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.network.packets.PacketDeletePlayerItem;
import ruiseki.jfmuy.network.packets.PacketGiveItemStack;
import ruiseki.jfmuy.network.packets.PacketJFMUY;
import ruiseki.jfmuy.network.packets.PacketRecipeTransfer;
import ruiseki.jfmuy.util.Log;

public class PacketHandler {

    public static final String CHANNEL_ID = Reference.MOD_ID;
    private final FMLEventChannel channel;

    public PacketHandler() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL_ID);
        channel.register(this);
    }

    @SubscribeEvent
    public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        PacketBuffer packetBuffer = new PacketBuffer(event.packet.payload());
        EntityPlayerMP player = ((NetHandlerPlayServer) event.handler).playerEntity;

        try {
            byte packetIdOrdinal = packetBuffer.readByte();
            PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
            PacketJFMUY packet;

            switch (packetId) {
                case RECIPE_TRANSFER: {
                    packet = new PacketRecipeTransfer();
                    break;
                }
                case DELETE_ITEM: {
                    packet = new PacketDeletePlayerItem();
                    break;
                }
                case GIVE_BIG: {
                    packet = new PacketGiveItemStack();
                    break;
                }
                default: {
                    return;
                }
            }

            checkThreadAndEnqueue(packet, packetBuffer, player);
        } catch (RuntimeException ex) {
            Log.error("Packet error", ex);
        }
    }

    /*
     * @SubscribeEvent
     * public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
     * PacketBuffer packetBuffer = new PacketBuffer(event.packet.payload());
     * Minecraft minecraft = Minecraft.getMinecraft();
     * EntityPlayer player = minecraft.thePlayer;
     * PacketJFMUY packet;
     * try {
     * byte packetIdOrdinal = packetBuffer.readByte();
     * PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
     * switch (packetId) {
     * default: {
     * return;
     * }
     * }
     * checkThreadAndEnqueue(packet, packetBuffer, player, minecraft);
     * } catch (Exception ex) {
     * ex.printStackTrace();
     * }
     * }
     */

    public void sendPacket(FMLProxyPacket packet, EntityPlayerMP player) {
        channel.sendTo(packet, player);
    }

    private static void checkThreadAndEnqueue(final PacketJFMUY packet, final PacketBuffer packetBuffer,
        final EntityPlayer player) {
        try {
            packet.readPacketData(packetBuffer, player);
        } catch (IOException e) {
            Log.error("Network Error", e);
        }
    }
}
