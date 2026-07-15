package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

import com.google.common.base.Throwables;

import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.network.packets.PacketCheatPermission;
import ruiseki.okcore.helper.Helpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;

/**
 * Server-side-safe utilities for commands.
 */
public final class CommandUtilServer {

    private CommandUtilServer() {}

    public static String[] getGiveCommandParameters(EntityPlayer sender, ItemStack itemStack, int amount) {
        String senderName = sender.getCommandSenderName();
        Item item = itemStack.getItem();
        ResourceLocation itemResourceLocation = Helpers.getLocation(item);
        if (itemResourceLocation == null) {
            String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
            throw new IllegalArgumentException("item.getRegistryName() returned null for: " + stackInfo);
        }

        List<String> commandStrings = new ArrayList<>();
        commandStrings.add(senderName);
        commandStrings.add(itemResourceLocation.toString());
        commandStrings.add(String.valueOf(amount));
        commandStrings.add(String.valueOf(itemStack.getItemDamage()));

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            commandStrings.add(tagCompound.toString());
        }

        return commandStrings.toArray(new String[0]);
    }

    public static void writeChatMessage(EntityPlayer player, String translationKey, EnumChatFormatting color) {
        ChatComponentTranslation component = new ChatComponentTranslation(translationKey);
        component.getChatStyle()
            .setColor(color);
        player.addChatMessage(component);
    }

    public static boolean hasPermission(EntityPlayerMP sender, ItemStack itemStack) {
        if (sender.capabilities.isCreativeMode) {
            return true;
        }

        MinecraftServer minecraftServer = MinecraftServer.getServer();
        ICommand giveCommand = getGiveCommand(sender);
        if (giveCommand != null && giveCommand.canCommandSenderUseCommand(sender)) {
            String[] commandParameters = getGiveCommandParameters(sender, itemStack, itemStack.stackSize);
            CommandEvent event = new CommandEvent(giveCommand, sender, commandParameters);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                Throwable exception = event.exception;
                if (exception != null) {
                    Throwables.propagateIfPossible(exception);
                }
                return false;
            }
            return true;
        } else {
            return minecraftServer.getConfigurationManager()
                .func_152596_g(sender.getGameProfile());
        }
    }

    public static void executeGive(EntityPlayerMP sender, ItemStack itemStack, GiveMode giveMode) {
        if (hasPermission(sender, itemStack)) {
            if (giveMode == GiveMode.INVENTORY) {
                giveToInventory(sender, itemStack);
            } else if (giveMode == GiveMode.MOUSE_PICKUP) {
                mousePickupItemStack(sender, itemStack);
            }
        } else {
            JFMUY.getProxy()
                .sendPacketToClient(new PacketCheatPermission(false), sender);
        }
    }

    public static void setHotbarSlot(EntityPlayerMP sender, ItemStack itemStack, int hotbarSlot) {
        if (hasPermission(sender, itemStack)) {
            if (hotbarSlot < 0 || hotbarSlot > 8) {
                Log.get()
                    .error("Tried to set slot that is not in the hotbar: {}", hotbarSlot);
                return;
            }
            ItemStack stackInSlot = sender.inventory.getStackInSlot(hotbarSlot);
            if (ItemStack.areItemStacksEqual(stackInSlot, itemStack)) {
                return;
            }
            final int count = itemStack.stackSize;
            ItemStack originalStack = itemStack.copy();
            sender.inventory.setInventorySlotContents(hotbarSlot, itemStack);
            sender.worldObj.playSoundAtEntity(
                sender,
                "random.pop",
                0.2F,
                ((sender.getRNG()
                    .nextFloat()
                    - sender.getRNG()
                        .nextFloat())
                    * 0.7F + 1.0F) * 2.0F);
            sender.inventoryContainer.detectAndSendChanges();
            notifyGive(sender, originalStack, count);
        } else {
            JFMUY.getProxy()
                .sendPacketToClient(new PacketCheatPermission(false), sender);
        }
    }

    public static void mousePickupItemStack(EntityPlayer sender, ItemStack itemStack) {
        final int giveCount;
        ItemStack existingStack = sender.inventory.getItemStack();
        if (canStack(existingStack, itemStack)) {
            int newCount = Math.min(existingStack.getMaxStackSize(), existingStack.stackSize + itemStack.stackSize);
            giveCount = newCount - existingStack.stackSize;
            if (giveCount > 0) {
                existingStack.stackSize = newCount;
            }
        } else {
            sender.inventory.setItemStack(itemStack);
            giveCount = itemStack.stackSize;
        }

        if (giveCount > 0 && sender instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) sender;
            notifyGive(playerMP, itemStack, giveCount);
            playerMP.updateHeldItem();
        }
    }

    public static boolean canStack(ItemStack a, ItemStack b) {
        return a != null && b != null && ItemHandlerHelpers.canItemStacksStack(a, b);
    }

    /**
     * Gives a player an item. Similar to vanilla but without the "fake" itemStack popping into the player's face.
     */
    private static void giveToInventory(EntityPlayerMP sender, ItemStack itemStack) {
        int count = itemStack.stackSize;
        ItemStack originalStack = itemStack.copy();
        boolean addedToInventory = sender.inventory.addItemStackToInventory(itemStack);

        if (addedToInventory) {
            sender.worldObj.playSoundAtEntity(
                sender,
                "random.pop",
                0.2F,
                ((sender.getRNG()
                    .nextFloat()
                    - sender.getRNG()
                        .nextFloat())
                    * 0.7F + 1.0F) * 2.0F);
            sender.inventoryContainer.detectAndSendChanges();
        }

        if (!addedToInventory || itemStack.stackSize > 0) {
            EntityItem entityitem = sender.dropPlayerItemWithRandomChoice(itemStack, false);
            if (entityitem != null) {
                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(sender.getCommandSenderName());
            }
        }

        notifyGive(sender, originalStack, count);
    }

    private static void notifyGive(EntityPlayerMP sender, ItemStack itemStack, int count) {
        if (!sender.capabilities.isCreativeMode && count > 0) {
            ICommand giveCommand = getGiveCommand(sender);
            if (giveCommand != null) {
                ItemStack copy = itemStack.copy();
                copy.stackSize = 1;
                CommandBase.func_152373_a(
                    sender,
                    giveCommand,
                    "commands.give.success",
                    copy.func_151000_E(),
                    count,
                    sender.getCommandSenderName());
            }
        }
    }

    @Nullable
    private static ICommand getGiveCommand(EntityPlayerMP sender) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        ICommandManager commandManager = minecraftServer.getCommandManager();
        Map<String, ICommand> commands = commandManager.getCommands();
        return commands.get("give");
    }
}
