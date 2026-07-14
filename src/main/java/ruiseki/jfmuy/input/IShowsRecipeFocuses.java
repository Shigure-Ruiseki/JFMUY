package ruiseki.jfmuy.input;

import javax.annotation.Nullable;

import ruiseki.jfmuy.gui.Focus;

public interface IShowsRecipeFocuses {

    @Nullable
    Focus getFocusUnderMouse(int mouseX, int mouseY);

    boolean canSetFocusWithMouse();

}
