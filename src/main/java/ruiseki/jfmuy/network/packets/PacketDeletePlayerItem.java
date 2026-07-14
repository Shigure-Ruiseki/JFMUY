package ruiseki.jfmuy.network.packets;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;

public class PacketDeletePlayerItem extends PacketJFMUY {

    private ItemStack itemStack;

    public PacketDeletePlayerItem() {

    }

    public PacketDeletePlayerItem(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.DELETE_ITEM;
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeItemStackToBuffer(itemStack);
    }

    @Override
    public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
        itemStack = buf.readItemStackFromBuffer();
        ItemStack playerItem = player.inventory.getItemStack();
        if (ItemStack.areItemStacksEqual(itemStack, playerItem)) {
            player.inventory.setItemStack(null);
        }
    }
}
