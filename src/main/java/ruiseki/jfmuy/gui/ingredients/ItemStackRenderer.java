package ruiseki.jfmuy.gui.ingredients;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.util.Translator;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {

    private static final String oreDictionaryIngredient = Translator.translateToLocal("jfmuy.tooltip.recipe.ore.dict");

    @Nullable
    private String oreDictEquivalent;

    @Override
    public void setIngredients(@Nonnull Collection<ItemStack> itemStacks) {
        oreDictEquivalent = Internal.getStackHelper()
            .getOreDictEquivalent(itemStacks);
    }

    @Override
    public void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        FontRenderer font = getFontRenderer(minecraft, itemStack);
        RenderItem renderItem = RenderItem.getInstance();
        renderItem.renderItemAndEffectIntoGUI(
            minecraft.fontRenderer,
            minecraft.getTextureManager(),
            itemStack,
            xPosition,
            yPosition);
        renderItem.renderItemOverlayIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition, yPosition, null);
    }

    @Nonnull
    @Override
    public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
        List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, itemStack.getRarity().rarityColor + list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + list.get(k));
            }
        }

        if (oreDictEquivalent != null) {
            final String acceptsAny = String.format(oreDictionaryIngredient, oreDictEquivalent);
            list.add(EnumChatFormatting.GRAY + acceptsAny);
        }

        return list;
    }

    @Override
    public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
        FontRenderer fontRenderer = itemStack.getItem()
            .getFontRenderer(itemStack);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;
    }
}
