package ruiseki.jfmuy.plugins.vanilla.ingredients;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.FakeClientPlayer;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {

    private static final RenderItem itemRender = new RenderItem();

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        if (ingredient != null) {
            RenderHelper.enableGUIStandardItemLighting();
            FontRenderer font = getFontRenderer(minecraft, ingredient);

            itemRender
                .renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), ingredient, xPosition, yPosition);

            itemRender
                .renderItemOverlayIntoGUI(font, minecraft.getTextureManager(), ingredient, xPosition, yPosition, null);

            GL11.glDisable(GL11.GL_BLEND);
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient) {
        EntityPlayer player = minecraft.thePlayer;
        if (player == null) {
            player = FakeClientPlayer.getInstance();
        }

        List<String> list;
        try {
            list = ingredient.getTooltip(player, minecraft.gameSettings.advancedItemTooltips);
        } catch (RuntimeException e) {
            String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
            Log.error("Failed to get tooltip: {}", itemStackInfo, e);
            list = new ArrayList<>();
            list.add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
            return list;
        } catch (LinkageError e) {
            String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
            Log.error("Failed to get tooltip: {}", itemStackInfo, e);
            list = new ArrayList<String>();
            list.add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
            return list;
        }

        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, ingredient.getRarity().rarityColor + list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + list.get(k));
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
