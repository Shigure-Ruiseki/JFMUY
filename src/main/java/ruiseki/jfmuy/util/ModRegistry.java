package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.jfmuy.RecipeRegistry;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.gui.RecipeClickableArea;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipe;

public class ModRegistry implements IModRegistry {

    @Nonnull
    private final IJFMUYHelpers jfmuyHelpers;
    @Nonnull
    private final IItemRegistry itemRegistry;
    private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
    private final List<IRecipeHandler> recipeHandlers = new ArrayList<>();
    private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<>();
    private final List<Object> recipes = new ArrayList<>();
    private final RecipeTransferRegistry recipeTransferRegistry = new RecipeTransferRegistry();
    private final Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = HashMultimap
        .create();
    private final Multimap<String, ItemStack> craftItemsForCategories = HashMultimap.create();

    public ModRegistry(@Nonnull IJFMUYHelpers jfmuyHelpers, @Nonnull IItemRegistry itemRegistry) {
        this.jfmuyHelpers = jfmuyHelpers;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public @NotNull IJFMUYHelpers getJFMUYHelpers() {
        return jfmuyHelpers;
    }

    @Nonnull
    @Override
    public IItemRegistry getItemRegistry() {
        return itemRegistry;
    }

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
    public void addRecipeClickArea(@Nonnull Class<? extends GuiContainer> guiClass, int xPos, int yPos, int width,
        int height, @Nonnull String... recipeCategoryUids) {
        RecipeClickableArea recipeClickableArea = new RecipeClickableArea(
            yPos,
            yPos + height,
            xPos,
            xPos + width,
            recipeCategoryUids);
        this.recipeClickableAreas.put(guiClass, recipeClickableArea);
    }

    @Override
    public void addRecipeCategoryCraftingItem(@Nullable ItemStack craftingItem, @Nonnull String... recipeCategoryUids) {
        if (craftingItem == null) {
            NullPointerException e = new NullPointerException();
            Log.error("Tried to add a RecipeCategoryCraftingItem with null craftingItem.", e);
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
    public void addAdvancedGuiHandlers(@Nonnull IAdvancedGuiHandler<?>... advancedGuiHandlers) {
        Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
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
    public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
        return advancedGuiHandlers;
    }

    @Nonnull
    public RecipeRegistry createRecipeRegistry() {
        List<IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
        return new RecipeRegistry(
            recipeCategories,
            recipeHandlers,
            recipeTransferHandlers,
            recipes,
            recipeClickableAreas,
            craftItemsForCategories);
    }
}
