package ruiseki.jfmuy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.util.CommandUtilServer;
import ruiseki.okcore.network.PacketCodec;

public class PacketRequestCheatPermission extends PacketCodec {

    public PacketRequestCheatPermission() {}

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        boolean hasPermission = CommandUtilServer.hasPermission(player, new ItemStack(Items.nether_star, 64));
        JFMUY.instance.getPacketHandler()
            .sendToPlayer(new PacketCheatPermission(hasPermission), player);
    }
}
