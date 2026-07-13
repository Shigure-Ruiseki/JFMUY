package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.RecipeRegistry;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipe;

public class ModRegistry implements IModRegistry {

    private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
    private final List<IRecipeHandler> recipeHandlers = new ArrayList<>();
    private final List<Object> recipes = new ArrayList<>();
    private final RecipeTransferRegistry recipeTransferRegistry = new RecipeTransferRegistry();

    @Override
    public void addRecipeCategories(IRecipeCategory... recipeCategories) {
        Collections.addAll(this.recipeCategories, recipeCategories);
    }

    @Override
    public void addRecipeHandlers(IRecipeHandler... recipeHandlers) {
        Collections.addAll(this.recipeHandlers, recipeHandlers);
    }

    @Override
    public void addRecipes(List recipes) {
        if (recipes != null) {
            this.recipes.addAll(recipes);
        }
    }

    @Override
    public void addDescription(List<ItemStack> itemStacks, String... descriptionKeys) {
        if (itemStacks == null || itemStacks.size() == 0) {
            IllegalArgumentException e = new IllegalArgumentException();
            Log.error("Tried to add description with no itemStacks.", e);
            return;
        }
        if (descriptionKeys.length == 0) {
            IllegalArgumentException e = new IllegalArgumentException();
            Log.error("Tried to add an empty list of descriptionKeys for itemStacks {}.", itemStacks, e);
            return;
        }
        List<ItemDescriptionRecipe> recipes = ItemDescriptionRecipe.create(itemStacks, descriptionKeys);
        this.recipes.addAll(recipes);
    }

    @Override
    public void addDescription(ItemStack itemStack, String... descriptionKeys) {
        addDescription(Collections.singletonList(itemStack), descriptionKeys);
    }

    @Override
    public IRecipeTransferRegistry getRecipeTransferRegistry() {
        return recipeTransferRegistry;
    }

    @Nonnull
    public RecipeRegistry createRecipeRegistry() {
        List<IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
        return new RecipeRegistry(recipeCategories, recipeHandlers, recipeTransferHandlers, recipes);
    }
}
