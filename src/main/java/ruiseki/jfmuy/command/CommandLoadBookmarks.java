package ruiseki.jfmuy.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.okcore.command.CommandMod;
import ruiseki.okcore.init.ModBase;

public class CommandLoadBookmarks extends CommandMod {

    public CommandLoadBookmarks(ModBase mod) {
        super(mod, "loadBookmarks");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public LiteralArgumentBuilder<ICommandSender> make() {
        return super.make().executes(this::executeLoad);
    }

    @Override
    public int run(CommandContext<ICommandSender> context) {
        ICommandSender sender = context.getSource();
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Usage:"));
        sender.addChatMessage(
            new ChatComponentText(
                EnumChatFormatting.WHITE + "  /" + getMod().getModId() + " loadBookmarks - Load saved bookmarks list"));
        return 1;
    }

    private int executeLoad(CommandContext<ICommandSender> ctx) throws CommandSyntaxException {
        ICommandSender sender = ctx.getSource();

        BookmarkList bookmarkList = Internal.getBookmarkList();
        if (bookmarkList == null) {
            throw new SimpleCommandExceptionType(() -> "jfmuy.command.load_bookmarks.failure").create();
        }

        bookmarkList.loadBookmarks();
        int amount = bookmarkList.size();
        sendLocalizedMessage(sender, "jfmuy.command.load_bookmarks.success", amount);
        return Command.SINGLE_SUCCESS;
    }
}
