package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;

import com.google.common.collect.ImmutableList;

import ruiseki.jfmuy.util.Log;

public class BrewingRecipeMaker {

    private static final Set<Class> unhandledRecipeClasses = new HashSet<>();

    @Nonnull
    public static List<BrewingRecipeWrapper> getBrewingRecipes(IItemRegistry itemRegistry) {
        Set<BrewingRecipeWrapper> recipes = new HashSet<>();

        addVanillaBrewingRecipes(itemRegistry, recipes);
        addModdedBrewingRecipes(recipes);

        List<BrewingRecipeWrapper> recipeList = new ArrayList<>(recipes);
        Collections.sort(recipeList, new Comparator<BrewingRecipeWrapper>() {

            @Override
            public int compare(BrewingRecipeWrapper o1, BrewingRecipeWrapper o2) {
                return Integer.compare(o1.getBrewingSteps(), o2.getBrewingSteps());
            }
        });

        return recipeList;
    }

    private static void addVanillaBrewingRecipes(IItemRegistry itemRegistry, Collection<BrewingRecipeWrapper> recipes) {
        ImmutableList<ItemStack> potionIngredients = itemRegistry.getPotionIngredients();
        Set<Integer> potionMetas = new HashSet<>();
        potionMetas.add(0);

        int brewingSteps = 1;
        ItemStack potionInput = new ItemStack(Items.potionitem);
        Set<Integer> newPotionMetas = new HashSet<>();
        do {
            newPotionMetas.clear();
            for (Integer potionInputMeta : potionMetas) {
                potionInput.setItemDamage(potionInputMeta);
                for (ItemStack potionIngredient : potionIngredients) {
                    ItemStack potionOutput = getVanillaBrewingOutput(potionInput, potionIngredient);
                    if (potionOutput != null) {
                        int potionOutputMeta = potionOutput.getItemDamage();
                        if (potionInputMeta != potionOutputMeta) {
                            BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(
                                potionIngredient,
                                potionInput.copy(),
                                potionOutput,
                                brewingSteps);
                            if (!recipes.contains(recipe)) {
                                recipes.add(recipe);
                                newPotionMetas.add(potionOutputMeta);
                            }
                        }
                    }
                }
            }
            potionMetas.addAll(newPotionMetas);
            brewingSteps++;
            if (brewingSteps > 100) {
                Log.error("Calculation of vanilla brewing recipes is broken, aborting after 100 brewing steps.");
                return;
            }
        } while (newPotionMetas.size() > 0);
    }

    @Nullable
    private static ItemStack getVanillaBrewingOutput(@Nonnull ItemStack input, @Nonnull ItemStack ingredient) {
        if (input.getItem() == null || ingredient.getItem() == null) {
            return null;
        }
        if (!ingredient.getItem()
            .isPotionIngredient(ingredient)) {
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

    private static int applyBrewingIngredient(int meta, ItemStack ingredient) {
        if (ingredient == null) {
            return meta;
        }
        if (ingredient.getItem()
            .isPotionIngredient(ingredient)) {
            return PotionHelper.applyIngredient(
                meta,
                ingredient.getItem()
                    .getPotionEffect(ingredient));
        }
        return meta;
    }

    private static boolean isValidBrewingOutput(int inputMeta, int outputMeta) {
        if (!ItemPotion.isSplash(inputMeta) && ItemPotion.isSplash(outputMeta)) {
            return true;
        }
        List list = Items.potionitem.getEffects(inputMeta);
        List list1 = Items.potionitem.getEffects(outputMeta);
        return (inputMeta <= 0 || list != list1) && (list == null || !list.equals(list1) && list1 != null)
            && inputMeta != outputMeta;
    }

    private static void addModdedBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
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
                    Class recipeClass = iBrewingRecipe.getClass();
                    if (!unhandledRecipeClasses.contains(recipeClass)) {
                        unhandledRecipeClasses.add(recipeClass);
                        Log.debug("Can't handle brewing recipe class: {}", recipeClass);
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {
            // BrewingRecipeRegistry is not present on this Forge version
        } catch (ReflectiveOperationException e) {
            Log.error("Failed to read modded brewing recipes.", e);
        }
    }
}
