package ruiseki.jfmuy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;

import ruiseki.jfmuy.util.CommandUtilServer;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketSetHotbarItemStack extends PacketCodec {

    @CodecField
    private ItemStack itemStack;

    @CodecField
    private int hotbarSlot;

    public PacketSetHotbarItemStack() {}

    public PacketSetHotbarItemStack(ItemStack itemStack, int hotbarSlot) {
        ErrorUtil.checkNotNull(itemStack, "itemStack");
        Preconditions
            .checkArgument(hotbarSlot >= 0 && hotbarSlot <= 8, "hotbar slot must be in the hotbar. got: " + hotbarSlot);
        this.itemStack = itemStack;
        this.hotbarSlot = hotbarSlot;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if (this.itemStack != null) {
            if (this.hotbarSlot >= 0 && this.hotbarSlot <= 8) {
                CommandUtilServer.setHotbarSlot(player, this.itemStack, this.hotbarSlot);
            }
        }
    }
}
