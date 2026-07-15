package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
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

        List<ItemStack> potionIngredients = ingredientRegistry.getPotionIngredients();

        List<ItemStack> knownPotions = new ArrayList<>();
        knownPotions.add(BrewingRecipeUtil.WATER_BOTTLE);

        boolean foundNewPotions;
        do {
            List<ItemStack> newPotions = getNewPotions(knownPotions, potionIngredients, recipes);
            foundNewPotions = !newPotions.isEmpty();
            knownPotions.addAll(newPotions);
        } while (foundNewPotions);

        List<BrewingRecipeWrapper> recipeList = new ArrayList<>(recipes);
        recipeList.sort(Comparator.comparingInt(BrewingRecipeWrapper::getBrewingSteps));

        return recipeList;
    }

    private List<ItemStack> getNewPotions(List<ItemStack> knownPotions, List<ItemStack> potionIngredients,
        Collection<BrewingRecipeWrapper> recipes) {
        List<ItemStack> newPotions = new ArrayList<>();

        for (ItemStack potionInput : knownPotions) {
            for (ItemStack potionIngredient : potionIngredients) {
                if (potionInput == null || potionIngredient == null) {
                    continue;
                }

                int inputDamage = potionInput.getItemDamage();

                String ingredientEffect = potionIngredient.getItem()
                    .getPotionEffect(potionIngredient);
                if (ingredientEffect == null || ingredientEffect.isEmpty()) {
                    continue;
                }

                int outputDamage = PotionHelper.applyIngredient(inputDamage, ingredientEffect);

                if (outputDamage != inputDamage) {

                    List<PotionEffect> inputEffects = PotionHelper.getPotionEffects(inputDamage, false);
                    List<PotionEffect> outputEffects = PotionHelper.getPotionEffects(outputDamage, false);

                    if (!Objects.equals(inputEffects, outputEffects)) {
                        ItemStack potionOutput = new ItemStack(Items.potionitem, 1, outputDamage);

                        BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(
                            Collections.singletonList(potionIngredient),
                            potionInput.copy(),
                            potionOutput);

                        if (!recipes.contains(recipe) && !disabledRecipes.contains(recipe)) {
                            recipes.add(recipe);
                            newPotions.add(potionOutput);
                        }
                    }
                }
            }
        }
        return newPotions;
    }
}
