package ruiseki.jfmuy.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;

public class PacketDeletePlayerItem extends PacketJFMUY {

    private final ItemStack itemStack;

    public PacketDeletePlayerItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.DELETE_ITEM;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        int itemId = Item.getIdFromItem(itemStack.getItem());
        buf.writeShort(itemId);
    }

    public static class Handler implements IPacketJeiHandler {

        @Override
        public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
            int itemId = buf.readShort();
            Item item = Item.getItemById(itemId);
            ItemStack playerItem = player.inventory.getItemStack();
            if (playerItem != null && playerItem.getItem() == item) {
                player.inventory.setItemStack(null);
            }
        }
    }
}
