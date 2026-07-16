package ruiseki.jfmuy.bookmarks;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;

public class DefaultGhostIngredientHandler implements IGhostIngredientHandler<GuiScreen> {

    @Override
    public <I> List<Target<I>> getTargets(GuiScreen gui, I ingredient, boolean doStart) {
        if (Internal.getBookmarkList()
            .getGroupOrganizer() == null) {
            return Collections.emptyList();
        }
        return Internal.getBookmarkList()
            .getGroupOrganizer()
            .getTargets(ingredient);
    }

    @Override
    public void onComplete() {

    }
}
