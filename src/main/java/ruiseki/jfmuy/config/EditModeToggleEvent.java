package ruiseki.jfmuy.config;

import cpw.mods.fml.common.eventhandler.Event;

public class EditModeToggleEvent extends Event {

    private final boolean editModeEnabled;

    public EditModeToggleEvent(boolean editModeEnabled) {
        this.editModeEnabled = editModeEnabled;
    }

    public boolean isEditModeEnabled() {
        return editModeEnabled;
    }
}
