package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Objects;

import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.util.Translator;

public class BrewingRecipeWrapper extends BlankRecipeWrapper {

    private final List<ItemStack> ingredients;
    private final ItemStack potionInput;
    private final ItemStack potionOutput;
    private final List<List<ItemStack>> inputs;
    private final int brewingSteps;
    private final int hashCode;

    public BrewingRecipeWrapper(ItemStack ingredient, ItemStack potionInput, ItemStack potionOutput, int brewingSteps) {
        this(Collections.singletonList(ingredient), potionInput, potionOutput, brewingSteps);
    }

    @SuppressWarnings("unchecked")
    public BrewingRecipeWrapper(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput,
        int brewingSteps) {
        this.ingredients = ingredients;
        this.potionInput = potionInput;
        this.potionOutput = potionOutput;
        this.brewingSteps = brewingSteps;

        // SỬA TẠI ĐÂY: Bọc từng potionInput đơn lẻ thành Collections.singletonList()
        this.inputs = new ArrayList<>();
        this.inputs.add(Collections.singletonList(potionInput));
        this.inputs.add(Collections.singletonList(potionInput));
        this.inputs.add(Collections.singletonList(potionInput));
        this.inputs.add(ingredients); // Thằng này bản thân nó đã là List rồi nên giữ nguyên

        ItemStack firstIngredient = ingredients.get(0);

        this.hashCode = Objects.hashCode(
            potionInput.getItemDamage(),
            potionOutput.getItemDamage(),
            firstIngredient.getItem(),
            firstIngredient.getItemDamage());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, potionOutput);
    }

    public List<List<ItemStack>> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return Collections.singletonList(potionOutput);
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (brewingSteps > 0) {
            String steps = Translator.translateToLocalFormatted("gui.jfmuy.category.brewing.steps", brewingSteps);
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
        return potion1.getItemDamage() == potion2.getItemDamage();
    }

    public int getBrewingSteps() {
        return brewingSteps;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return ingredients + " + " + potionInput + " = " + potionOutput;
    }
}
