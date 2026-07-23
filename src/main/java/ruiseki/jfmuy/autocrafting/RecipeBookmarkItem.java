package ruiseki.jfmuy.autocrafting;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.jfmuy.autocrafting.favorites.FavoriteRecipes;
import ruiseki.jfmuy.bookmarks.BookmarkGroup;
import ruiseki.jfmuy.bookmarks.BookmarkItem;
import ruiseki.jfmuy.bookmarks.DummyBookmarkItem;
import ruiseki.jfmuy.gui.recipes.RecipeLayout;
import ruiseki.jfmuy.ingredients.Ingredients;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapelessRecipesWrapper;

public class RecipeBookmarkItem<I> extends BookmarkItem<I> {

    // How much of this item is produced by its recipe.
    public long outputAmount = 0L;
    // How much of this item the player requested apart from the recipe chain.
    public long selfOutputAmount = 0L;
    public IRecipeWrapper recipe;
    public IRecipeCategory<?> category;
    public List<RecipeBookmarkItem<?>> inputs;
    public BookmarkItem<?> secondaryTo;
    // These are possible ingredients, which are helpful for OreDictionary.
    public List<I> aliases;
    public boolean foundAliases = false;
    public boolean reusableInCrafting = false;
    private List<DummyBookmarkItem<?>> inputDummyItems; // Cached for performance

    public RecipeBookmarkItem(I ingredient) {
        super(ingredient);
        this.aliases = new ObjectArrayList<>();
        this.aliases.add(this.ingredient);
    }

    public RecipeBookmarkItem(List<I> aliases) {
        super(aliases.get(0));
        this.aliases = aliases;
        this.aliases.set(0, this.ingredient); // In case it needed to be normalized.
    }

    public RecipeBookmarkItem(List<I> aliases, int amount) {
        this(aliases, amount, false);
    }

    public RecipeBookmarkItem(List<I> aliases, int amount, boolean reusableInCrafting) {
        super(aliases.get(0));
        this.aliases = aliases;
        this.aliases.set(0, this.ingredient); // In case it needed to be normalized.
        this.amount = amount;
        this.reusableInCrafting = reusableInCrafting;
    }

    public RecipeBookmarkItem(RecipeBookmarkItem<I> other) {
        super(other.ingredient);
        this.aliases = other.aliases;
        this.foundAliases = other.foundAliases;
        this.reusableInCrafting = other.reusableInCrafting;
        this.amount = other.amount;
        this.outputAmount = other.outputAmount;
        this.selfOutputAmount = other.selfOutputAmount;
        this.recipe = other.recipe;
        this.category = other.category;
        this.inputs = other.inputs;
        this.secondaryTo = other.secondaryTo;
    }

    public void populateWithFavorite() {
        if (recipe != null) {
            return;
        }
        for (I alias : aliases) {
            IRecipeWrapper favorite = FavoriteRecipes.getFavorite(alias);
            IRecipeCategory<?> favoriteCategory = null;
            if (favorite == null) {
                // Find recipe from other bookmarked recipes
                RecipeBookmarkItem<?> found = (RecipeBookmarkItem<?>) Internal.getBookmarkList()
                    .getBookmarkGroupsInternal()
                    .stream()
                    .map(BookmarkGroup::getItemsInternal)
                    .flatMap(Collection::stream)
                    .filter(
                        item -> item instanceof RecipeBookmarkItem && item != this
                            && ((RecipeBookmarkItem<?>) item).recipe != null
                            && IngredientUtil.aliasesContains(((RecipeBookmarkItem<?>) item).aliases, alias))
                    .findFirst()
                    .orElse(null);
                if (found != null) {
                    favorite = found.recipe;
                    favoriteCategory = found.category;
                }
            } else {
                favoriteCategory = FavoriteRecipes.getFavoriteCategory(alias);
            }
            if (favorite != null) {
                this.ingredient = alias;
                populateWith(favorite, favoriteCategory);
                return;
            }
        }
    }

    public void populateWith(IRecipeWrapper recipe, IRecipeCategory<?> category) {
        if (!Internal.getIngredientRegistry()
            .isIngredientCraftable(this.ingredient)) {
            return;
        }
        this.recipe = recipe;
        this.category = category;
        Ingredients ingredients = new Ingredients();
        this.recipe.getIngredients(ingredients);
        inputs = new ObjectArrayList<>();
        for (IIngredientType<?> type : ingredients.getInputIngredients()
            .keySet()) {
            List<List> typeInputs = (List) ingredients.getInputs(type);
            List<Boolean> reusableInputs = type == VanillaTypes.ITEM ? getReusableInputs((List) typeInputs) : null;
            populateInputType(typeInputs, reusableInputs);
        }
        this.outputAmount = 0L;
        for (Object other : ingredients.getOutputIngredients()
            .get(
                Internal.getIngredientRegistry()
                    .getIngredientType(ingredient))) {
            if (IngredientUtil.equals(ingredient, other)) {
                this.outputAmount += IngredientUtil.getCount(other);
            }
        }

        inputDummyItems = inputs.stream()
            .map((input) -> {
                final long initialSize = input.amount;
                return new DummyBookmarkItem<>(input.aliases.get(0), getGroup(), () -> {
                    long amount = initialSize * getMultiplier();
                    if (input.reusableInCrafting && amount > 1) {
                        return 1L;
                    }
                    return amount;
                });
            })
            .collect(Collectors.toList());
    }

    public void populateSelf(RecipeChain chain) {
        populateWith(recipe, category);
        RecipeBookmarkItem<?> possibleSecondary = chain.findOutputWithSameRecipe(this);
        if (possibleSecondary != null) {
            secondaryTo = possibleSecondary;
        }
    }

    private void populateInputType(List<List> typeInputs, List<Boolean> reusableInputs) {
        int typeSize = typeInputs.size();
        boolean[] seen = new boolean[typeSize];
        for (int i = 0; i < typeSize; i++) {
            if (seen[i]) {
                continue;
            }
            List inputAliases = removeNulls(typeInputs.get(i));
            if (inputAliases.isEmpty()) {
                continue;
            }
            boolean reusable = checkReusableInputStatus(reusableInputs, i);
            int count = IngredientUtil.getCount(inputAliases.get(0));
            for (int j = i + 1; j < typeSize; j++) {
                List other = typeInputs.get(j);
                if (reusable == checkReusableInputStatus(reusableInputs, j)
                    && IngredientUtil.aliasesEquals(inputAliases, other)) {
                    count += IngredientUtil.getCount(other.get(0));
                    seen[j] = true;
                }
            }
            inputs.add(new RecipeBookmarkItem<>(inputAliases, count, reusable));
        }
        inputs.forEach(input -> input.foundAliases = true);
    }

    private boolean checkReusableInputStatus(List<Boolean> reusableInputs, int index) {
        return reusableInputs != null && index < reusableInputs.size() && reusableInputs.get(index);
    }

    private List<Boolean> getReusableInputs(List<List<ItemStack>> typeInputs) {
        List<Boolean> reusableInputs = new java.util.ArrayList<>(typeInputs.size());
        for (int i = 0; i < typeInputs.size(); i++) {
            reusableInputs.add(false);
        }

        if (!(recipe instanceof ShapelessRecipesWrapper<?>)) {
            return reusableInputs;
        }

        for (int i = 0; i < typeInputs.size(); i++) {
            if (isReusableInput(typeInputs, i)) {
                reusableInputs.set(i, true);
            }
        }
        return reusableInputs;
    }

    private boolean isReusableInput(List<List<ItemStack>> typeInputs, int inputIndex) {
        List<ItemStack> aliases = removeNulls(typeInputs.get(inputIndex));
        if (aliases.isEmpty()) {
            return false;
        }

        for (ItemStack alias : aliases) {
            if (alias == null || alias.getItem() == null) {
                continue;
            }
            try {
                if (alias.getItem()
                    .hasContainerItem(alias)) {
                    ItemStack containerStack = alias.getItem()
                        .getContainerItem(alias);

                    if (containerStack != null
                        && hasMatchingRemainder(Collections.singletonList(alias), containerStack)) {
                        continue;
                    }
                }
                return false;
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    private InventoryCrafting getInventory() {
        int width = 3;
        int height = 3;
        if (recipe instanceof IShapedCraftingRecipeWrapper) {
            IShapedCraftingRecipeWrapper shapedRecipe = (IShapedCraftingRecipeWrapper) recipe;
            width = shapedRecipe.getWidth();
            height = shapedRecipe.getHeight();
        }
        return new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, width, height);
    }

    private boolean hasMatchingRemainder(List<ItemStack> aliases, ItemStack remainder) {
        if (remainder == null) {
            return false;
        }
        for (ItemStack alias : removeNulls(aliases)) {
            if (alias != null && Internal.getStackHelper()
                .isEquivalent(alias, remainder)) {
                return true;
            }
        }
        return false;
    }

    private <T> List<T> removeNulls(List<T> original) {
        List<T> list = new ObjectArrayList<>(original.size());
        for (T item : original) {
            if (item != null) {
                list.add(item);
            }
        }
        return list;
    }

    public boolean isPopulated() {
        return recipe != null;
    }

    @Override
    public boolean startsNewRow() {
        return secondaryTo == null && this.inputs != null;
    }

    public List<DummyBookmarkItem<?>> getInputs() {
        return inputDummyItems;
    }

    public long getMultiplier() {
        if (outputAmount == 0) {
            return 0;
        }
        return (amount + outputAmount - 1) / outputAmount;
    }

    @Override
    public void changeAmount(long delta) {
        this.selfOutputAmount = Math.round(this.selfOutputAmount / delta) * delta;
        this.selfOutputAmount += delta;
        this.selfOutputAmount = Math.max(0, this.selfOutputAmount);

        if (this.getGroup() instanceof RecipeBookmarkGroup) {
            ((RecipeBookmarkGroup) this.getGroup()).update();
        }
    }

    public IRecipeLayout createLayout() {
        return RecipeLayout.create(-1, (IRecipeCategory) category, recipe, null, 0, 0);
    }

    @Override
    public long getDisplayAmount() {
        if (outputAmount == 0) {
            return amount;
        }
        return outputAmount * getMultiplier();
    }

    @Override
    public boolean deserialize(NBTTagCompound serialized) {
        super.deserialize(serialized);
        this.selfOutputAmount = serialized.getLong("selfOutputAmount");
        this.category = Internal.getRuntime()
            .getRecipeRegistry()
            .getRecipeCategory(serialized.getString("category"));
        this.recipe = Internal.getRuntime()
            .getRecipeRegistry()
            .getRecipeById(serialized.getLong("recipe"), category);
        return category != null && recipe != null;
    }

    public String serialize() {
        NBTTagCompound tag = getNBTOfIngredient(ingredient);
        tag.setLong("amount", amount);
        tag.setLong("selfOutputAmount", selfOutputAmount);
        tag.setString("category", category.getUid());
        tag.setLong(
            "recipe",
            Internal.getRuntime()
                .getRecipeRegistry()
                .getRecipeId(recipe));

        if (ingredient instanceof ItemStack) {
            return MARKER_RECIPE + MARKER_STACK + tag;
        } else {
            return MARKER_RECIPE + MARKER_OTHER + tag;
        }
    }

    @Override
    public void setGroup(BookmarkGroup group) {
        super.setGroup(group);
        if (this.inputDummyItems != null) {
            this.inputDummyItems.forEach(item -> item.setGroup(group));
        }
    }

    @Override
    public RecipeBookmarkItem<I> copy() {
        return new RecipeBookmarkItem<>(this);
    }
}
