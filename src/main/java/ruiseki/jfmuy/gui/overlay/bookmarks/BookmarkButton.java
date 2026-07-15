package ruiseki.jfmuy.gui.overlay.bookmarks;

import java.util.List;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.elements.GuiIconToggleButton;
import ruiseki.jfmuy.util.Translator;

public class BookmarkButton extends GuiIconToggleButton {

    public static BookmarkButton create(BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList,
        GuiHelper guiHelper) {
        IDrawableStatic offIcon = guiHelper.getBookmarkButtonDisabledIcon();
        IDrawableStatic onIcon = guiHelper.getBookmarkButtonEnabledIcon();
        return new BookmarkButton(offIcon, onIcon, bookmarkOverlay, bookmarkList);
    }

    private final BookmarkOverlay bookmarkOverlay;
    private final BookmarkList bookmarkList;

    private BookmarkButton(IDrawable offIcon, IDrawable onIcon, BookmarkOverlay bookmarkOverlay,
        BookmarkList bookmarkList) {
        super(offIcon, onIcon);
        this.bookmarkOverlay = bookmarkOverlay;
        this.bookmarkList = bookmarkList;
    }

    @Override
    protected void getTooltips(List<String> tooltip) {
        tooltip.add(Translator.translateToLocal("jei.tooltip.bookmarks"));
        KeyBinding bookmarkKey = KeyBindings.bookmark;
        if (bookmarkKey.getKeyCode() == 0) {
            tooltip.add(EnumChatFormatting.RED + Translator.translateToLocal("jei.tooltip.bookmarks.usage.nokey"));
        } else if (!bookmarkOverlay.hasRoom()) {
            tooltip
                .add(EnumChatFormatting.GOLD + Translator.translateToLocal("jei.tooltip.bookmarks.not.enough.space"));
        } else {
            tooltip.add(
                EnumChatFormatting.GRAY + Translator
                    .translateToLocalFormatted("jei.tooltip.bookmarks.usage.key", bookmarkKey.getKeyDescription()));
        }
    }

    @Override
    protected boolean isIconToggledOn() {
        return bookmarkOverlay.isListDisplayed();
    }

    @Override
    protected boolean onMouseClicked(int mouseX, int mouseY) {
        if (!bookmarkList.isEmpty() && bookmarkOverlay.hasRoom()) {
            Config.toggleBookmarkEnabled();
            return true;
        }
        return false;
    }
}
