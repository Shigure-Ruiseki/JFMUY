package ruiseki.jfmuy.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import com.google.common.base.Preconditions;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;
import ruiseki.jfmuy.util.CommandUtilServer;
import ruiseki.jfmuy.util.ErrorUtil;

public class PacketSetHotbarItemStack extends PacketJFMUY {

    private final ItemStack itemStack;
    private final int hotbarSlot;

    public PacketSetHotbarItemStack(ItemStack itemStack, int hotbarSlot) {
        ErrorUtil.checkNotNull(itemStack, "itemStack");
        Preconditions
            .checkArgument(hotbarSlot >= 0 && hotbarSlot <= 8, "hotbar slot must be in the hotbar. got: " + hotbarSlot);
        this.itemStack = itemStack;
        this.hotbarSlot = hotbarSlot;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.SET_HOTBAR_ITEM;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        NBTTagCompound nbt = itemStack.writeToNBT(new NBTTagCompound());
        try {
            buf.writeNBTTagCompoundToBuffer(nbt);
        } catch (Exception ignore) {}
        buf.writeVarIntToBuffer(hotbarSlot);
    }

    public static void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
        if (player instanceof EntityPlayerMP sender) {

            NBTTagCompound itemStackSerialized = buf.readNBTTagCompoundFromBuffer();
            if (itemStackSerialized != null) {
                int hotbarSlot = buf.readVarIntFromBuffer();
                ItemStack itemStack = ItemStack.loadItemStackFromNBT(itemStackSerialized);
                if (itemStack != null) {
                    CommandUtilServer.setHotbarSlot(sender, itemStack, hotbarSlot);
                }
            }
        }
    }
}
