package ruiseki.jfmuy.plugins.vanilla.ingredients.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {

    private static final RenderItem itemRender = RenderItem.getInstance();

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        if (ingredient != null) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            FontRenderer font = getFontRenderer(minecraft, ingredient);

            itemRender
                .renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), ingredient, xPosition, yPosition);
            if (ingredient.stackSize > 64) {
                renderCustomStackSize(font, ingredient, xPosition, yPosition);
            } else {
                itemRender.renderItemOverlayIntoGUI(
                    font,
                    minecraft.getTextureManager(),
                    ingredient,
                    xPosition,
                    yPosition,
                    null);
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GlStateManager.disableDepth();

            GL11.glPopMatrix();
        }
    }

    /**
     * Custom method for rendering item stack count
     *
     * @param font      The font renderer
     * @param stack     The item stack
     * @param xPosition X coordinate
     * @param yPosition Y coordinate
     */
    private void renderCustomStackSize(FontRenderer font, ItemStack stack, int xPosition, int yPosition) {
        int count = stack.stackSize;
        String countText = formatStackCount(count);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        boolean shouldScale = count > 99;
        if (shouldScale) {
            GlStateManager.scale(0.5F, 0.5F, 1.0F);
        }

        int x = shouldScale ? (xPosition + 16) * 2 - font.getStringWidth(countText)
            : xPosition + 17 - font.getStringWidth(countText);
        int y = shouldScale ? (yPosition + 16) * 2 - 8 : yPosition + 17 - 8;

        font.drawStringWithShadow(countText, x, y, 0xFFFFFF);

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

    /**
     * Formats the stack count for display
     */
    private String formatStackCount(int count) {
        if (count <= 99) {
            return String.valueOf(count);
        }

        if (count <= 9999) {
            return String.valueOf(count);
        }

        if (count <= 999999) {
            float k = count / 1000f;
            return String.format(k % 1 == 0 ? "%.0fk" : "%.1fk", k);
        }

        if (count <= 999999999) {
            float m = count / 1000000f;
            return String.format(m % 1 == 0 ? "%.0fm" : "%.1fm", m);
        }

        float g = count / 1000000000f;
        return String.format(g % 1 == 0 ? "%.0fg" : "%.1fg", g);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, boolean tooltipFlag) {
        EntityPlayer player = minecraft.thePlayer;
        List<String> list = new ArrayList<>();

        if (player == null && ingredient.getItem() instanceof ItemMap) {
            list.add(ingredient.getDisplayName());
        } else {
            try {
                boolean showAdvanced = minecraft.gameSettings.advancedItemTooltips;
                list = ingredient.getTooltip(player, showAdvanced);
            } catch (RuntimeException | LinkageError e) {
                String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
                Log.get()
                    .warn("Failed to get tooltip for {} during startup indexing (Player is null).", itemStackInfo);

                list.clear();
                try {
                    list.add(ingredient.getDisplayName());
                } catch (Exception ex) {
                    list.add(
                        net.minecraft.util.EnumChatFormatting.RED
                            + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
                }
            }
        }

        EnumRarity rarity;
        try {
            rarity = ingredient.getItem()
                .getRarity(ingredient);
        } catch (RuntimeException | LinkageError e) {
            String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
            Log.get()
                .error("Failed to get rarity: {}", itemStackInfo, e);
            rarity = EnumRarity.common;
        }

        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, rarity.rarityColor + list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + list.get(k));
            }
        }

        return list;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        if (ingredient.getItem() == null) return minecraft.fontRenderer;
        FontRenderer fontRenderer = ingredient.getItem()
            .getFontRenderer(ingredient);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;
    }
}
