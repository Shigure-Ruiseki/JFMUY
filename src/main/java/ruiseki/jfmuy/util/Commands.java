package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import org.apache.commons.lang3.StringUtils;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.network.packets.PacketGiveItemMessageBig;

public class Commands {

    public static void giveFullStack(@Nonnull ItemStack itemstack) {
        giveStack(itemstack, itemstack.getMaxStackSize());
    }

    public static void giveOneFromStack(@Nonnull ItemStack itemstack) {
        giveStack(itemstack, 1);
    }

    /**
     * /give <player> <item> [amount] [data] [dataTag]
     */
    public static void giveStack(@Nonnull ItemStack itemStack, int amount) {
        EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
        String senderName = sender.getCommandSenderName();

        List<String> commandStrings = new ArrayList<>();
        commandStrings.add("/give");
        commandStrings.add(senderName);
        commandStrings.add(
            Item.itemRegistry.getNameForObject(itemStack.getItem())
                .toString());
        commandStrings.add(String.valueOf(amount));
        commandStrings.add(String.valueOf(itemStack.getItemDamage()));

        if (itemStack.hasTagCompound()) {
            commandStrings.add(
                itemStack.getTagCompound()
                    .toString());
        }

        String fullCommand = StringUtils.join(commandStrings, " ");
        sendChatMessage(sender, fullCommand);
    }

    private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
        if (chatMessage.length() <= 100) {
            if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(sender, chatMessage) != 0) {
                return;
            }
            NetHandlerPlayClient netHandler = Minecraft.getMinecraft()
                .getNetHandler();
            if (netHandler != null) {
                netHandler.addToSendQueue(new C01PacketChatMessage(chatMessage));
            }
        } else {
            if (Config.isJfmuyOnServer()) {
                PacketGiveItemMessageBig packet = new PacketGiveItemMessageBig(chatMessage);
                JFMUY.getProxy()
                    .sendPacketToServer(packet);
            } else {
                ChatComponentTranslation errorMessage = new ChatComponentTranslation(
                    "jfmuy.chat.error.command.too.long");
                errorMessage.getChatStyle()
                    .setColor(EnumChatFormatting.RED);
                sender.addChatComponentMessage(errorMessage);

                ChatComponentText chatMessageComponent = new ChatComponentText(chatMessage);
                chatMessageComponent.getChatStyle()
                    .setColor(EnumChatFormatting.RED);
                sender.addChatComponentMessage(chatMessageComponent);
            }
        }
    }
}
