package ruiseki.jfmuy.autocrafting.favorites;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.util.Log;

public class FavoriteRecipes {

    private static final Map<String, IRecipeWrapper> ingredients = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<IRecipeWrapper, IRecipeCategory<?>> recipeCategories = new Object2ObjectOpenHashMap<>(
        8192);

    private static IngredientRegistry getIngredientRegistry() {
        return Internal.getIngredientRegistry();
    }

    private static RecipeRegistry getRecipeRegistry() {
        return Internal.getRuntime()
            .getRecipeRegistry();
    }

    public static void load() {
        ingredients.clear();
        recipeCategories.clear();

        File file = Config.getFavoriteFile();
        if (file == null || !file.exists()) {
            return;
        }

        List<String> strings;
        try (FileReader reader = new FileReader(file)) {
            strings = IOUtils.readLines(reader);
        } catch (IOException e) {
            Log.get()
                .error("Failed to load favorite recipes from file {}", file, e);
            return;
        }

        Long2ObjectMap<String> rawRecipes = new Long2ObjectOpenHashMap<>(8192);
        IRecipeCategory<?> currentCategory = null;
        RecipeRegistry recipeRegistry = getRecipeRegistry();

        for (String string : strings) {
            if (string.isEmpty()) continue;
            if (string.charAt(0) == '#') {
                addRecipesForCategory(currentCategory, rawRecipes, recipeRegistry);
                currentCategory = recipeRegistry.getRecipeCategory(string.substring(1));
                continue;
            }
            String[] split = string.split("%");
            if (split.length >= 2) {
                try {
                    long recipeIdString = Long.parseLong(split[0]);
                    String ingredientString = split[1];
                    rawRecipes.put(recipeIdString, ingredientString);
                } catch (NumberFormatException ignored) {}
            }
        }
        addRecipesForCategory(currentCategory, rawRecipes, recipeRegistry);
    }

    public static void addRecipesForCategory(IRecipeCategory<?> category, Long2ObjectMap<String> rawRecipes,
        RecipeRegistry recipeRegistry) {
        if (category != null && !rawRecipes.isEmpty()) {
            for (Long2ObjectMap.Entry<String> entry : rawRecipes.long2ObjectEntrySet()) {
                IRecipeWrapper recipe = recipeRegistry.getRecipeById(entry.getLongKey(), category);
                if (recipe != null) {
                    ingredients.put(entry.getValue(), recipe);
                    recipeCategories.put(recipe, category);
                } else {
                    Log.get()
                        .warn(
                            "Could not find recipe with id {} in category {}!",
                            entry.getLongKey(),
                            category.getUid());
                }
            }
        }
        rawRecipes.clear();
    }

    public static void save() {
        File file = Config.getFavoriteFile();
        if (file == null) return;

        List<String> strings = new ArrayList<>();
        RecipeRegistry recipeRegistry = getRecipeRegistry();

        Map<IRecipeCategory<?>, Map<String, IRecipeWrapper>> categoryMap = ingredients.entrySet()
            .stream()
            .collect(Object2ObjectOpenHashMap::new, (map, entry) -> {
                IRecipeCategory<?> category = recipeCategories.get(entry.getValue());
                if (category != null) {
                    map.computeIfAbsent(category, k -> new Object2ObjectOpenHashMap<>())
                        .put(entry.getKey(), entry.getValue());
                }
            }, Object2ObjectOpenHashMap::putAll);

        for (Map.Entry<IRecipeCategory<?>, Map<String, IRecipeWrapper>> categoryEntry : categoryMap.entrySet()) {
            strings.add(
                "#" + categoryEntry.getKey()
                    .getUid());
            for (Map.Entry<String, IRecipeWrapper> ingredientAndRecipe : categoryEntry.getValue()
                .entrySet()) {
                long recipeId = recipeRegistry.getRecipeId(ingredientAndRecipe.getValue());
                if (recipeId != -1) {
                    strings.add(recipeId + "%" + ingredientAndRecipe.getKey());
                }
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            IOUtils.writeLines(strings, "\n", writer);
        } catch (IOException e) {
            Log.get()
                .error("Failed to save favorite recipes to file {}", file, e);
        }
    }

    public static boolean isFavorite(IRecipeWrapper recipe) {
        return ingredients.containsValue(recipe);
    }

    public static boolean isFavoriteFor(IRecipeWrapper recipe, Object ingredient) {
        return getFavorite(ingredient) == recipe;
    }

    public static void toggleFavorite(Object ingredient, Object displayIngredient, IRecipeWrapper recipe,
        IRecipeCategory<?> category) {
        if (ingredient == null || recipe == null) return;

        List<Object> allVariants = getAllIngredientVariants(ingredient, displayIngredient);

        Object primaryObj = (displayIngredient != null) ? displayIngredient : ingredient;
        if (primaryObj instanceof Collection && !((Collection<?>) primaryObj).isEmpty()) {
            primaryObj = ((Collection<?>) primaryObj).iterator()
                .next();
        }

        String mainId = getIngredientRegistry().getIngredientHelper(primaryObj)
            .getUniqueId(primaryObj);
        boolean isAlreadyFavorite = ingredients.containsKey(mainId) && ingredients.get(mainId) == recipe;

        if (isAlreadyFavorite) {
            ingredients.entrySet()
                .removeIf(entry -> entry.getValue() == recipe);
            recipeCategories.remove(recipe);
        } else {
            for (Object variant : allVariants) {
                if (variant != null) {
                    String id = getIngredientRegistry().getIngredientHelper(variant)
                        .getUniqueId(variant);
                    ingredients.put(id, recipe);
                }
            }
            recipeCategories.put(recipe, category);
        }
        save();
    }

    public static void toggleFavorite(Object ingredient, IRecipeWrapper recipe, IRecipeCategory<?> category) {
        toggleFavorite(ingredient, null, recipe, category);
    }

    public static void removeFavorite(IRecipeWrapper recipe) {
        if (recipe == null) return;
        ingredients.entrySet()
            .removeIf(entry -> entry.getValue() == recipe);
        recipeCategories.remove(recipe);
        save();
    }

    public static IRecipeWrapper getFavorite(Object ingredient, Object displayIngredient) {
        if (displayIngredient != null) {
            String displayId = getIngredientRegistry().getIngredientHelper(displayIngredient)
                .getUniqueId(displayIngredient);
            IRecipeWrapper recipe = ingredients.get(displayId);
            if (recipe != null) {
                return recipe;
            }
        }

        if (ingredient == null) return null;

        if (!(ingredient instanceof Collection)) {
            String id = getIngredientRegistry().getIngredientHelper(ingredient)
                .getUniqueId(ingredient);
            IRecipeWrapper recipe = ingredients.get(id);
            if (recipe != null) {
                return recipe;
            }
        }

        if (ingredient instanceof Collection) {
            for (Object item : (Collection<?>) ingredient) {
                if (item != null) {
                    String subId = getIngredientRegistry().getIngredientHelper(item)
                        .getUniqueId(item);
                    IRecipeWrapper subRecipe = ingredients.get(subId);
                    if (subRecipe != null) return subRecipe;
                }
            }
        }

        return null;
    }

    public static IRecipeWrapper getFavorite(Object ingredient) {
        return getFavorite(ingredient, null);
    }

    public static IRecipeCategory<?> getFavoriteCategory(Object ingredient) {
        IRecipeWrapper recipe = getFavorite(ingredient);
        return recipe != null ? recipeCategories.get(recipe) : null;
    }

    public static IRecipeCategory<?> getFavoriteCategory(Object ingredient, Object displayIngredient) {
        IRecipeWrapper recipe = getFavorite(ingredient, displayIngredient);
        return recipe != null ? recipeCategories.get(recipe) : null;
    }

    private static List<Object> getAllIngredientVariants(Object ingredient, Object displayIngredient) {
        List<Object> variants = new ArrayList<>();

        if (displayIngredient != null) {
            variants.add(displayIngredient);
        }

        if (ingredient instanceof Collection) {
            for (Object item : (Collection<?>) ingredient) {
                if (item != null && !item.equals(displayIngredient)) {
                    variants.add(item);
                }
            }
        } else if (ingredient != null && !ingredient.equals(displayIngredient)) {
            variants.add(ingredient);
        }

        return variants;
    }
}
