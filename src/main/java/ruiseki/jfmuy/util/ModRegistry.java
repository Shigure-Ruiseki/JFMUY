package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;

import ruiseki.jfmuy.JFMUYHelpers;
import ruiseki.jfmuy.RecipeRegistry;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeRegistryPlugin;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.gui.recipes.RecipeClickableArea;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipe;

public class ModRegistry implements IModRegistry {

    private final IJFMUYHelpers jfmuyHelpers;
    private final IIngredientRegistry ingredientRegistry;
    private final List<IRecipeCategory> recipeCategories = new ArrayList<IRecipeCategory>();
    private final List<IRecipeHandler> recipeHandlers = new ArrayList<IRecipeHandler>();
    private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<IAdvancedGuiHandler<?>>();
    private final List<Object> recipes = new ArrayList<Object>();
    private final RecipeTransferRegistry recipeTransferRegistry;
    private final Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = HashMultimap
        .create();
    private final Multimap<String, ItemStack> craftItemsForCategories = ArrayListMultimap.create();
    private final List<IRecipeRegistryPlugin> recipeRegistryPlugins = new ArrayList<IRecipeRegistryPlugin>();

    public ModRegistry(JFMUYHelpers jfmuyHelpers, IIngredientRegistry ingredientRegistry) {
        this.jfmuyHelpers = jfmuyHelpers;
        this.ingredientRegistry = ingredientRegistry;
        this.recipeTransferRegistry = new RecipeTransferRegistry(
            jfmuyHelpers.getStackHelper(),
            jfmuyHelpers.recipeTransferHandlerHelper());
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
    public void addRecipeCategories(@Nullable IRecipeCategory... recipeCategories) {
        if (recipeCategories != null) {
            Collections.addAll(this.recipeCategories, recipeCategories);
        }
    }

    @Override
    public void addRecipeHandlers(@Nullable IRecipeHandler... recipeHandlers) {
        if (recipeHandlers != null) {
            for (IRecipeHandler recipeHandler : recipeHandlers) {
                Preconditions.checkNotNull(recipeHandler.getRecipeClass());
                Preconditions.checkArgument(
                    !recipeHandler.getRecipeClass()
                        .equals(Object.class),
                    "Recipe handlers must handle a specific class, not Object.class");
                this.recipeHandlers.add(recipeHandler);
            }
        }
    }

    @Override
    public void addRecipes(@Nullable List recipes) {
        if (recipes != null) {
            this.recipes.addAll(recipes);
        }
    }

    @Override
    public void addRecipeClickArea(@Nullable Class<? extends GuiContainer> guiClass, int xPos, int yPos, int width,
        int height, @Nullable String... recipeCategoryUids) {
        if (guiClass == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeClickArea with null guiClass.", e);
            return;
        }

        if (recipeCategoryUids == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeClickArea with null recipeCategoryUids.", e);
            return;
        }

        if (recipeCategoryUids.length == 0) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeClickArea with empty list of recipeCategoryUids.", e);
            return;
        }
        RecipeClickableArea recipeClickableArea = new RecipeClickableArea(
            yPos,
            yPos + height,
            xPos,
            xPos + width,
            recipeCategoryUids);
        this.recipeClickableAreas.put(guiClass, recipeClickableArea);
    }

    @Override
    public void addRecipeCategoryCraftingItem(@Nullable ItemStack craftingItem,
        @Nullable String... recipeCategoryUids) {
        if (craftingItem == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeCategoryCraftingItem with null craftingItem.", e);
            return;
        }

        if (craftingItem.getItem() == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeCategoryCraftingItem with null item in the craftingItem.", e);
            return;
        }

        if (recipeCategoryUids == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUids.", e);
            return;
        }

        if (recipeCategoryUids.length == 0) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeCategoryCraftingItem with an empty list of recipeCategoryUids.", e);
            return;
        }

        for (String recipeCategoryUid : recipeCategoryUids) {
            if (recipeCategoryUid == null) {
                IllegalArgumentException e = new IllegalArgumentException();
                Log.error("Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUid.", e);
            } else {
                this.craftItemsForCategories.put(recipeCategoryUid, craftingItem);
            }
        }
    }

    @Override
    public void addAdvancedGuiHandlers(@Nullable IAdvancedGuiHandler<?>... advancedGuiHandlers) {
        if (advancedGuiHandlers != null) {
            Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
        }
    }

    @Override
    public void addDescription(@Nullable List<ItemStack> itemStacks, @Nullable String... descriptionKeys) {
        if (itemStacks == null || itemStacks.size() == 0) {
            IllegalArgumentException e = new IllegalArgumentException();
            Log.error("Tried to add description with no itemStacks.", e);
            return;
        }

        if (descriptionKeys == null) {
            IllegalArgumentException e = new IllegalArgumentException();
            Log.error("Tried to add a null descriptionKey for itemStacks {}.", itemStacks, e);
            return;
        }

        if (descriptionKeys.length == 0) {
            IllegalArgumentException e = new IllegalArgumentException();
            Log.error("Tried to add an empty list of descriptionKeys for itemStacks {}.", itemStacks, e);
            return;
        }

        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();
        List<ItemDescriptionRecipe> recipes = ItemDescriptionRecipe.create(guiHelper, itemStacks, descriptionKeys);
        this.recipes.addAll(recipes);
    }

    @Override
    public void addDescription(@Nullable ItemStack itemStack, @Nullable String... descriptionKeys) {
        addDescription(Collections.singletonList(itemStack), descriptionKeys);
    }

    @Override
    public IRecipeTransferRegistry getRecipeTransferRegistry() {
        return recipeTransferRegistry;
    }

    @Override
    public void addRecipeRegistryPlugin(@Nullable IRecipeRegistryPlugin recipeRegistryPlugin) {
        if (recipeRegistryPlugin == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add null recipeRegistryPlugin.", e);
            return;
        }
        Log.info("Added recipe registry plugin: {}", recipeRegistryPlugin.getClass());
        recipeRegistryPlugins.add(recipeRegistryPlugin);
    }

    public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
        return advancedGuiHandlers;
    }

    public RecipeRegistry createRecipeRegistry(StackHelper stackHelper, IIngredientRegistry ingredientRegistry) {
        ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry
            .getRecipeTransferHandlers();
        return new RecipeRegistry(
            stackHelper,
            recipeCategories,
            recipeHandlers,
            recipeTransferHandlers,
            recipes,
            recipeClickableAreas,
            craftItemsForCategories,
            ingredientRegistry,
            recipeRegistryPlugins);
    }
}
