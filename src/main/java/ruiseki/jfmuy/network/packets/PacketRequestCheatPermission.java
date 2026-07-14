package ruiseki.jfmuy.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import ruiseki.jfmuy.CommonProxy;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;
import ruiseki.jfmuy.util.CommandUtilServer;

public class PacketRequestCheatPermission extends PacketJFMUY {

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.CHEAT_PERMISSION_REQUEST;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        // the packet itself is the only data needed
    }

    public static class Handler implements IPacketJeiHandler {

        @Override
        public void readPacketData(PacketBuffer buf, EntityPlayer player) {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP sender = (EntityPlayerMP) player;
                boolean hasPermission = CommandUtilServer.hasPermission(sender, new ItemStack(Items.nether_star, 64));
                PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

                CommonProxy proxy = JFMUY.getProxy();
                proxy.sendPacketToClient(packetCheatPermission, sender);
            }
        }
    }
}
