package ruiseki.jfmuy.network;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.network.packets.IPacketJFMUYHandler;
import ruiseki.jfmuy.network.packets.PacketCheatPermission;
import ruiseki.jfmuy.network.packets.PacketDeletePlayerItem;
import ruiseki.jfmuy.network.packets.PacketGiveItemStack;
import ruiseki.jfmuy.network.packets.PacketRecipeTransfer;
import ruiseki.jfmuy.network.packets.PacketRequestCheatPermission;
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
            IPacketJFMUYHandler packetHandler;

            switch (packetId) {
                case RECIPE_TRANSFER: {
                    packetHandler = new PacketRecipeTransfer.Handler();
                    break;
                }
                case DELETE_ITEM: {
                    packetHandler = new PacketDeletePlayerItem.Handler();
                    break;
                }
                case GIVE_BIG: {
                    packetHandler = new PacketGiveItemStack.Handler();
                    break;
                }
                case CHEAT_PERMISSION_REQUEST: {
                    packetHandler = new PacketRequestCheatPermission.Handler();
                    break;
                }
                default: {
                    return;
                }
            }

            checkThreadAndEnqueue(packetHandler, packetBuffer, player);
        } catch (RuntimeException ex) {
            Log.error("Packet error", ex);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        PacketBuffer packetBuffer = new PacketBuffer(event.packet.payload());
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.thePlayer;
        IPacketJFMUYHandler packetHandler;

        try {
            byte packetIdOrdinal = packetBuffer.readByte();
            PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
            switch (packetId) {
                case CHEAT_PERMISSION: {
                    packetHandler = new PacketCheatPermission.Handler();
                    break;
                }
                default: {
                    return;
                }
            }

            checkThreadAndEnqueue(packetHandler, packetBuffer, player);
        } catch (Exception ex) {
            Log.error("Packet error", ex);
        }
    }

    public void sendPacket(FMLProxyPacket packet, EntityPlayerMP player) {
        channel.sendTo(packet, player);
    }

    private static void checkThreadAndEnqueue(final IPacketJFMUYHandler packet, final PacketBuffer packetBuffer,
        final EntityPlayer player) {
        try {
            packet.readPacketData(packetBuffer, player);
        } catch (IOException e) {
            Log.error("Network Error", e);
        }
    }
}
