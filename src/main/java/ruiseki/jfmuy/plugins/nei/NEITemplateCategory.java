package ruiseki.jfmuy.plugins.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;

public class NEITemplateCategory implements IRecipeCategory<NEITemplateWrapper> {

    private final String uid;
    private final String title;
    private final IDrawable background;

    public NEITemplateCategory(IGuiHelper guiHelper, TemplateRecipeHandler handler, String recipeId) {
        this.title = handler.getRecipeName();
        this.uid = recipeId;

        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
        int recipeHeight = handler.getRecipeHeight(0);
        int height = (recipeHeight > 0 ? recipeHeight : info.getHeight()) + info.getYShift() + 4;
        int width = Math.max(HandlerInfo.DEFAULT_WIDTH, info.getWidth());

        this.background = guiHelper.createBlankDrawable(width, height);
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public String getTitle() {
        if ("nei.crafting".equals(this.uid)) {
            return "Crafting";
        }
        return this.title;
    }

    @Override
    public String getModName() {
        return "Not Enough Items";
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, NEITemplateWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        int slotIndex = 0;
        int recipeIdx = recipeWrapper.getRecipeIndex();
        TemplateRecipeHandler currentHandler = recipeWrapper.getHandler();

        if (currentHandler == null || recipeIdx < 0 || recipeIdx >= currentHandler.numRecipes()) {
            return;
        }

        // 1. Set Input Slots
        List<PositionedStack> inputs = currentHandler.getIngredientStacks(recipeIdx);
        if (inputs != null) {
            for (PositionedStack stack : inputs) {
                if (isValidStack(stack)) {
                    itemStacks.init(slotIndex, true, stack.relx - 1, stack.rely - 1);
                    itemStacks.set(slotIndex, extractStacks(stack));
                    slotIndex++;
                }
            }
        }

        // 2. Set Output Slot
        PositionedStack result = currentHandler.getResultStack(recipeIdx);
        if (isValidStack(result)) {
            itemStacks.init(slotIndex, false, result.relx - 1, result.rely - 1);
            itemStacks.set(slotIndex, extractStacks(result));
            slotIndex++;
        }

        // 3. Set Other Slots (Catalyst / Fuels / Extra)
        List<PositionedStack> otherStacks = currentHandler.getOtherStacks(recipeIdx);
        if (otherStacks != null) {
            for (PositionedStack stack : otherStacks) {
                if (isValidStack(stack)) {
                    itemStacks.init(slotIndex, true, stack.relx - 1, stack.rely - 1);
                    itemStacks.set(slotIndex, extractStacks(stack));
                    slotIndex++;
                }
            }
        }
    }

    public static boolean isValidStack(PositionedStack stack) {
        if (stack == null) return false;
        if (stack.items != null && stack.items.length > 0) {
            for (ItemStack is : stack.items) {
                if (is != null) return true;
            }
        }
        return stack.item != null;
    }

    public static List<ItemStack> extractStacks(PositionedStack pStack) {
        if (pStack == null) return Collections.emptyList();

        try {
            pStack.generatePermutations();
        } catch (Throwable ignored) {}

        List<ItemStack> filtered = pStack.getFilteredPermutations();
        if (filtered != null && !filtered.isEmpty()) {
            List<ItemStack> list = new ArrayList<>(filtered.size());
            for (ItemStack is : filtered) {
                if (is != null) {
                    list.add(is.copy());
                }
            }
            return list;
        }

        if (pStack.items != null && pStack.items.length > 0) {
            List<ItemStack> list = new ArrayList<>(pStack.items.length);
            for (ItemStack is : pStack.items) {
                if (is != null) {
                    list.add(is.copy());
                }
            }
            return list;
        } else if (pStack.item != null) {
            return Collections.singletonList(pStack.item.copy());
        }

        return Collections.emptyList();
    }
}
