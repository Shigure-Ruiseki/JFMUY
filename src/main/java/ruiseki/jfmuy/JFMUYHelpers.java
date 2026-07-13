package ruiseki.jfmuy;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.transfer.RecipeTransferHandlerHelper;

public class JFMUYHelpers implements IJFMUYHelpers {

    private final GuiHelper guiHelper;
    private final ItemBlacklist itemBlacklist;
    private final NbtIgnoreList nbtIgnoreList;
    private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

    public JFMUYHelpers() {
        this.guiHelper = new GuiHelper();
        this.itemBlacklist = new ItemBlacklist();
        this.nbtIgnoreList = new NbtIgnoreList();
        this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();
    }

    @Nonnull
    @Override
    public GuiHelper getGuiHelper() {
        return guiHelper;
    }

    @Nonnull
    @Override
    public ItemBlacklist getItemBlacklist() {
        return itemBlacklist;
    }

    @Nonnull
    @Override
    public NbtIgnoreList getNbtIgnoreList() {
        return nbtIgnoreList;
    }

    @Nonnull
    @Override
    public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
        return recipeTransferHandlerHelper;
    }

    @Override
    public void reload() {
        JFMUY.getProxy()
            .restartNEI();
    }
}
