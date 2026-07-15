package ruiseki.jfmuy.runtime;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.ingredients.IngredientBlacklist;
import ruiseki.jfmuy.ingredients.IngredientBlacklistInternal;
import ruiseki.jfmuy.plugins.vanilla.VanillaRecipeFactory;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.transfer.RecipeTransferHandlerHelper;

public class JFMUYHelpers implements IJFMUYHelpers {

    private final GuiHelper guiHelper;
    private final StackHelper stackHelper;
    private final IngredientBlacklist ingredientBlacklist;
    private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;
    private final IVanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory();

    public JFMUYHelpers(GuiHelper guiHelper, IIngredientRegistry ingredientRegistry,
        IngredientBlacklistInternal ingredientBlacklistInternal, StackHelper stackHelper) {
        this.guiHelper = guiHelper;
        this.stackHelper = stackHelper;
        this.ingredientBlacklist = new IngredientBlacklist(ingredientRegistry, ingredientBlacklistInternal);
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

    @Override
    public IVanillaRecipeFactory getVanillaRecipeFactory() {
        return vanillaRecipeFactory;
    }
}
