package ruiseki.jfmuy.gui.ingredients;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {

    public static void enableGuiItemRender() {
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void disableGuiItemRender() {
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
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

    @SuppressWarnings("unchecked")
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
