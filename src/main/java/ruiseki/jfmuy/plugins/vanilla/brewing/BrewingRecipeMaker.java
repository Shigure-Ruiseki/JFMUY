package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;

import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;

public class BrewingRecipeMaker {

    private final Set<BrewingRecipeWrapper> disabledRecipes = new HashSet<>();
    private final IIngredientRegistry ingredientRegistry;

    public static List<BrewingRecipeWrapper> getBrewingRecipes(IIngredientRegistry ingredientRegistry) {
        BrewingRecipeMaker brewingRecipeMaker = new BrewingRecipeMaker(ingredientRegistry);
        return brewingRecipeMaker.getBrewingRecipes();
    }

    private BrewingRecipeMaker(IIngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
    }

    private List<BrewingRecipeWrapper> getBrewingRecipes() {
        Set<BrewingRecipeWrapper> recipes = new HashSet<>();
        List<ItemStack> potionIngredients = new ArrayList<>(ingredientRegistry.getPotionIngredients());

        List<ItemStack> knownPotions = new ArrayList<>();
        knownPotions.add(BrewingRecipeUtil.WATER_BOTTLE);

        List<ItemStack> searchPotions = new ArrayList<>();
        searchPotions.add(BrewingRecipeUtil.WATER_BOTTLE);

        do {
            List<ItemStack> newPotions = new ArrayList<>();
            for (ItemStack potionInput : searchPotions) {
                int basePotion = potionInput.getItemDamage();

                if (ItemPotion.isSplash(basePotion)) continue;

                for (ItemStack ingredient : potionIngredients) {
                    if (ingredient == null || ingredient.getItem() == null) continue;

                    String effectStr = ingredient.getItem()
                        .getPotionEffect(ingredient);
                    int resultDamage = PotionHelper.applyIngredient(basePotion, effectStr);

                    if (ItemPotion.isSplash(resultDamage)) {
                        addRecipeAndPotion(
                            potionInput,
                            ingredient,
                            new ItemStack(Items.potionitem, 1, resultDamage),
                            recipes,
                            newPotions,
                            knownPotions);
                        continue;
                    }

                    List<?> baseMods = Items.potionitem.getEffects(basePotion);
                    List<?> newMods = Items.potionitem.getEffects(resultDamage);

                    if ((basePotion > 0 && Objects.equals(baseMods, newMods))
                        || (baseMods != null && (baseMods.equals(newMods) || newMods == null))
                        || basePotion == resultDamage
                        || levelModifierChanged(basePotion, resultDamage)) {
                        continue;
                    }

                    addRecipeAndPotion(
                        potionInput,
                        ingredient,
                        new ItemStack(Items.potionitem, 1, resultDamage),
                        recipes,
                        newPotions,
                        knownPotions);
                }
            }
            searchPotions = newPotions;
        } while (!searchPotions.isEmpty());

        List<BrewingRecipeWrapper> recipeList = new ArrayList<>(recipes);
        recipeList.sort(Comparator.comparingInt(BrewingRecipeWrapper::getBrewingSteps));

        return recipeList;
    }

    private static boolean levelModifierChanged(int basePotionID, int result) {
        int basemod = basePotionID & 0xE0;
        int resultmod = result & 0xE0;
        return basemod != 0 && basemod != resultmod;
    }

    private void addRecipeAndPotion(ItemStack input, ItemStack ingredient, ItemStack output,
        Set<BrewingRecipeWrapper> recipes, List<ItemStack> newPotions, List<ItemStack> knownPotions) {
        BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(
            Collections.singletonList(ingredient),
            input.copy(),
            output);

        if (!recipes.contains(recipe) && !disabledRecipes.contains(recipe)) {
            recipes.add(recipe);

            boolean exists = false;
            for (ItemStack known : knownPotions) {
                if (known.getItem() == output.getItem() && known.getItemDamage() == output.getItemDamage()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                knownPotions.add(output);
                newPotions.add(output);
            }
        }
    }
}
