package ruiseki.jfmuy.plugins.jfmuy.ingredients;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.okcore.client.renderer.GlStateManager;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {

    private final IIngredientHelper<DebugIngredient> ingredientHelper;

    public DebugIngredientRenderer(IIngredientHelper<DebugIngredient> ingredientHelper) {
        this.ingredientHelper = ingredientHelper;
    }

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
        if (ingredient != null) {
            FontRenderer font = getFontRenderer(minecraft, ingredient);
            font.drawString("JEI", xPosition, yPosition, Color.RED.getRGB());
            font.drawString("#" + ingredient.getNumber(), xPosition, yPosition + 8, Color.RED.getRGB());
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, DebugIngredient ingredient, boolean tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        String displayName = ingredientHelper.getDisplayName(ingredient);
        tooltip.add(displayName);
        tooltip.add(EnumChatFormatting.GRAY + "debug ingredient");
        return tooltip;
    }
}
