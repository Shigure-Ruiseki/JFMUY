package ruiseki.jfmuy.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.ServerInfo;
import ruiseki.jfmuy.network.packets.PacketGiveItemStack;
import ruiseki.jfmuy.network.packets.PacketSetHotbarItemStack;
import ruiseki.okcore.helper.Helpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;

public final class CommandUtil {

    private CommandUtil() {}

    /**
     * /give <player> <item> [amount] [data] [dataTag]
     * {@link GuiContainerCreative} has special client-side handling for itemStacks, just give the item on the client
     */
    public static void giveStack(ItemStack itemStack, int mouseButton) {
        final GiveMode giveMode = Config.getGiveMode();
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.thePlayer;
        if (player == null) {
            Log.get()
                .error("Can't give stack, there is no player");
            return;
        }
        if (minecraft.currentScreen instanceof GuiContainerCreative && giveMode == GiveMode.MOUSE_PICKUP) {
            final int amount = giveMode.getStackSize(itemStack, mouseButton);
            ItemStack sendStack = ItemHandlerHelpers.copyStackWithSize(itemStack, amount);
            CommandUtilServer.mousePickupItemStack(player, sendStack);
        } else if (ServerInfo.isJFMUYOnServer()) {
            final int amount = giveMode.getStackSize(itemStack, mouseButton);
            ItemStack sendStack = ItemHandlerHelpers.copyStackWithSize(itemStack, amount);
            PacketGiveItemStack packet = new PacketGiveItemStack(sendStack, giveMode);
            JFMUY.getProxy()
                .sendPacketToServer(packet);
        } else {
            int amount = GiveMode.INVENTORY.getStackSize(itemStack, mouseButton);
            giveStackVanilla(itemStack, amount);
        }
    }

    public static void setHotbarStack(ItemStack itemStack, int hotbarSlot) {
        if (ServerInfo.isJFMUYOnServer()) {
            ItemStack sendStack = ItemHandlerHelpers.copyStackWithSize(itemStack, itemStack.getMaxStackSize());
            PacketSetHotbarItemStack packet = new PacketSetHotbarItemStack(sendStack, hotbarSlot);
            JFMUY.getProxy()
                .sendPacketToServer(packet);
        }
    }

    /**
     * Fallback for when JEI is not on the server, tries to use the /give command
     * Uses the Creative Inventory Action Packet when in creative, which doesn't require the player to be op.
     */
    private static void giveStackVanilla(ItemStack itemStack, int amount) {
        if (itemStack == null) {
            String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
            Log.get()
                .error("Empty itemStack: {}", stackInfo, new IllegalArgumentException());
            return;
        }

        Item item = itemStack.getItem();
        ResourceLocation itemResourceLocation = Helpers.getLocation(item);
        ErrorUtil.checkNotNull(itemResourceLocation, "itemStack.getItem().getRegistryName()");

        EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
        if (sender != null) {
            if (sender.canCommandSenderUseCommand(2, "give")) {
                sendGiveAction(sender, itemStack, amount);
            } else if (sender.capabilities.isCreativeMode) {
                sendCreativeInventoryActions(sender, itemStack, amount);
            } else {
                // try this in case the vanilla server has permissions set so regular players can use /give
                sendGiveAction(sender, itemStack, amount);
            }
        }
    }

    private static void sendGiveAction(EntityPlayerSP sender, ItemStack itemStack, int amount) {
        String[] commandParameters = CommandUtilServer.getGiveCommandParameters(sender, itemStack, amount);
        String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
        sendChatMessage(sender, fullCommand);
    }

    private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
        if (chatMessage.length() <= 256) {
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
            if (currentStack == null) {
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
