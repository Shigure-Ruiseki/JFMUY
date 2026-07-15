package ruiseki.jfmuy.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;
import ruiseki.jfmuy.api.gui.IGlobalGuiHandler;
import ruiseki.jfmuy.api.gui.IGuiScreenHandler;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeCategoryRegistration;
import ruiseki.jfmuy.api.recipe.IRecipeRegistryPlugin;
import ruiseki.jfmuy.api.recipe.IRecipeWrapperFactory;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.collect.ListMultiMap;
import ruiseki.jfmuy.collect.SetMultiMap;
import ruiseki.jfmuy.gui.recipes.RecipeClickableArea;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.plugins.jfmuy.info.IngredientInfoRecipe;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.recipes.RecipeTransferRegistry;
import ruiseki.jfmuy.runtime.JFMUYHelpers;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class ModRegistry implements IModRegistry, IRecipeCategoryRegistration {

    private final JFMUYHelpers jfmuyHelpers;
    private final IIngredientRegistry ingredientRegistry;
    private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
    private final Set<String> recipeCategoryUids = new HashSet<>();
    private final SetMultiMap<String, Class> recipeHandlerClasses = new SetMultiMap<>();
    private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<>();
    private final List<IGlobalGuiHandler> globalGuiHandlers = new ArrayList<>();
    private final Map<Class, IGuiScreenHandler> guiScreenHandlers = new HashMap<>();
    private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = new HashMap<>();
    private final ListMultiMap<String, Object> recipes = new ListMultiMap<>();
    private final RecipeTransferRegistry recipeTransferRegistry;
    private final ListMultiMap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = new ListMultiMap<>();
    private final ListMultiMap<String, Object> recipeCatalysts = new ListMultiMap<>();
    private final List<IRecipeRegistryPlugin> recipeRegistryPlugins = new ArrayList<>();

    public ModRegistry(JFMUYHelpers jfmuyHelpers, IIngredientRegistry ingredientRegistry) {
        this.jfmuyHelpers = jfmuyHelpers;
        this.ingredientRegistry = ingredientRegistry;
        this.recipeTransferRegistry = new RecipeTransferRegistry(
            jfmuyHelpers.getStackHelper(),
            jfmuyHelpers.recipeTransferHandlerHelper());
    }

    @Override
    public void addRecipeCategories(IRecipeCategory... recipeCategories) {
        ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

        for (IRecipeCategory recipeCategory : recipeCategories) {
            String uid = recipeCategory.getUid();
            Preconditions.checkNotNull(uid, "Recipe category UID cannot be null %s", recipeCategory);
            if (!recipeCategoryUids.add(uid)) {
                throw new IllegalArgumentException("A RecipeCategory with UID \"" + uid + "\" has already been registered.");
            }
        }

        Collections.addAll(this.recipeCategories, recipeCategories);
    }


    @Override
    public IJFMUYHelpers getJFMUYHelpers() {
        return jfmuyHelpers;
    }

    @Override
    public IIngredientRegistry getIngredientRegistry() {
        return ingredientRegistry;
    }

    @Override
    public void addRecipes(Collection<?> recipes, String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipes, "recipes");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

        if (!this.recipeCategoryUids.contains(recipeCategoryUid)) {
            Log.get()
                .warn("No recipe category has been registered for recipeCategoryUid {}", recipeCategoryUid);
        }

        for (Object recipe : recipes) {
            ErrorUtil.checkNotNull(recipe, "recipe");
            this.recipes.put(recipeCategoryUid, recipe);
        }
    }

    @Override
    public <T> void handleRecipes(final Class<T> recipeClass, final IRecipeWrapperFactory<T> recipeWrapperFactory,
        final String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipeClass, "recipeClass");
        Preconditions.checkArgument(
            !recipeClass.equals(Object.class),
            "Recipe handlers must handle a specific class, not Object.class");
        ErrorUtil.checkNotNull(recipeWrapperFactory, "recipeWrapperFactory");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

        if (this.recipeHandlerClasses.contains(recipeCategoryUid, recipeClass)) {
            Log.get()
                .error(
                    "A Recipe Handler has already been registered for '{}': {}",
                    recipeCategoryUid,
                    recipeClass.getName());
        } else {
            this.recipeHandlerClasses.put(recipeCategoryUid, recipeClass);
        }
    }

    @Override
    public void addRecipeClickArea(Class<? extends GuiContainer> guiContainerClass, int xPos, int yPos, int width,
        int height, String... recipeCategoryUids) {
        ErrorUtil.checkNotNull(guiContainerClass, "guiContainerClass");
        ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

        RecipeClickableArea recipeClickableArea = new RecipeClickableArea(
            yPos,
            yPos + height,
            xPos,
            xPos + width,
            recipeCategoryUids);
        this.recipeClickableAreas.put(guiContainerClass, recipeClickableArea);
    }

    @Override
    public void addRecipeCatalyst(Object catalystIngredient, String... recipeCategoryUids) {
        ErrorUtil.checkIsValidIngredient(catalystIngredient, "catalystIngredient");
        ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

        for (String recipeCategoryUid : recipeCategoryUids) {
            ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
            this.recipeCatalysts.put(recipeCategoryUid, catalystIngredient);
        }
    }

    @Override
    public void addAdvancedGuiHandlers(IAdvancedGuiHandler<?>... advancedGuiHandlers) {
        ErrorUtil.checkNotEmpty(advancedGuiHandlers, "advancedGuiHandlers");

        Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
    }

    @Override
    public void addGlobalGuiHandlers(IGlobalGuiHandler... globalGuiHandlers) {
        ErrorUtil.checkNotEmpty(globalGuiHandlers, "globalGuiHandlers");
        Collections.addAll(this.globalGuiHandlers, globalGuiHandlers);
    }

    @Override
    public <T extends GuiScreen> void addGuiScreenHandler(Class<T> guiClass, IGuiScreenHandler<T> handler) {
        ErrorUtil.checkNotNull(guiClass, "guiClass");
        Preconditions.checkArgument(GuiScreen.class.isAssignableFrom(guiClass), "guiClass must inherit from GuiScreen");
        Preconditions.checkArgument(
            !GuiScreen.class.equals(guiClass),
            "you cannot add a handler for GuiScreen, only a subclass.");
        ErrorUtil.checkNotNull(handler, "guiScreenHandler");
        this.guiScreenHandlers.put(guiClass, handler);
    }

    private static final List<Class<? extends GuiScreen>> ghostIngredientGuiBlacklist = ImmutableList
        .of(GuiScreen.class, GuiInventory.class, GuiContainerCreative.class);

    @Override
    public <T extends GuiScreen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler) {
        ErrorUtil.checkNotNull(guiClass, "guiClass");
        Preconditions.checkArgument(GuiScreen.class.isAssignableFrom(guiClass), "guiClass must inherit from GuiScreen");
        Preconditions.checkArgument(
            !ghostIngredientGuiBlacklist.contains(guiClass),
            "you cannot add a ghost ingredient handler for the following Guis, it would interfere with using JEI: %s",
            ghostIngredientGuiBlacklist);
        ErrorUtil.checkNotNull(handler, "handler");
        this.ghostIngredientHandlers.put(guiClass, handler);
    }

    @Override
    public <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, String... descriptionKeys) {
        ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
        ErrorUtil.checkNotNull(ingredientType, "ingredientType");
        ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

        addIngredientInfo(Collections.singletonList(ingredient), ingredientType, descriptionKeys);
    }

    @Override
    public <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType,
        String... descriptionKeys) {
        ErrorUtil.checkNotEmpty(ingredients, "ingredients");
        for (Object ingredient : ingredients) {
            ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
        }
        ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();
        List<IngredientInfoRecipe<T>> recipes = IngredientInfoRecipe
            .create(guiHelper, ingredients, ingredientType, descriptionKeys);
        addRecipes(recipes, VanillaRecipeCategoryUid.INFORMATION);
    }

    @Override
    public IRecipeTransferRegistry getRecipeTransferRegistry() {
        return recipeTransferRegistry;
    }

    @Override
    public void addRecipeRegistryPlugin(IRecipeRegistryPlugin recipeRegistryPlugin) {
        ErrorUtil.checkNotNull(recipeRegistryPlugin, "recipeRegistryPlugin");

        Log.get()
            .info("Added recipe registry plugin: {}", recipeRegistryPlugin.getClass());
        recipeRegistryPlugins.add(recipeRegistryPlugin);
    }

    public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
        return advancedGuiHandlers;
    }

    public List<IGlobalGuiHandler> getGlobalGuiHandlers() {
        return globalGuiHandlers;
    }

    public Map<Class, IGuiScreenHandler> getGuiScreenHandlers() {
        return guiScreenHandlers;
    }

    public Map<Class, IGhostIngredientHandler> getGhostIngredientHandlers() {
        return ghostIngredientHandlers;
    }

    public RecipeRegistry createRecipeRegistry(IngredientRegistry ingredientRegistry) {
        ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry
            .getRecipeTransferHandlers();
        return new RecipeRegistry(
            recipeCategories,
            recipeTransferHandlers,
            recipes,
            recipeClickableAreas,
            recipeCatalysts,
            ingredientRegistry,
            recipeRegistryPlugins);
    }
}
