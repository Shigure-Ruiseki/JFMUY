package ruiseki.jfmuy.plugins.vanilla.ingredients.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {

    private static final RenderItem itemRender = RenderItem.getInstance();

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        if (ingredient != null) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            FontRenderer font = getFontRenderer(minecraft, ingredient);

            itemRender
                .renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), ingredient, xPosition, yPosition);
            itemRender
                .renderItemOverlayIntoGUI(font, minecraft.getTextureManager(), ingredient, xPosition, yPosition, null);

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glPopMatrix();
        }
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, boolean tooltipFlag) {
        net.minecraft.entity.player.EntityPlayer player = minecraft.thePlayer;
        List<String> list;
        try {
            boolean showAdvanced = minecraft.gameSettings.advancedItemTooltips;

            list = ingredient.getTooltip(player, showAdvanced);
        } catch (RuntimeException | LinkageError e) {
            String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
            Log.get()
                .error("Failed to get tooltip: {}", itemStackInfo, e);
            list = new ArrayList<>();
            list.add(
                net.minecraft.util.EnumChatFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
            return list;
        }

        net.minecraft.item.EnumRarity rarity;
        try {
            rarity = ingredient.getItem()
                .getRarity(ingredient);
        } catch (RuntimeException | LinkageError e) {
            String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
            Log.get()
                .error("Failed to get rarity: {}", itemStackInfo, e);
            rarity = net.minecraft.item.EnumRarity.common;
        }

        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, rarity.rarityColor + list.get(k));
            } else {
                list.set(k, net.minecraft.util.EnumChatFormatting.GRAY + list.get(k));
            }
        }

        return list;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        FontRenderer fontRenderer = ingredient.getItem()
            .getFontRenderer(ingredient);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;
    }
}
