package ruiseki.jfmuy.network.packets;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;

public class PacketGiveItemStack extends PacketJFMUY {

    private ItemStack itemStack;

    public PacketGiveItemStack() {

    }

    public PacketGiveItemStack(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.GIVE_BIG;
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        NBTTagCompound nbt = new NBTTagCompound();
        if (itemStack != null) {
            itemStack.writeToNBT(nbt);
        }
        buf.writeNBTTagCompoundToBuffer(nbt);
    }

    @Override
    public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP sender = (EntityPlayerMP) player;

            if (hasPermission(sender)) {
                NBTTagCompound itemStackSerialized = buf.readNBTTagCompoundFromBuffer();
                if (itemStackSerialized != null && !itemStackSerialized.hasNoTags()) {
                    ItemStack itemStack = ItemStack.loadItemStackFromNBT(itemStackSerialized);
                    if (itemStack != null) {
                        executeGive(sender, itemStack);
                    }
                }
            } else {
                ChatComponentTranslation textcomponenttranslation1 = new ChatComponentTranslation(
                    "commands.generic.permission");
                textcomponenttranslation1.getChatStyle()
                    .setColor(EnumChatFormatting.RED);
                sender.addChatMessage(textcomponenttranslation1);
            }
        }
    }

    private static boolean hasPermission(EntityPlayerMP sender) {
        if (sender.capabilities.isCreativeMode) {
            return true;
        }

        MinecraftServer minecraftServer = MinecraftServer.getServer();
        ICommandManager commandManager = minecraftServer.getCommandManager();
        Map commands = commandManager.getCommands();
        ICommand giveCommand = (ICommand) commands.get("give");

        if (giveCommand != null && giveCommand.canCommandSenderUseCommand(sender)) {
            return true;
        } else {
            return minecraftServer.getConfigurationManager()
                .func_152596_g(sender.getGameProfile());
        }
    }

    private static void executeGive(EntityPlayer entityplayer, ItemStack itemStack) {
        int originalStackSize = itemStack.stackSize;
        boolean addedToInventory = entityplayer.inventory.addItemStackToInventory(itemStack);

        if (addedToInventory) {
            entityplayer.worldObj.playSoundAtEntity(
                entityplayer,
                "random.pop",
                0.2F,
                ((entityplayer.getRNG()
                    .nextFloat()
                    - entityplayer.getRNG()
                        .nextFloat())
                    * 0.7F + 1.0F) * 2.0F);
            entityplayer.inventoryContainer.detectAndSendChanges();
        }

        if (!addedToInventory || itemStack.stackSize > 0) {
            EntityItem entityitem = entityplayer.dropPlayerItemWithRandomChoice(itemStack, false);
            if (entityitem != null) {
                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(entityplayer.getCommandSenderName());
            }
        }
    }
}
