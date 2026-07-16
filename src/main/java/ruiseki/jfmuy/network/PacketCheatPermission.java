package ruiseki.jfmuy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.util.CommandUtilServer;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketCheatPermission extends PacketCodec {

    @CodecField
    private boolean hasPermission;

    public PacketCheatPermission() {}

    public PacketCheatPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (!this.hasPermission && Config.isCheatItemsEnabled()) {
            CommandUtilServer
                .writeChatMessage(player, "jfmuy.chat.error.no.cheat.permission.1", EnumChatFormatting.RED);
            CommandUtilServer
                .writeChatMessage(player, "jfmuy.chat.error.no.cheat.permission.2", EnumChatFormatting.RED);
            Config.setCheatItemsEnabled(false);
            player.closeScreen();
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {}
}
