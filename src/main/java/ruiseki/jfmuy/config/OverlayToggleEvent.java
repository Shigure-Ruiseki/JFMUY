package ruiseki.jfmuy.config;

import cpw.mods.fml.common.eventhandler.Event;

public class OverlayToggleEvent extends Event {

    private final boolean overlayEnabled;

    public OverlayToggleEvent(boolean overlayEnabled) {
        this.overlayEnabled = overlayEnabled;
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }
}
