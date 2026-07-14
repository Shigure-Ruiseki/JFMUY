package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.config.SessionData;
import ruiseki.jfmuy.network.packets.PacketGiveItemStack;

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
        if (SessionData.isJfmuyOnServer()) {
            ItemStack sendStack = itemStack.copy();
            sendStack.stackSize = amount;
            PacketGiveItemStack packet = new PacketGiveItemStack(sendStack);
            JFMUY.getProxy()
                .sendPacketToServer(packet);
        } else {
            giveStackVanilla(itemStack, amount);
        }
    }

    /**
     * Fallback for when JFMUY is not on the server, tries to use the /give command.
     */
    private static void giveStackVanilla(@Nonnull ItemStack itemStack, int amount) {
        EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
        String senderName = sender.getCommandSenderName();

        List<String> commandStrings = new ArrayList<>();
        commandStrings.add("/give");
        commandStrings.add(senderName);
        commandStrings.add(
            GameData.getItemRegistry()
                .getNameForObject(itemStack.getItem()));
        commandStrings.add(String.valueOf(amount));
        commandStrings.add(String.valueOf(itemStack.getItemDamage()));

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            commandStrings.add(tagCompound.toString());
        }

        String fullCommand = StringUtils.join(commandStrings, " ");
        sendChatMessage(sender, fullCommand);
    }

    private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
        if (chatMessage.length() <= 100) {
            sender.addChatComponentMessage(new ChatComponentText(chatMessage));
        } else {
            IChatComponent errorMessage = new ChatComponentTranslation("jfmuy.chat.error.command.too.long");
            errorMessage.getChatStyle()
                .setColor(EnumChatFormatting.RED);
            sender.addChatComponentMessage(errorMessage);

            IChatComponent chatMessageComponent = new ChatComponentText(chatMessage);
            chatMessageComponent.getChatStyle()
                .setColor(EnumChatFormatting.RED);
            sender.addChatComponentMessage(chatMessageComponent);
        }
    }
}
