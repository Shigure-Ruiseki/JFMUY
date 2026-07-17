package ruiseki.jfmuy.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;

import cpw.mods.fml.common.ProgressManager;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.api.gui.IRecipeLayoutDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeRegistryPlugin;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.collect.ListMultiMap;
import ruiseki.jfmuy.collect.SetMultiMap;
import ruiseki.jfmuy.collect.Table;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.recipes.RecipeClickableArea;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.ingredients.Ingredients;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class RecipeRegistry implements IRecipeRegistry {

    private final Object2LongMap<IRecipeWrapper> recipeIds = new Object2LongOpenHashMap<>();
    private final IngredientRegistry ingredientRegistry;
    private final ImmutableList<IRecipeCategory> recipeCategories;
    private final Set<String> hiddenRecipeCategoryUids = new ObjectOpenHashSet<>();
    private final List<IRecipeCategory> recipeCategoriesVisibleCache = new ArrayList<>();
    private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
    private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
    private final ImmutableListMultimap<IRecipeCategory, Object> recipeCatalysts;
    private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
    private final RecipeCategoryComparator recipeCategoryComparator;
    private final Table<String, Object, IRecipeWrapper> wrapperMaps = new Table<>(
        new Object2ObjectOpenHashMap<>(),
        Reference2ObjectOpenHashMap::new);
    private final Table<IRecipeCategory, Long, IRecipeWrapper> recipeWrappersByCategory = Table.hashBasedTable();
    private final ListMultiMap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = new ListMultiMap<>();
    private final RecipeMap recipeInputMap;
    private final RecipeMap recipeOutputMap;
    private final List<RecipeRegistryPluginSafeWrapper> plugins = new ArrayList<>();
    private final SetMultiMap<String, IRecipeWrapper> hiddenRecipes = new SetMultiMap<>(ReferenceOpenHashSet::new);

    public RecipeRegistry(List<IRecipeCategory> recipeCategories,
        ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers,
        ListMultiMap<String, Object> recipes,
        ListMultiMap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
        ListMultiMap<String, Object> recipeCatalysts, IngredientRegistry ingredientRegistry,
        List<IRecipeRegistryPlugin> plugins) {
        this.ingredientRegistry = ingredientRegistry;
        this.recipeTransferHandlers = recipeTransferHandlers;
        this.recipeClickableAreasMap = recipeClickableAreasMap.toImmutable();

        this.recipeCategories = ImmutableList.copyOf(recipeCategories);
        this.recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
        this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
        this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
        this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

        addRecipes(recipes);

        ImmutableListMultimap.Builder<IRecipeCategory, Object> recipeCatalystsBuilder = ImmutableListMultimap.builder();
        ImmutableMultimap.Builder<String, String> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();

        for (Map.Entry<String, List<Object>> recipeCatalystEntry : recipeCatalysts.entrySet()) {
            String recipeCategoryUid = recipeCatalystEntry.getKey();
            IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
            if (recipeCategory != null) {
                Collection<Object> catalystIngredients = recipeCatalystEntry.getValue();
                recipeCatalystsBuilder.putAll(recipeCategory, catalystIngredients);
                for (Object catalystIngredient : catalystIngredients) {
                    IIngredientType ingredientType = ingredientRegistry.getIngredientType(catalystIngredient);
                    @SuppressWarnings("unchecked")
                    IIngredientHelper ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
                    // noinspection unchecked
                    recipeInputMap.addRecipeCategory(recipeCategory, catalystIngredient, ingredientHelper);
                    String catalystIngredientKey = getUniqueId(catalystIngredient);
                    categoriesForRecipeCatalystKeysBuilder.put(catalystIngredientKey, recipeCategoryUid);
                }
            }
        }

        this.recipeCatalysts = recipeCatalystsBuilder.build();
        ImmutableMultimap<String, String> categoriesForRecipeCatalystKeys = categoriesForRecipeCatalystKeysBuilder
            .build();

        IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(
            this,
            categoriesForRecipeCatalystKeys,
            ingredientRegistry,
            recipeCategoriesMap,
            recipeInputMap,
            recipeOutputMap,
            recipeWrappersForCategories);
        this.plugins.add(new RecipeRegistryPluginSafeWrapper(internalRecipeRegistryPlugin));
        for (IRecipeRegistryPlugin plugin : plugins) {
            this.plugins.add(new RecipeRegistryPluginSafeWrapper(plugin));
        }
    }

    @SuppressWarnings("unchecked")
    private long calculateId(IRecipeCategory<?> category, Ingredients ingredients) {
        long hash = 0;
        for (IIngredientType<?> type : Internal.getIngredientRegistry()
            .getCraftableIngredientTypes()) {
            List<Object> ings = ingredients.getInputIngredients()
                .get(type);
            if (ings == null) {
                continue;
            }
            for (Object ingredient : ings) {
                if (ingredient == null) { // Looking at you, Techguns
                    continue;
                }
                hash = (long) this.ingredientRegistry.getIngredientHelper(ingredient)
                    .getHash(ingredient) + hash * 31;
            }
        }
        for (IIngredientType<?> type : Internal.getIngredientRegistry()
            .getCraftableIngredientTypes()) {
            List<Object> ings = ingredients.getOutputIngredients()
                .get(type);
            if (ings == null) {
                continue;
            }
            for (Object ingredient : ings) {
                if (ingredient == null) {
                    continue;
                }
                hash = (long) this.ingredientRegistry.getIngredientHelper(ingredient)
                    .getHash(ingredient) + hash * 31;
            }
        }
        hash = category.getUid()
            .hashCode() + hash * 31;
        return hash;
    }

    private <T> String getUniqueId(T ingredient) {
        IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        return ingredientHelper.getUniqueId(ingredient);
    }

    private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(
        List<IRecipeCategory> recipeCategories) {
        ImmutableMap.Builder<String, IRecipeCategory> mapBuilder = ImmutableMap.builder();
        for (IRecipeCategory recipeCategory : recipeCategories) {
            mapBuilder.put(recipeCategory.getUid(), recipeCategory);
        }
        return mapBuilder.build();
    }

    private void addRecipes(ListMultiMap<String, Object> recipes) {
        Collection<Map.Entry<String, List<Object>>> entries = recipes.entrySet();
        if (Config.skipShowingProgressBar()) {
            for (Map.Entry<String, List<Object>> entry : entries) {
                String recipeCategoryUid = entry.getKey();
                for (Object recipe : entry.getValue()) {
                    if (recipe instanceof IRecipeWrapper recipeWrapper) {
                        IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
                        if (recipeCategory != null) {
                            addRecipe(recipe, recipeWrapper, recipeCategory);
                        }
                    }
                }
            }
        } else {
            ProgressManager.ProgressBar progressBar = ProgressManager.push("Loading recipes", recipes.getTotalSize());
            for (Map.Entry<String, List<Object>> entry : entries) {
                String recipeCategoryUid = entry.getKey();
                for (Object recipe : entry.getValue()) {
                    progressBar.step("");
                    if (recipe instanceof IRecipeWrapper recipeWrapper) {
                        IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
                        if (recipeCategory != null) {
                            addRecipe(recipe, recipeWrapper, recipeCategory);
                        }
                    }
                }
            }
            ProgressManager.pop(progressBar);
        }
    }

    @Override
    public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
        return new Focus<>(mode, ingredient);
    }

    @Override
    @Nullable
    public IRecipeCategory getRecipeCategory(String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
        return recipeCategoriesMap.get(recipeCategoryUid);
    }

    private <T> void addRecipe(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
        try {
            addRecipeUnchecked(recipe, recipeWrapper, recipeCategory);
        } catch (BrokenCraftingRecipeException e) {
            Log.get()
                .error("Found a broken crafting recipe.", e);
        } catch (RuntimeException | LinkageError e) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
            Log.get()
                .error("Found a broken recipe: {}\n", recipeInfo, e);
        }
    }

    private <T> void addRecipeUnchecked(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
        wrapperMaps.put(recipeCategory.getUid(), recipe, recipeWrapper);

        Ingredients ingredients = getIngredients(recipeWrapper);

        // noinspection unchecked
        recipeInputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getInputIngredients());
        // noinspection unchecked
        recipeOutputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getOutputIngredients());

        recipeWrappersForCategories.put(recipeCategory, recipeWrapper);

        long recipeId = calculateId(recipeCategory, ingredients);
        recipeIds.put(recipeWrapper, recipeId);
        recipeWrappersByCategory.put(recipeCategory, recipeId, recipeWrapper);

        unhideRecipe(recipeWrapper, recipeCategory.getUid());

        recipeCategoriesVisibleCache.clear();
    }

    public Ingredients getIngredients(IRecipeWrapper recipeWrapper) {
        Ingredients ingredients = new Ingredients();
        recipeWrapper.getIngredients(ingredients);
        return ingredients;
    }

    private <T> void removeRecipe(T recipe, String recipeCategoryUid) {
        IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
        if (recipeCategory == null) {
            Log.get()
                .error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
            return;
        }

        try {
            IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeCategory.getUid());
            if (recipeWrapper != null) {
                hideRecipe(recipeWrapper, recipeCategoryUid);
            }
        } catch (BrokenCraftingRecipeException e) {
            Log.get()
                .error("Found a broken crafting recipe.", e);
        }
    }

    @Override
    public List<IRecipeCategory> getRecipeCategories() {
        if (recipeCategoriesVisibleCache.isEmpty()) {
            for (IRecipeCategory<?> recipeCategory : this.recipeCategories) {
                if (isCategoryVisible(recipeCategory)) {
                    recipeCategoriesVisibleCache.add(recipeCategory);
                }
            }
        }
        return recipeCategoriesVisibleCache;
    }

    private boolean isCategoryVisible(IRecipeCategory<?> recipeCategory) {
        if (hiddenRecipeCategoryUids.contains(recipeCategory.getUid())) {
            return false;
        }
        List<Object> allCatalysts = getRecipeCatalysts(recipeCategory, true);
        if (!allCatalysts.isEmpty()) {
            List<Object> visibleCatalysts = getRecipeCatalysts(recipeCategory, false);
            if (visibleCatalysts.isEmpty()) {
                return false;
            }
        }
        return !getRecipeWrappers(recipeCategory).isEmpty();
    }

    @Override
    public List<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids) {
        ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");

        List<IRecipeCategory> categories = new ArrayList<>();
        for (String recipeCategoryUid : recipeCategoryUids) {
            IRecipeCategory<?> recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
            if (recipeCategory != null && getRecipeCategories().contains(recipeCategory)) {
                categories.add(recipeCategory);
            }
        }
        Comparator<IRecipeCategory> comparator = Comparator
            .comparing(IRecipeCategory::getUid, recipeCategoryComparator);
        categories.sort(comparator);
        return Collections.unmodifiableList(categories);
    }

    @Nullable
    @Override
    public IRecipeWrapper getRecipeWrapper(Object recipe, String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipe, "recipe");
        ErrorUtil.checkNotEmpty(recipeCategoryUid, "recipeCategoryUid");
        IRecipeWrapper recipeWrapper = wrapperMaps.get(recipeCategoryUid, recipe);
        if (recipeWrapper != null) {
            return recipeWrapper;
        }
        if (recipe instanceof IRecipeWrapper) {
            return (IRecipeWrapper) recipe;
        }
        return null;
    }

    @Nullable
    public RecipeClickableArea getRecipeClickableArea(GuiContainer gui, int mouseX, int mouseY) {
        ImmutableCollection<RecipeClickableArea> recipeClickableAreas = recipeClickableAreasMap.get(gui.getClass());
        for (RecipeClickableArea recipeClickableArea : recipeClickableAreas) {
            if (recipeClickableArea.checkHover(mouseX, mouseY)) {
                return recipeClickableArea;
            }
        }
        return null;
    }

    @Override
    public <V> List<IRecipeCategory> getRecipeCategories(IFocus<V> focus) {
        focus = Focus.check(focus);

        List<String> allRecipeCategoryUids = new ArrayList<>();
        for (IRecipeRegistryPlugin plugin : this.plugins) {
            List<String> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
            for (String recipeCategoryUid : recipeCategoryUids) {
                if (!allRecipeCategoryUids.contains(recipeCategoryUid)) {
                    if (hiddenRecipes.containsKey(recipeCategoryUid)) {
                        IRecipeCategory<?> recipeCategory = getRecipeCategory(recipeCategoryUid);
                        if (recipeCategory != null) {
                            List<?> recipeWrappers = getRecipeWrappers(recipeCategory, focus);
                            if (!recipeWrappers.isEmpty()) {
                                allRecipeCategoryUids.add(recipeCategoryUid);
                            }
                        }
                    } else {
                        allRecipeCategoryUids.add(recipeCategoryUid);
                    }
                }
            }
        }

        return getRecipeCategories(allRecipeCategoryUids);
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
        focus = Focus.check(focus);

        List<T> allRecipeWrappers = new ArrayList<>();
        for (IRecipeRegistryPlugin plugin : this.plugins) {
            List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory, focus);
            allRecipeWrappers.addAll(recipeWrappers);
        }

        @SuppressWarnings("unchecked")
        Set<T> hidden = (Set<T>) hiddenRecipes.get(recipeCategory.getUid());
        allRecipeWrappers.removeAll(hidden);

        return allRecipeWrappers;
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

        List<T> allRecipeWrappers = new ArrayList<>();
        for (IRecipeRegistryPlugin plugin : this.plugins) {
            List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory);
            allRecipeWrappers.addAll(recipeWrappers);
        }

        @SuppressWarnings("unchecked")
        Set<T> hidden = (Set<T>) hiddenRecipes.get(recipeCategory.getUid());
        allRecipeWrappers.removeAll(hidden);

        return allRecipeWrappers;
    }

    public List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory, boolean includeHidden) {
        ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
        ImmutableList<Object> catalysts = recipeCatalysts.get(recipeCategory);
        if (includeHidden) {
            return catalysts;
        }
        List<Object> visibleCatalysts = new ArrayList<>();
        IngredientFilter ingredientFilter = Internal.getIngredientFilter();
        for (Object catalyst : catalysts) {
            if (ingredientRegistry.isIngredientVisible(catalyst, ingredientFilter)) {
                visibleCatalysts.add(catalyst);
            }
        }
        return visibleCatalysts;
    }

    @Override
    public List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory) {
        return getRecipeCatalysts(recipeCategory, false);
    }

    @Override
    @Nullable
    public IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory) {
        ErrorUtil.checkNotNull(container, "container");
        ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

        Class<? extends Container> containerClass = container.getClass();
        IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers
            .get(containerClass, recipeCategory.getUid());
        if (recipeTransferHandler != null) {
            return recipeTransferHandler;
        }

        return recipeTransferHandlers.get(containerClass, Reference.UNIVERSAL_RECIPE_TRANSFER_UID);
    }

    @Override
    public <T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(
        IRecipeCategory<T> recipeCategory, T recipeWrapper, IFocus<?> focus) {
        focus = Focus.check(focus);
        RecipeLayout recipeLayout = RecipeLayout.create(-1, recipeCategory, recipeWrapper, focus, 0, 0);
        Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
        return recipeLayout;
    }

    @Override
    public void hideRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipe, "recipe");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
        ErrorUtil.assertMainThread();
        hiddenRecipes.put(recipeCategoryUid, recipe);
        recipeCategoriesVisibleCache.clear();
    }

    @Override
    public void unhideRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipe, "recipe");
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
        ErrorUtil.assertMainThread();
        hiddenRecipes.remove(recipeCategoryUid, recipe);
        recipeCategoriesVisibleCache.clear();
    }

    @Override
    public void hideRecipeCategory(String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
        ErrorUtil.assertMainThread();
        IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
        if (recipeCategory == null) {
            throw new IllegalArgumentException("Unknown recipe category: " + recipeCategoryUid);
        }
        hiddenRecipeCategoryUids.add(recipeCategoryUid);
        recipeCategoriesVisibleCache.remove(recipeCategory);
    }

    @Override
    public void unhideRecipeCategory(String recipeCategoryUid) {
        ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
        ErrorUtil.assertMainThread();
        if (!recipeCategoriesMap.containsKey(recipeCategoryUid)) {
            throw new IllegalArgumentException("Unknown recipe category: " + recipeCategoryUid);
        }
        hiddenRecipeCategoryUids.remove(recipeCategoryUid);
        recipeCategoriesVisibleCache.clear();
    }

    @Nullable
    public IRecipeWrapper getRecipeById(long id, IRecipeCategory recipeCategory) {
        return recipeWrappersByCategory.get(recipeCategory, id);
    }

    public long getRecipeId(IRecipeWrapper recipe) {
        return recipeIds.getLong(recipe);
    }
}
