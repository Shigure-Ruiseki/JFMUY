package ruiseki.jfmuy.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;
import ruiseki.jfmuy.util.CommandUtilServer;
import ruiseki.jfmuy.util.GiveMode;

public class PacketGiveItemStack extends PacketJFMUY {

    private final ItemStack itemStack;
    private final GiveMode giveMode;

    public PacketGiveItemStack(ItemStack itemStack, GiveMode giveMode) {
        this.itemStack = itemStack;
        this.giveMode = giveMode;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.GIVE_ITEM;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        try {
            buf.writeItemStackToBuffer(itemStack);
            buf.writeVarIntToBuffer(giveMode.ordinal());
        } catch (Exception ignore) {

        }
    }

    public static void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP sender = (EntityPlayerMP) player;

            ItemStack itemStack = buf.readItemStackFromBuffer();
            if (itemStack != null) {
                GiveMode giveMode = GiveMode.fromOrdinal(buf.readVarIntFromBuffer());
                CommandUtilServer.executeGive(sender, itemStack, giveMode);
            }
        }
    }
}
