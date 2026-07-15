package ruiseki.jfmuy.config;

import cpw.mods.fml.common.eventhandler.Event;

public class BookmarkOverlayToggleEvent extends Event {

    private final boolean bookmarkOverlayEnabled;

    public BookmarkOverlayToggleEvent(boolean bookmarkOverlayEnabled) {
        this.bookmarkOverlayEnabled = bookmarkOverlayEnabled;
    }

    public boolean isBookmarkOverlayEnabled() {
        return bookmarkOverlayEnabled;
    }
}
