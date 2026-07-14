package ruiseki.jfmuy.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;
import ruiseki.jfmuy.util.CommandUtilServer;

public class PacketGiveItemStack extends PacketJFMUY {

    private final ItemStack itemStack;

    public PacketGiveItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.GIVE_BIG;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        NBTTagCompound nbt = itemStack.writeToNBT(new NBTTagCompound());
        try {
            buf.writeNBTTagCompoundToBuffer(nbt);
        } catch (Exception ignore) {}
    }

    public static class Handler implements IPacketJFMUYHandler {

        @Override
        public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP sender = (EntityPlayerMP) player;

                NBTTagCompound itemStackSerialized = buf.readNBTTagCompoundFromBuffer();
                if (itemStackSerialized != null) {
                    ItemStack itemStack = ItemStack.loadItemStackFromNBT(itemStackSerialized);
                    if (itemStack != null) {
                        CommandUtilServer.executeGive(sender, itemStack);
                    }
                }
            }
        }
    }
}
