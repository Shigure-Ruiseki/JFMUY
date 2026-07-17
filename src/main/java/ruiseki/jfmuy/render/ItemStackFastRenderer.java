package ruiseki.jfmuy.render;

import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.okcore.client.renderer.GlStateManager;

public class ItemStackFastRenderer extends IngredientRenderer<ItemStack> {

    public ItemStackFastRenderer(IIngredientListElement<ItemStack> itemStackElement) {
        super(itemStackElement);
    }

    public void renderItemAndEffectIntoGUI() {
        try {
            uncheckedRenderItemAndEffectIntoGUI();
        } catch (RuntimeException | LinkageError e) {
            throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
        }
    }

    private void uncheckedRenderItemAndEffectIntoGUI() {
        if (Config.isEditModeEnabled()) {
            renderEditMode(element, area, padding);
            GL11.glEnable(GL11.GL_BLEND);
        }

        ItemStack itemStack = element.getIngredient();
        if (itemStack == null || itemStack.getItem() == null) {
            return;
        }

        renderItemAndEffectIntoGUI(Minecraft.getMinecraft(), itemStack, area.x + padding, area.y + padding);
    }

    public static void renderItemAndEffectIntoGUI(Minecraft minecraft, ItemStack itemStack, int x, int y) {
        RenderItem renderItem = RenderItem.getInstance();

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();

        try {
            renderItem
                .renderItemAndEffectIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(), itemStack, x, y);
        } catch (Exception e) {
            throw ErrorUtil.createRenderIngredientException(new RuntimeException(e), itemStack);
        } finally {
            GlStateManager.disableLighting();
            GlStateManager.disableColorMaterial();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        }
    }

    public void renderOverlay() {
        ItemStack itemStack = element.getIngredient();
        try {
            renderOverlay(itemStack, area, padding);
        } catch (RuntimeException | LinkageError e) {
            throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
        }
    }

    private void renderOverlay(ItemStack itemStack, Rectangle area, int padding) {
        if (itemStack == null || itemStack.getItem() == null) {
            return;
        }
        FontRenderer font = getFontRenderer(itemStack);
        RenderItem renderItem = RenderItem.getInstance();

        renderItem.renderItemOverlayIntoGUI(
            font,
            Minecraft.getMinecraft()
                .getTextureManager(),
            itemStack,
            area.x + padding,
            area.y + padding,
            null);
    }

    public static FontRenderer getFontRenderer(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItem() == null) {
            return Minecraft.getMinecraft().fontRenderer;
        }
        Item item = itemStack.getItem();
        FontRenderer fontRenderer = item.getFontRenderer(itemStack);
        if (fontRenderer == null) {
            fontRenderer = Minecraft.getMinecraft().fontRenderer;
        }
        return fontRenderer;
    }
}
