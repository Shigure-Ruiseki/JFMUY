package ruiseki.jfmuy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketDeletePlayerItem extends PacketCodec {

    @CodecField
    private Item item = null;

    public PacketDeletePlayerItem() {}

    public PacketDeletePlayerItem(ItemStack itemStack) {
        this.item = itemStack.getItem();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if (this.item != null) {
            ItemStack playerItem = player.inventory.getItemStack();
            if (playerItem != null && playerItem.getItem() == this.item) {
                player.inventory.setItemStack(null);
                player.updateHeldItem();
            }
        }
    }
}
