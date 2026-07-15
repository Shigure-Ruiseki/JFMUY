package ruiseki.jfmuy.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdClient;
import ruiseki.jfmuy.util.CommandUtilServer;

public class PacketCheatPermission extends PacketJFMUY {

    private final boolean hasPermission;

    public PacketCheatPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdClient.CHEAT_PERMISSION;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeBoolean(hasPermission);
    }

    public static void readPacketData(PacketBuffer buf, EntityPlayer player) {
        boolean hasPermission = buf.readBoolean();
        if (!hasPermission && Config.isCheatItemsEnabled()) {
            CommandUtilServer
                .writeChatMessage(player, "jfmuy.chat.error.no.cheat.permission.1", EnumChatFormatting.RED);
            CommandUtilServer
                .writeChatMessage(player, "jfmuy.chat.error.no.cheat.permission.2", EnumChatFormatting.RED);
            Config.setCheatItemsEnabled(false);
            player.closeScreen();
        }
    }
}
