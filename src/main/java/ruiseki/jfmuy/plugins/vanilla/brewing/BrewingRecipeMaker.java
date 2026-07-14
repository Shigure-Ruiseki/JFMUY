package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;

import com.google.common.collect.ImmutableList;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.util.Log;

public class BrewingRecipeMaker {

    private final Set<Class<?>> unhandledRecipeClasses = new HashSet<Class<?>>();
    private final Map<String, Integer> brewingSteps = new HashMap<String, Integer>();
    private final IIngredientRegistry ingredientRegistry;
    private final IIngredientHelper<ItemStack> itemStackHelper;

    public static List<BrewingRecipeWrapper> getBrewingRecipes(IIngredientRegistry ingredientRegistry) {
        BrewingRecipeMaker brewingRecipeMaker = new BrewingRecipeMaker(ingredientRegistry);
        return brewingRecipeMaker.getBrewingRecipes();
    }

    private BrewingRecipeMaker(IIngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
        this.itemStackHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
    }

    private List<BrewingRecipeWrapper> getBrewingRecipes() {
        unhandledRecipeClasses.clear();
        brewingSteps.clear();

        Set<BrewingRecipeWrapper> recipes = new HashSet<BrewingRecipeWrapper>();

        addVanillaBrewingRecipes(recipes);
        addModdedBrewingRecipes(recipes);

        List<BrewingRecipeWrapper> recipeList = new ArrayList<BrewingRecipeWrapper>(recipes);
        Collections.sort(recipeList, new Comparator<BrewingRecipeWrapper>() {

            @Override
            public int compare(BrewingRecipeWrapper o1, BrewingRecipeWrapper o2) {
                return Integer.compare(o1.getBrewingSteps(), o2.getBrewingSteps());
            }
        });

        return recipeList;
    }

    private void addVanillaBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
        ImmutableList<ItemStack> potionIngredients = ingredientRegistry.getPotionIngredients();

        List<ItemStack> knownPotions = new ArrayList<ItemStack>();

        ItemStack waterBottle = new ItemStack(Items.potionitem, 1, 0);
        knownPotions.add(waterBottle);

        String waterBottleUid = itemStackHelper.getUniqueId(waterBottle);
        brewingSteps.put(waterBottleUid, 0);

        int currentStep = 1;
        boolean foundNewPotions;

        do {
            List<ItemStack> newPotions = getNewPotions(currentStep, knownPotions, potionIngredients, recipes);
            foundNewPotions = !newPotions.isEmpty();
            knownPotions.addAll(newPotions);

            currentStep++;
            if (currentStep > 50) {
                break;
            }
        } while (foundNewPotions);
    }

    private List<ItemStack> getNewPotions(final int currentStep, List<ItemStack> knownPotions,
        ImmutableList<ItemStack> potionIngredients, Collection<BrewingRecipeWrapper> recipes) {
        List<ItemStack> newPotions = new ArrayList<ItemStack>();

        List<ItemStack> inputsToProcess = new ArrayList<ItemStack>(knownPotions);

        for (ItemStack potionInput : inputsToProcess) {
            String inputUid = itemStackHelper.getUniqueId(potionInput);
            Integer inputStep = brewingSteps.get(inputUid);

            if (inputStep == null || inputStep != currentStep - 1) {
                continue;
            }

            for (ItemStack potionIngredient : potionIngredients) {
                ItemStack potionOutput = getVanillaBrewingOutput(potionInput, potionIngredient);
                if (potionOutput == null) {
                    continue;
                }

                int potionInputMeta = potionInput.getItemDamage();
                int potionOutputMeta = potionOutput.getItemDamage();

                if (potionInputMeta != potionOutputMeta) {
                    BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(
                        potionIngredient,
                        potionInput.copy(),
                        potionOutput,
                        currentStep);

                    if (!recipes.contains(recipe)) {
                        recipes.add(recipe);

                        String outputUid = itemStackHelper.getUniqueId(potionOutput);
                        if (!brewingSteps.containsKey(outputUid)) {
                            brewingSteps.put(outputUid, currentStep);
                            newPotions.add(potionOutput);
                        }
                    }
                }
            }
        }
        return newPotions;
    }

    @Nullable
    private static ItemStack getVanillaBrewingOutput(@Nonnull ItemStack input, @Nonnull ItemStack ingredient) {
        if (input.getItem() == null || ingredient.getItem() == null) {
            return null;
        }
        if (!isPotionIngredient(ingredient)) {
            return null;
        }
        if (!(input.getItem() instanceof ItemPotion)) {
            return null;
        }

        int inputMeta = input.getItemDamage();
        int outputMeta = applyBrewingIngredient(inputMeta, ingredient);

        if (!isValidBrewingOutput(inputMeta, outputMeta)) {
            return null;
        }
        ItemStack output = input.copy();
        output.setItemDamage(outputMeta);
        return output;
    }

    private static boolean isPotionIngredient(ItemStack ingredient) {
        if (ingredient == null || ingredient.getItem() == null) {
            return false;
        }
        String effectStr = ingredient.getItem()
            .getPotionEffect(ingredient);
        return effectStr != null && !effectStr.isEmpty();
    }

    private static int applyBrewingIngredient(int meta, ItemStack ingredient) {
        if (ingredient == null || ingredient.getItem() == null) {
            return meta;
        }
        String effectStr = ingredient.getItem()
            .getPotionEffect(ingredient);
        if (effectStr != null && !effectStr.isEmpty()) {
            return PotionHelper.applyIngredient(meta, effectStr);
        }
        return meta;
    }

    @SuppressWarnings("unchecked")
    private static boolean isValidBrewingOutput(int inputMeta, int outputMeta) {
        if (!ItemPotion.isSplash(inputMeta) && ItemPotion.isSplash(outputMeta)) {
            return true;
        }

        List<?> list = Items.potionitem.getEffects(inputMeta);
        List<?> list1 = Items.potionitem.getEffects(outputMeta);

        return (inputMeta <= 0 || list != list1) && (list == null || !list.equals(list1) && list1 != null)
            && inputMeta != outputMeta;
    }

    private void addModdedBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
        try {
            Class<?> registryClass = Class.forName("net.minecraftforge.common.brewing.BrewingRecipeRegistry");
            Method getRecipesMethod = registryClass.getMethod("getRecipes");
            @SuppressWarnings("unchecked")
            List<Object> brewingRecipes = (List<Object>) getRecipesMethod.invoke(null);

            Class<?> brewingRecipeClass = Class.forName("net.minecraftforge.common.brewing.BrewingRecipe");
            Class<?> brewingOreRecipeClass = Class.forName("net.minecraftforge.common.brewing.BrewingOreRecipe");
            Class<?> vanillaBrewingRecipeClass = Class
                .forName("net.minecraftforge.common.brewing.VanillaBrewingRecipe");

            Field ingredientField = brewingRecipeClass.getField("ingredient");
            Field inputField = brewingRecipeClass.getField("input");
            Field outputField = brewingRecipeClass.getField("output");

            for (Object iBrewingRecipe : brewingRecipes) {
                if (brewingRecipeClass.isInstance(iBrewingRecipe)) {
                    ItemStack ingredient = (ItemStack) ingredientField.get(iBrewingRecipe);
                    ItemStack input = (ItemStack) inputField.get(iBrewingRecipe);
                    ItemStack output = (ItemStack) outputField.get(iBrewingRecipe);
                    BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(ingredient, input, output, 0);
                    recipes.add(recipe);
                } else if (brewingOreRecipeClass.isInstance(iBrewingRecipe)) {
                    ItemStack ingredient = (ItemStack) ingredientField.get(iBrewingRecipe);
                    ItemStack input = (ItemStack) inputField.get(iBrewingRecipe);
                    ItemStack output = (ItemStack) outputField.get(iBrewingRecipe);
                    BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(ingredient, input, output, 0);
                    recipes.add(recipe);
                } else if (!vanillaBrewingRecipeClass.isInstance(iBrewingRecipe)) {
                    Class<?> recipeClass = iBrewingRecipe.getClass();
                    if (!unhandledRecipeClasses.contains(recipeClass)) {
                        unhandledRecipeClasses.add(recipeClass);
                        Log.debug("Can't handle brewing recipe class: " + recipeClass);
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {

        } catch (Exception e) {
            Log.error("Failed to read modded brewing recipes.", e);
        }
    }
}
