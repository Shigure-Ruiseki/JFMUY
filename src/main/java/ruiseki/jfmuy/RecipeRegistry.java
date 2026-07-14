package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.ProgressManager;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.api.gui.IRecipeLayoutDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeRegistryPlugin;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.recipes.RecipeClickableArea;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.plugins.vanilla.furnace.SmeltingRecipe;
import ruiseki.jfmuy.util.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Ingredients;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.RecipeCategoryComparator;
import ruiseki.jfmuy.util.RecipeMap;
import ruiseki.jfmuy.util.StackHelper;

public class RecipeRegistry implements IRecipeRegistry {

    private final StackHelper stackHelper;
    private final IIngredientRegistry ingredientRegistry;
    private final ImmutableList<IRecipeHandler> recipeHandlers;
    private final ImmutableList<IRecipeCategory> recipeCategories;
    private final Set<IRecipeCategory> emptyRecipeCategories = new HashSet<IRecipeCategory>();
    private final Set<IRecipeCategory> checkIfEmptyRecipeCategories = new HashSet<IRecipeCategory>();
    private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
    private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
    private final ImmutableListMultimap<IRecipeCategory, ItemStack> craftItemsForCategories;
    private final ImmutableMultimap<String, String> categoriesForCraftItemKeys;
    private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
    private final Map<Object, IRecipeWrapper> wrapperMap = new IdentityHashMap<>(); // used when removing recipes
    private final ListMultimap<IRecipeCategory, Object> recipesForCategories = ArrayListMultimap.create();
    private final ListMultimap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = ArrayListMultimap
        .create();
    private final RecipeMap recipeInputMap;
    private final RecipeMap recipeOutputMap;
    private final Set<Class> unhandledRecipeClasses = new HashSet<Class>();
    private final List<IRecipeRegistryPlugin> plugins = new ArrayList<IRecipeRegistryPlugin>();

    public RecipeRegistry(StackHelper stackHelper, List<IRecipeCategory> recipeCategories,
        List<IRecipeHandler> recipeHandlers,
        ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers, List<Object> recipes,
        Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
        Multimap<String, ItemStack> craftItemsForCategories, IIngredientRegistry ingredientRegistry,
        List<IRecipeRegistryPlugin> plugins) {
        this.stackHelper = stackHelper;
        this.ingredientRegistry = ingredientRegistry;
        this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
        this.recipeTransferHandlers = recipeTransferHandlers;
        this.recipeHandlers = buildRecipeHandlersList(recipeHandlers);
        this.recipeClickableAreasMap = ImmutableMultimap.copyOf(recipeClickableAreasMap);

        RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
        this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
        this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

        addRecipes(recipes);

        ImmutableListMultimap.Builder<IRecipeCategory, ItemStack> craftItemsForCategoriesBuilder = ImmutableListMultimap
            .builder();
        ImmutableMultimap.Builder<String, String> categoriesForCraftItemKeysBuilder = ImmutableMultimap.builder();

        IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
        for (Map.Entry<String, Collection<ItemStack>> recipeCategoryEntry : craftItemsForCategories.asMap()
            .entrySet()) {
            String recipeCategoryUid = recipeCategoryEntry.getKey();
            IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
            if (recipeCategory != null) {
                Collection<ItemStack> craftItems = recipeCategoryEntry.getValue();
                craftItemsForCategoriesBuilder.putAll(recipeCategory, craftItems);
                for (ItemStack craftItem : craftItems) {
                    recipeInputMap.addRecipeCategory(recipeCategory, craftItem);
                    String craftItemKey = ingredientHelper.getUniqueId(craftItem);
                    categoriesForCraftItemKeysBuilder.put(craftItemKey, recipeCategoryUid);
                }
            }
        }

        this.craftItemsForCategories = craftItemsForCategoriesBuilder.build();
        this.categoriesForCraftItemKeys = categoriesForCraftItemKeysBuilder.build();

        IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(
            this,
            categoriesForCraftItemKeys,
            ingredientRegistry,
            recipeCategoriesMap,
            recipeInputMap,
            recipeOutputMap,
            recipeWrappersForCategories);
        this.plugins.add(internalRecipeRegistryPlugin);
        this.plugins.addAll(plugins);

        for (IRecipeCategory recipeCategory : recipeCategories) {
            List recipeWrappers = getRecipeWrappers(recipeCategory);
            if (recipeWrappers.isEmpty()) {
                this.emptyRecipeCategories.add(recipeCategory);
            }
        }
        this.recipeCategories = ImmutableList.copyOf(recipeCategories);
    }

    private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(
        List<IRecipeCategory> recipeCategories) {
        ImmutableMap.Builder<String, IRecipeCategory> mapBuilder = ImmutableMap.builder();
        for (IRecipeCategory recipeCategory : recipeCategories) {
            mapBuilder.put(recipeCategory.getUid(), recipeCategory);
        }
        return mapBuilder.build();
    }

    private static ImmutableList<IRecipeHandler> buildRecipeHandlersList(List<IRecipeHandler> recipeHandlers) {
        ImmutableList.Builder<IRecipeHandler> listBuilder = ImmutableList.builder();
        Set<Class> recipeHandlerClasses = new HashSet<Class>();
        for (IRecipeHandler recipeHandler : recipeHandlers) {
            if (recipeHandler == null) {
                continue;
            }

            Class recipeClass;
            try {
                recipeClass = recipeHandler.getRecipeClass();
            } catch (RuntimeException e) {
                Log.error("Recipe handler crashed.", e);
                continue;
            } catch (LinkageError e) {
                Log.error("Recipe handler crashed.", e);
                continue;
            }

            if (recipeHandlerClasses.contains(recipeClass)) {
                Log.error(
                    "A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
                continue;
            }

            recipeHandlerClasses.add(recipeClass);
            listBuilder.add(recipeHandler);
        }
        return listBuilder.build();
    }

    private void addRecipes(@Nullable List<Object> recipes) {
        if (recipes == null) {
            return;
        }

        ProgressManager.ProgressBar progressBar = ProgressManager.push("Adding recipes", recipes.size());
        for (Object recipe : recipes) {
            progressBar.step("");
            addRecipe(recipe);
        }
        ProgressManager.pop(progressBar);
    }

    @Override
    public <V> IFocus<V> createFocus(IFocus.Mode mode, @Nullable V ingredient) {
        return new Focus<>(mode, ingredient);
    }

    @Override
    public void addRecipe(@Nullable Object recipe) {
        if (recipe == null) {
            Log.error("Null recipe", new NullPointerException());
            return;
        }

        addRecipe(recipe, recipe.getClass());
    }

    private <T> void addRecipe(T recipe, Class<? extends T> recipeClass) {
        IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass);
        if (recipeHandler == null) {
            if (!unhandledRecipeClasses.contains(recipeClass)) {
                unhandledRecipeClasses.add(recipeClass);
                if (Config.isDebugModeEnabled()) {
                    Log.debug("Can't handle recipe: {}", recipeClass);
                }
            }
            return;
        }

        String recipeCategoryUid = "";
        try {
            recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
        } catch (AbstractMethodError ignored) { // legacy handlers don't have that method

        }

        IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
        if (recipeCategory == null) {
            Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
            return;
        }

        try {
            if (!recipeHandler.isRecipeValid(recipe)) {
                return;
            }
        } catch (RuntimeException e) {
            Log.error("Recipe check crashed", e);
            return;
        } catch (LinkageError e) {
            Log.error("Recipe check crashed", e);
            return;
        }

        try {
            addRecipeUnchecked(recipe, recipeCategory, recipeHandler);
        } catch (BrokenCraftingRecipeException e) {
            Log.error("Found a broken crafting recipe.", e);
        } catch (RuntimeException e) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeHandler);

            // suppress the null item in stack exception, that information is redundant here.
            String errorMessage = e.getMessage();
            if (StackHelper.nullItemInStack.equals(errorMessage)) {
                Log.error("Found a broken recipe: {}\n", recipeInfo);
            } else {
                Log.error("Found a broken recipe: {}\n", recipeInfo, e);
            }
        } catch (LinkageError e) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeHandler);

            // suppress the null item in stack exception, that information is redundant here.
            String errorMessage = e.getMessage();
            if (StackHelper.nullItemInStack.equals(errorMessage)) {
                Log.error("Found a broken recipe: {}\n", recipeInfo);
            } else {
                Log.error("Found a broken recipe: {}\n", recipeInfo, e);
            }
        }
    }

    private <T> void addRecipeUnchecked(T recipe, IRecipeCategory recipeCategory, IRecipeHandler<T> recipeHandler) {
        IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
        wrapperMap.put(recipe, recipeWrapper);

        Ingredients ingredients = getIngredients(recipeWrapper);

        recipeInputMap.addRecipe(recipe, recipeWrapper, recipeCategory, ingredients.getInputIngredients());
        recipeOutputMap.addRecipe(recipe, recipeWrapper, recipeCategory, ingredients.getOutputIngredients());

        recipesForCategories.put(recipeCategory, recipe);
        recipeWrappersForCategories.put(recipeCategory, recipeWrapper);

        if (emptyRecipeCategories.contains(recipeCategory)) {
            emptyRecipeCategories.remove(recipeCategory);
        }
    }

    public Ingredients getIngredients(IRecipeWrapper recipeWrapper) {
        Ingredients ingredients = new Ingredients();
        try {
            recipeWrapper.getIngredients(ingredients);
        } catch (AbstractMethodError ignored) {
            // older recipe wrappers do not support getIngredients
        }

        return ingredients;
    }

    @Override
    public void removeRecipe(Object recipe) {
        Preconditions.checkNotNull(recipe, "Null recipe");

        removeRecipe(recipe, recipe.getClass());
    }

    private <T> void removeRecipe(T recipe, Class<? extends T> recipeClass) {
        IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass);
        if (recipeHandler == null) {
            if (!unhandledRecipeClasses.contains(recipeClass)) {
                unhandledRecipeClasses.add(recipeClass);
                if (Config.isDebugModeEnabled()) {
                    Log.debug("Can't handle recipe: {}", recipeClass);
                }
            }
            return;
        }

        String recipeCategoryUid = "";
        try {
            recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
        } catch (AbstractMethodError ignored) { // legacy handlers don't have that method

        }

        IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
        if (recipeCategory == null) {
            Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
            return;
        }

        try {
            removeRecipeUnchecked(recipe, recipeCategory);
        } catch (BrokenCraftingRecipeException e) {
            Log.error("Found a broken crafting recipe.", e);
        } catch (RuntimeException e) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeHandler);

            // suppress the null item in stack exception, that information is redundant here.
            String errorMessage = e.getMessage();
            if (StackHelper.nullItemInStack.equals(errorMessage)) {
                Log.error("Found a broken recipe: {}\n", recipeInfo);
            } else {
                Log.error("Found a broken recipe: {}\n", recipeInfo, e);
            }
        } catch (LinkageError e) {
            String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeHandler);

            // suppress the null item in stack exception, that information is redundant here.
            String errorMessage = e.getMessage();
            if (StackHelper.nullItemInStack.equals(errorMessage)) {
                Log.error("Found a broken recipe: {}\n", recipeInfo);
            } else {
                Log.error("Found a broken recipe: {}\n", recipeInfo, e);
            }
        }
    }

    private <T> void removeRecipeUnchecked(T recipe, IRecipeCategory recipeCategory) {
        IRecipeWrapper recipeWrapper = wrapperMap.remove(recipe);
        if (recipeWrapper != null) {
            Ingredients ingredients = getIngredients(recipeWrapper);

            recipeInputMap.removeRecipe(recipe, recipeWrapper, recipeCategory, ingredients.getInputIngredients());
            recipeOutputMap.removeRecipe(recipe, recipeWrapper, recipeCategory, ingredients.getOutputIngredients());

            recipesForCategories.remove(recipeCategory, recipe);
            recipeWrappersForCategories.remove(recipeCategory, recipeWrapper);

            checkIfEmptyRecipeCategories.add(recipeCategory);
        }
    }

    @Override
    public void addSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
        Preconditions.checkNotNull(inputs, "null inputs");
        Preconditions.checkArgument(!inputs.isEmpty(), "empty inputs");
        Preconditions.checkNotNull(output, "null output");

        SmeltingRecipe smeltingRecipe = new SmeltingRecipe(inputs, output);
        addRecipe(smeltingRecipe);
    }

    @Override
    public List<IRecipeCategory> getRecipeCategories() {
        for (IRecipeCategory recipeCategory : this.checkIfEmptyRecipeCategories) {
            if (getRecipeWrappers(recipeCategory).isEmpty()) {
                this.emptyRecipeCategories.add(recipeCategory);
            }
        }
        this.checkIfEmptyRecipeCategories.clear();

        List<IRecipeCategory> recipeCategories = new ArrayList<IRecipeCategory>(this.recipeCategories);
        recipeCategories.removeAll(this.emptyRecipeCategories);
        return recipeCategories;
    }

    @Override
    public ImmutableList<IRecipeCategory> getRecipeCategories(@Nullable List<String> recipeCategoryUids) {
        if (recipeCategoryUids == null) {
            Log.error("Null recipeCategoryUids", new NullPointerException());
            return ImmutableList.of();
        }

        Set<String> uniqueUids = new HashSet<String>();
        ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
        for (String recipeCategoryUid : recipeCategoryUids) {
            if (!uniqueUids.contains(recipeCategoryUid)) {
                uniqueUids.add(recipeCategoryUid);
                IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
                if (recipeCategory != null && !getRecipeWrappers(recipeCategory).isEmpty()) {
                    builder.add(recipeCategory);
                }
            }
        }
        return builder.build();
    }

    @Nullable
    @Override
    public <T> IRecipeHandler<T> getRecipeHandler(@Nullable Class<? extends T> recipeClass) {
        if (recipeClass == null) {
            Log.error("Null recipeClass", new NullPointerException());
            return null;
        }

        // first try to find the exact handler for this recipeClass
        for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
            if (recipeHandler.getRecipeClass()
                .equals(recipeClass)) {
                // noinspection unchecked
                return (IRecipeHandler<T>) recipeHandler;
            }
        }

        // fall back on any handler that can accept this recipeClass
        for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
            if (recipeHandler.getRecipeClass()
                .isAssignableFrom(recipeClass)) {
                // noinspection unchecked
                return (IRecipeHandler<T>) recipeHandler;
            }
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

    /**
     * Special case for ItemBlocks containing fluid blocks.
     * Nothing crafts those, the player probably wants to look up fluids.
     */
    @Nullable
    private static FluidStack getFluidFromItemBlock(IFocus<?> focus) {
        if (focus.getMode() == IFocus.Mode.NONE) {
            return null;
        }

        Object ingredient = focus.getValue();
        if (ingredient instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) ingredient;
            Item item = itemStack.getItem();
            if (item instanceof ItemBlock) {
                Block block = ((ItemBlock) item).field_150939_a;
                Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
                if (fluid != null) {
                    return new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
                }
            }
        }

        return null;
    }

    @Override
    public <V> List<IRecipeCategory> getRecipeCategories(@Nullable IFocus<V> focus) {
        if (focus == null) {
            Log.error("Null focus", new NullPointerException());
            return ImmutableList.of();
        }

        FluidStack fluidStack = getFluidFromItemBlock(focus);
        if (fluidStack != null) {
            return getRecipeCategories(createFocus(focus.getMode(), fluidStack));
        }

        List<String> allRecipeCategoryUids = new ArrayList<String>();
        for (IRecipeRegistryPlugin plugin : this.plugins) {
            long start_time = System.currentTimeMillis();
            List<String> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
            long timeElapsed = System.currentTimeMillis() - start_time;
            if (timeElapsed > 10) {
                Log.warning("Recipe Category lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
            }
            allRecipeCategoryUids.addAll(recipeCategoryUids);
        }

        return getRecipeCategories(allRecipeCategoryUids);
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(@Nullable IRecipeCategory<T> recipeCategory,
        @Nullable IFocus<V> focus) {
        if (recipeCategory == null) {
            Log.error("Null recipeCategory", new NullPointerException());
            return ImmutableList.of();
        }

        if (focus == null) {
            Log.error("Null focus", new NullPointerException());
            return ImmutableList.of();
        }

        FluidStack fluidStack = getFluidFromItemBlock(focus);
        if (fluidStack != null) {
            return getRecipeWrappers(recipeCategory, createFocus(focus.getMode(), fluidStack));
        }

        List<T> allRecipeWrappers = new ArrayList<T>();
        for (IRecipeRegistryPlugin plugin : this.plugins) {
            long start_time = System.currentTimeMillis();
            List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory, focus);
            long timeElapsed = System.currentTimeMillis() - start_time;
            if (timeElapsed > 10) {
                Log.warning("Recipe Wrapper lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
            }
            allRecipeWrappers.addAll(recipeWrappers);
        }

        return allRecipeWrappers;
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(@Nullable IRecipeCategory<T> recipeCategory) {
        return getRecipeWrappers(recipeCategory, createFocus(IFocus.Mode.NONE, null));
    }

    @Override
    public List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory, IFocus focus) {
        List<ItemStack> craftingItems = craftItemsForCategories.get(recipeCategory);
        Object ingredient = focus.getValue();
        if (ingredient instanceof ItemStack && focus.getMode() == IFocus.Mode.INPUT) {
            ItemStack itemStack = (ItemStack) ingredient;
            IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
            ItemStack matchingStack = ingredientHelper.getMatch(craftingItems, itemStack);
            if (matchingStack != null) {
                return Collections.singletonList(matchingStack);
            }
        }
        return craftingItems;
    }

    @Nullable
    public IRecipeTransferHandler getRecipeTransferHandler(@Nullable Container container,
        @Nullable IRecipeCategory recipeCategory) {
        if (container == null) {
            Log.error("Null container", new NullPointerException());
            return null;
        } else if (recipeCategory == null) {
            Log.error("Null recipeCategory", new NullPointerException());
            return null;
        }

        Class<? extends Container> containerClass = container.getClass();
        IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers
            .get(containerClass, recipeCategory.getUid());
        if (recipeTransferHandler != null) {
            return recipeTransferHandler;
        }

        return recipeTransferHandlers.get(containerClass, Reference.UNIVERSAL_RECIPE_TRANSFER_UID);
    }

    @Nullable
    @Override
    public <T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(
        IRecipeCategory<T> recipeCategory, T recipeWrapper, IFocus focus) {
        return new RecipeLayout(-1, recipeCategory, recipeWrapper, focus, 0, 0);
    }
}
