package ruiseki.jfmuy.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.config.SessionData;
import ruiseki.jfmuy.network.packets.PacketGiveItemStack;
import ruiseki.okcore.helper.ItemHandlerHelpers;

public final class CommandUtil {

    private CommandUtil() {}

    /**
     * /give <player> <item> [amount] [data] [dataTag]
     * {@link net.minecraft.client.gui.inventory.GuiContainerCreative} has special client-side handling for itemStacks,
     * just give the item on the client
     */
    public static void giveStack(ItemStack itemStack, int mouseButton) {
        final int amount = (mouseButton == 0) ? itemStack.getMaxStackSize() : 1;
        if (SessionData.isJfmuyOnServer()) {
            ItemStack sendStack = ItemHandlerHelpers.copyStackWithSize(itemStack, amount);
            PacketGiveItemStack packet = new PacketGiveItemStack(sendStack);
            JFMUY.getProxy()
                .sendPacketToServer(packet);
        } else {
            giveStackVanilla(itemStack, amount);
        }
    }

    /**
     * Fallback for when JFMUY is not on the server, tries to use the /give command
     * Uses the Creative Inventory Action Packet when in creative, which doesn't require the player to be op.
     */
    private static void giveStackVanilla(@Nullable ItemStack itemStack, int amount) {
        if (itemStack == null || itemStack.stackSize == 0) {
            String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
            Log.error("Empty itemStack: {}", stackInfo, new IllegalArgumentException());
            return;
        }

        Item item = itemStack.getItem();
        ResourceLocation itemResourceLocation = new ResourceLocation(
            GameData.getItemRegistry()
                .getNameForObject(item));
        Preconditions.checkNotNull(itemResourceLocation, "itemStack.getItem().getRegistryName()");

        EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
        if (sender.canCommandSenderUseCommand(2, "give")) {
            sendGiveAction(sender, itemStack, amount);
        } else if (sender.capabilities.isCreativeMode) {
            sendCreativeInventoryActions(sender, itemStack, amount);
        } else {
            // try this in case the vanilla server has permissions set so regular players can use /give
            sendGiveAction(sender, itemStack, amount);
        }
    }

    private static void sendGiveAction(EntityPlayerSP sender, ItemStack itemStack, int amount) {
        String[] commandParameters = CommandUtilServer.getGiveCommandParameters(sender, itemStack, amount);
        String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
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

    private static void sendCreativeInventoryActions(EntityPlayerSP sender, ItemStack stack, int amount) {
        int i = 0; // starting in the inventory, not armour or crafting slots
        while (i < sender.inventory.mainInventory.length && amount > 0) {
            ItemStack currentStack = sender.inventory.mainInventory[i];
            if (currentStack == null || currentStack.stackSize == 0) {
                ItemStack sendAllRemaining = ItemHandlerHelpers.copyStackWithSize(stack, amount);
                sendSlotPacket(sendAllRemaining, i);
                amount = 0;
            } else if (currentStack.isItemEqual(stack) && currentStack.getMaxStackSize() > currentStack.stackSize) {
                int canAdd = Math.min(currentStack.getMaxStackSize() - currentStack.stackSize, amount);
                ItemStack fillRemainingSpace = ItemHandlerHelpers
                    .copyStackWithSize(stack, canAdd + currentStack.stackSize);
                sendSlotPacket(fillRemainingSpace, i);
                amount -= canAdd;
            }
            i++;
        }
        if (amount > 0) {
            ItemStack toDrop = ItemHandlerHelpers.copyStackWithSize(stack, amount);
            sendSlotPacket(toDrop, -1);
        }
    }

    private static void sendSlotPacket(ItemStack stack, int mainInventorySlot) {
        if (mainInventorySlot < 9 && mainInventorySlot != -1) {
            // slot ID for the message is different from the slot id used in the mainInventory
            mainInventorySlot += 36;
        }
        Minecraft.getMinecraft().playerController.sendSlotPacket(stack, mainInventorySlot);
    }
}
