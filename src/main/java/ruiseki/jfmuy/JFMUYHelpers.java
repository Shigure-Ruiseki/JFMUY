package ruiseki.jfmuy;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.transfer.RecipeTransferHandlerHelper;
import ruiseki.jfmuy.util.StackHelper;

public class JFMUYHelpers implements IJFMUYHelpers {

    private final GuiHelper guiHelper;
    private final StackHelper stackHelper;
    private final IngredientBlacklist ingredientBlacklist;
    private final SubtypeRegistry subtypeRegistry;
    private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

    public JFMUYHelpers(IIngredientRegistry ingredientRegistry, StackHelper stackHelper,
        SubtypeRegistry subtypeRegistry) {
        this.guiHelper = new GuiHelper(stackHelper);
        this.stackHelper = stackHelper;
        this.ingredientBlacklist = new IngredientBlacklist(ingredientRegistry);
        this.subtypeRegistry = subtypeRegistry;
        this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();
    }

    @Override
    public GuiHelper getGuiHelper() {
        return guiHelper;
    }

    @Override
    public StackHelper getStackHelper() {
        return stackHelper;
    }

    @Override
    public IngredientBlacklist getIngredientBlacklist() {
        return ingredientBlacklist;
    }

    @Override
    public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
        return recipeTransferHandlerHelper;
    }
}
