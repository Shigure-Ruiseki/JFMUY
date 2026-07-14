package ruiseki.jfmuy;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ISubtypeRegistry;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.transfer.RecipeTransferHandlerHelper;
import ruiseki.jfmuy.util.StackHelper;

public class JFMUYHelpers implements IJFMUYHelpers {

    @Nonnull
    private final GuiHelper guiHelper;
    @Nonnull
    private final StackHelper stackHelper;
    @Nonnull
    private final ItemBlacklist itemBlacklist;
    @Nonnull
    private final SubtypeRegistry subtypeRegistry;
    @Nonnull
    private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

    public JFMUYHelpers() {
        this.guiHelper = new GuiHelper();
        this.stackHelper = new StackHelper();
        this.itemBlacklist = new ItemBlacklist();
        this.subtypeRegistry = new SubtypeRegistry();
        this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();
    }

    @Nonnull
    @Override
    public GuiHelper getGuiHelper() {
        return guiHelper;
    }

    @Nonnull
    @Override
    public StackHelper getStackHelper() {
        return stackHelper;
    }

    @Nonnull
    @Override
    public ItemBlacklist getItemBlacklist() {
        return itemBlacklist;
    }

    @Override
    public ISubtypeRegistry getSubtypeRegistry() {
        return subtypeRegistry;
    }

    @Nonnull
    @Override
    public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
        return recipeTransferHandlerHelper;
    }

    @Override
    public void reload() {
        JFMUY.getProxy()
            .restartJFMUY();
    }
}
