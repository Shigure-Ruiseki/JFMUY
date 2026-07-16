package ruiseki.jfmuy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import ruiseki.jfmuy.Internal;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketCraftUpdate extends PacketCodec {

    @CodecField
    private boolean success = false;

    @CodecField
    private int itemsCrafted = 0;

    public PacketCraftUpdate() {}

    public PacketCraftUpdate(boolean success, int itemsCrafted) {
        this.success = success;
        this.itemsCrafted = itemsCrafted;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        Internal.getRuntime()
            .getAutocraftingHandler()
            .stepFinished(this.success, success ? this.itemsCrafted : 0);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
