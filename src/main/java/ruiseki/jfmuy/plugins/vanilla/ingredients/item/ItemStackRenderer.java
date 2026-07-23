package ruiseki.jfmuy.plugins.vanilla.ingredients.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
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
import ruiseki.jfmuy.util.CountUtil;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {

    protected static final int SLOT_SIZE = 18;
    protected static final int MAX_COLUMNS = 11;
    protected static final int MARGIN_TOP = 2;

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
                CountUtil.renderCountString(font, ingredient.stackSize, xPosition, yPosition, true);
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

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient, int amount) {
        if (ingredient != null) ingredient.stackSize = amount;
        render(minecraft, xPosition, yPosition, ingredient);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, boolean tooltipFlag) {
        EntityPlayer player = minecraft.thePlayer;
        List<String> list = new ArrayList<>();

        if (player == null && ingredient.getItem() instanceof ItemMap) {
            list.add(ingredient.getDisplayName());
        } else {
            try {
                list = ingredient.getTooltip(player, tooltipFlag);
            } catch (RuntimeException | LinkageError e) {
                String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
                Log.get()
                    .warn("Failed to get tooltip for {} during startup indexing (Player is null).", itemStackInfo);

                list.clear();
                try {
                    list.add(ingredient.getDisplayName());
                } catch (Exception ex) {
                    list.add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
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
    public ExtraSize renderTooltipExtras(Minecraft minecraft, int mouseX, int mouseY, List<ItemStack> allIngredients,
        int activeIndex, boolean isDrawingPass) {
        if (allIngredients == null || allIngredients.isEmpty() || allIngredients.size() == 1) {
            return null;
        }

        int totalItems = allIngredients.size();
        int columns = Math.min(totalItems, MAX_COLUMNS);
        int rows = (totalItems + MAX_COLUMNS - 1) / MAX_COLUMNS;

        int extraWidth = columns * SLOT_SIZE;
        int extraHeight = rows * SLOT_SIZE + MARGIN_TOP;

        if (isDrawingPass) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

            for (int i = 0; i < totalItems; i++) {
                ItemStack stack = allIngredients.get(i);
                if (stack == null) continue;

                int currentX = (i % MAX_COLUMNS) * SLOT_SIZE;
                int currentY = MARGIN_TOP + (i / MAX_COLUMNS) * SLOT_SIZE;

                if (i == activeIndex) {
                    GL11.glDisable(GL11.GL_LIGHTING);
                    Gui.drawRect(
                        currentX - 1,
                        currentY - 1,
                        currentX + SLOT_SIZE - 1,
                        currentY + SLOT_SIZE - 1,
                        0x66555555);
                    GL11.glEnable(GL11.GL_LIGHTING);
                }

                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 0.0F, 300.0F);

                FontRenderer fontRenderer = getFontRenderer(minecraft, stack);
                itemRender
                    .renderItemAndEffectIntoGUI(fontRenderer, minecraft.getTextureManager(), stack, currentX, currentY);
                itemRender.renderItemOverlayIntoGUI(
                    fontRenderer,
                    minecraft.getTextureManager(),
                    stack,
                    currentX,
                    currentY,
                    null);

                GL11.glPopMatrix();
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glPopAttrib();
        }

        return new ExtraSize(extraWidth, extraHeight);
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
