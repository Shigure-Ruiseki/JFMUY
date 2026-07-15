package ruiseki.jfmuy.plugins.jfmuy.ingredients;

import java.awt.Color;
import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.util.CommandUtilServer;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {

    @Nullable
    @Override
    public DebugIngredient getMatch(Iterable<DebugIngredient> ingredients, DebugIngredient ingredientToMatch) {
        for (DebugIngredient debugIngredient : ingredients) {
            if (debugIngredient.getNumber() == ingredientToMatch.getNumber()) {
                return debugIngredient;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(DebugIngredient ingredient) {
        return "JFMUY Debug Item #" + ingredient.getNumber();
    }

    @Override
    public String getUniqueId(DebugIngredient ingredient) {
        return "JFMUY_debug_" + ingredient.getNumber();
    }

    @Override
    public String getWildcardId(DebugIngredient ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(DebugIngredient ingredient) {
        return Reference.MOD_ID;
    }

    @Override
    public Iterable<Color> getColors(DebugIngredient ingredient) {
        return Collections.emptyList();
    }

    @Override
    public String getResourceId(DebugIngredient ingredient) {
        return "debug_" + ingredient.getNumber();
    }

    @Override
    public ItemStack getCheatItemStack(DebugIngredient ingredient) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            CommandUtilServer.writeChatMessage(player, "Debug ingredients cannot be cheated", EnumChatFormatting.RED);
        }
        return null;
    }

    @Override
    public DebugIngredient copyIngredient(DebugIngredient ingredient) {
        return ingredient.copy();
    }

    @Override
    public String getErrorInfo(@Nullable DebugIngredient ingredient) {
        if (ingredient == null) {
            return "debug ingredient: null";
        }
        return getDisplayName(ingredient);
    }
}
