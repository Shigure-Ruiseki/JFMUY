package ruiseki.jfmuy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.jfmuy.util.CommandUtilServer;
import ruiseki.jfmuy.util.GiveMode;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketGiveItemStack extends PacketCodec {

    @CodecField
    private ItemStack itemStack = null;

    @CodecField
    private int giveModeOrdinal = 0;

    public PacketGiveItemStack() {}

    public PacketGiveItemStack(ItemStack itemStack, GiveMode giveMode) {
        this.itemStack = itemStack;
        this.giveModeOrdinal = giveMode.ordinal();
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
        GiveMode giveMode = GiveMode.fromOrdinal(this.giveModeOrdinal);
        CommandUtilServer.executeGive(player, this.itemStack, giveMode);
    }
}
