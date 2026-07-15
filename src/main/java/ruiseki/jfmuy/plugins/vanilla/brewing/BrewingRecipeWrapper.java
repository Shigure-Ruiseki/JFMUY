package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.google.common.base.Objects;

import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.util.Translator;

public class BrewingRecipeWrapper implements IRecipeWrapper {

    private static final BrewingRecipeUtil UTIL = new BrewingRecipeUtil();

    private final List<ItemStack> ingredients;
    private final ItemStack potionInput;
    private final ItemStack potionOutput;
    private final List<List<ItemStack>> inputs;
    private final int hashCode;

    public BrewingRecipeWrapper(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
        this.ingredients = ingredients;
        this.potionInput = potionInput;
        this.potionOutput = potionOutput;

        UTIL.addRecipe(potionInput, potionOutput);

        this.inputs = new ArrayList<>();
        this.inputs.add(Collections.singletonList(potionInput));
        this.inputs.add(Collections.singletonList(potionInput));
        this.inputs.add(Collections.singletonList(potionInput));
        this.inputs.add(ingredients);

        ItemStack firstIngredient = ingredients.get(0);

        this.hashCode = Objects.hashCode(
            potionInput.getItem(),
            potionInput.getItemDamage(),
            potionOutput.getItem(),
            potionOutput.getItemDamage(),
            firstIngredient.getItem(),
            firstIngredient.getItemDamage());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, potionOutput);
    }

    public List getInputs() {
        return inputs;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int brewingSteps = getBrewingSteps();
        if (brewingSteps < Integer.MAX_VALUE) {
            String steps = Translator.translateToLocalFormatted("gui.jei.category.brewing.steps", brewingSteps);
            minecraft.fontRenderer.drawString(steps, 70, 28, Color.gray.getRGB());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrewingRecipeWrapper)) {
            return false;
        }
        BrewingRecipeWrapper other = (BrewingRecipeWrapper) obj;

        if (!arePotionsEqual(other.potionInput, potionInput)) {
            return false;
        }

        if (!arePotionsEqual(other.potionOutput, potionOutput)) {
            return false;
        }

        if (ingredients.size() != other.ingredients.size()) {
            return false;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            if (!ItemStack.areItemStacksEqual(ingredients.get(i), other.ingredients.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean arePotionsEqual(ItemStack potion1, ItemStack potion2) {
        if (potion1 == null || potion2 == null) {
            return potion1 == potion2;
        }
        return potion1.getItem() == potion2.getItem() && potion1.getItemDamage() == potion2.getItemDamage();
    }

    public int getBrewingSteps() {
        return UTIL.getBrewingSteps(potionOutput);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return ingredients + " + ["
            + potionInput.getItem()
            + " M:"
            + potionInput.getItemDamage()
            + "] = ["
            + potionOutput
            + " M:"
            + potionOutput.getItemDamage()
            + "]";
    }
}
