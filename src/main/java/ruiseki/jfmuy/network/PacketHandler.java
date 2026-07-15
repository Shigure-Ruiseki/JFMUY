package ruiseki.jfmuy.network;

import java.io.IOException;
import java.util.EnumMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.network.packets.IPacketJFMUYHandler;
import ruiseki.jfmuy.network.packets.PacketDeletePlayerItem;
import ruiseki.jfmuy.network.packets.PacketGiveItemStack;
import ruiseki.jfmuy.network.packets.PacketRecipeTransfer;
import ruiseki.jfmuy.network.packets.PacketRequestCheatPermission;
import ruiseki.jfmuy.network.packets.PacketSetHotbarItemStack;
import ruiseki.jfmuy.util.Log;

public class PacketHandler {

    public static final String CHANNEL_ID = Reference.MOD_ID;

    public final EnumMap<PacketIdServer, IPacketJFMUYHandler> serverHandlers = new EnumMap<>(PacketIdServer.class);

    public PacketHandler() {
        serverHandlers.put(PacketIdServer.RECIPE_TRANSFER, PacketRecipeTransfer::readPacketData);
        serverHandlers.put(PacketIdServer.DELETE_ITEM, PacketDeletePlayerItem::readPacketData);
        serverHandlers.put(PacketIdServer.GIVE_ITEM, PacketGiveItemStack::readPacketData);
        serverHandlers.put(PacketIdServer.SET_HOTBAR_ITEM, PacketSetHotbarItemStack::readPacketData);
        serverHandlers.put(PacketIdServer.CHEAT_PERMISSION_REQUEST, PacketRequestCheatPermission::readPacketData);
    }

    @SubscribeEvent
    public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        PacketBuffer packetBuffer = new PacketBuffer(event.packet.payload());
        EntityPlayerMP player = ((NetHandlerPlayServer) event.handler).playerEntity;

        try {
            byte packetIdOrdinal = packetBuffer.readByte();
            PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
            IPacketJFMUYHandler packetHandler = serverHandlers.get(packetId);
            checkThreadAndEnqueue(packetHandler, packetBuffer, player);
        } catch (RuntimeException ex) {
            Log.get()
                .error("Packet error", ex);
        }
    }

    private static void checkThreadAndEnqueue(IPacketJFMUYHandler packetHandler, PacketBuffer packetBuffer,
        EntityPlayer player) {
        try {
            packetHandler.readPacketData(packetBuffer, player);
        } catch (IOException e) {
            Log.get()
                .error("Network Error", e);
        }
    }
}
