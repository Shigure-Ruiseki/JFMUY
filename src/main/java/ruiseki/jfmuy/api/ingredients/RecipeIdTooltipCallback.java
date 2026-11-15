package ruiseki.jfmuy.api.ingredients;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.startup.ForgeModIdHelper;
import ruiseki.jfmuy.util.Translator;

public class RecipeIdTooltipCallback<T> implements ITooltipCallback<T> {

    private final ResourceLocation recipeId;
    private final boolean includeRecipeBy;
    private final IIngredientHelper<T> ingredientHelper;

    public RecipeIdTooltipCallback(ResourceLocation recipeId, boolean includeRecipeBy,
        IIngredientHelper<T> ingredientHelper) {
        this.recipeId = recipeId;
        this.includeRecipeBy = includeRecipeBy;
        this.ingredientHelper = ingredientHelper;
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip) {
        if (!input) {
            if (includeRecipeBy && Config.getTooltipShowRecipeBy()) {
                String recipeModId = recipeId.getResourceDomain();
                String ingredientModId = ingredientHelper.getDisplayModId(ingredient);

                if (!ingredientModId.isEmpty() && !recipeModId.equals(ingredientModId)) {
                    String modName = ForgeModIdHelper.getInstance()
                        .getFormattedModNameForModId(recipeModId);
                    if (modName != null) {
                        tooltip.add(
                            EnumChatFormatting.GRAY
                                + Translator.translateToLocalFormatted("jfmuy.tooltip.recipe.by", modName));
                    }
                }
            }

            boolean showAdvanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips
                && GuiScreen.isShiftKeyDown();
            if (showAdvanced) {
                tooltip.add(
                    EnumChatFormatting.DARK_GRAY
                        + Translator.translateToLocalFormatted("jfmuy.tooltip.recipe.id", recipeId));
            }
        }
    }
}
