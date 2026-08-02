package ruiseki.jfmuy.plugins.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.okcore.client.renderer.GlStateManager;

public class NEITemplateWrapper implements IRecipeWrapper {

    @NotNull
    private final TemplateRecipeHandler handler;
    private final int recipeIndex;

    public NEITemplateWrapper(@NotNull TemplateRecipeHandler handler, int recipeIndex) {
        this.handler = handler;
        this.recipeIndex = recipeIndex;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputLists = new ArrayList<>();

        List<PositionedStack> inputs = handler.getIngredientStacks(recipeIndex);
        if (inputs != null) {
            for (PositionedStack stack : inputs) {
                List<ItemStack> extracted = NEITemplateCategory.extractStacks(stack);
                if (!extracted.isEmpty()) {
                    inputLists.add(extracted);
                }
            }
        }

        List<PositionedStack> otherStacks = handler.getOtherStacks(recipeIndex);
        if (otherStacks != null) {
            for (PositionedStack stack : otherStacks) {
                List<ItemStack> extracted = NEITemplateCategory.extractStacks(stack);
                if (!extracted.isEmpty()) {
                    inputLists.add(extracted);
                }
            }
        }
        ingredients.setInputLists(VanillaTypes.ITEM, inputLists);

        PositionedStack result = handler.getResultStack(recipeIndex);
        List<ItemStack> outputList = NEITemplateCategory.extractStacks(result);
        ingredients.setOutputs(VanillaTypes.ITEM, outputList);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (recipeIndex >= 0 && recipeIndex < handler.numRecipes()) {
            handler.cycleticks = (int) (System.currentTimeMillis() / 50L);

            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();

            try {

                handler.drawBackground(recipeIndex);
                handler.drawForeground(recipeIndex);
            } catch (Throwable ignored) {}

            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    public TemplateRecipeHandler getHandler() {
        return handler;
    }

    public int getRecipeIndex() {
        return recipeIndex;
    }
}
