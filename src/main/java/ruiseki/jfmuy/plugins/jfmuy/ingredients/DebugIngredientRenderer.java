package ruiseki.jfmuy.plugins.jfmuy.ingredients;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.plugins.jfmuy.JFMUYInternalPlugin;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
        if (ingredient != null) {
            FontRenderer font = getFontRenderer(minecraft, ingredient);
            font.drawString("JEI", xPosition, yPosition, Color.RED.getRGB());
            font.drawString("#" + ingredient.getNumber(), xPosition, yPosition + 8, Color.RED.getRGB());
        }
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, DebugIngredient ingredient) {
        List<String> tooltip = new ArrayList<>();
        IIngredientRegistry ingredientRegistry = JFMUYInternalPlugin.ingredientRegistry;
        if (ingredientRegistry != null) {
            IIngredientHelper<DebugIngredient> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
            String displayName = ingredientHelper.getDisplayName(ingredient);
            tooltip.add(displayName);
            tooltip.add(EnumChatFormatting.GRAY + "debug ingredient");
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, DebugIngredient ingredient) {
        return minecraft.fontRenderer;
    }
}
