package ruiseki.jfmuy.gui.ingredients;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.util.ItemStackElement;

public class GuiItemStackFastList {

    private final List<GuiItemStackFast> renderItemsAll = new ArrayList<GuiItemStackFast>();
    private static final RenderItem itemRender = new RenderItem();

    public void clear() {
        renderItemsAll.clear();
    }

    public int size() {
        return renderItemsAll.size();
    }

    public void add(GuiItemStackFast guiItemStack) {
        renderItemsAll.add(guiItemStack);
    }

    public void set(int i, List<ItemStackElement> itemList) {
        for (GuiItemStackFast guiItemStack : renderItemsAll) {
            if (i >= itemList.size()) {
                guiItemStack.clear();
            } else {
                ItemStack stack = itemList.get(i)
                    .getItemStack();
                if (stack == null) {
                    guiItemStack.clear();
                } else {
                    guiItemStack.setItemStack(stack);
                }
            }
            i++;
        }
    }

    @Nullable
    public Focus getFocusUnderMouse(int mouseX, int mouseY) {
        GuiItemStackFast hovered = getHovered(mouseX, mouseY);
        if (hovered != null) {
            return new Focus(hovered.getItemStack());
        }
        return null;
    }

    @Nullable
    private GuiItemStackFast getHovered(int mouseX, int mouseY) {
        for (GuiItemStackFast guiItemStack : renderItemsAll) {
            if (guiItemStack.isMouseOver(mouseX, mouseY)) {
                return guiItemStack;
            }
        }
        return null;
    }

    /** renders all ItemStacks and returns hovered gui item stack for later render pass */
    @Nullable
    public GuiItemStackFast render(@Nonnull Minecraft minecraft, boolean isMouseOver, int mouseX, int mouseY) {
        GuiItemStackFast hovered = null;
        if (isMouseOver) {
            hovered = getHovered(mouseX, mouseY);
        }

        RenderHelper.enableGUIStandardItemLighting();

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 50.0F);

        minecraft.getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (GuiItemStackFast guiItemStack : renderItemsAll) {
            if (hovered != guiItemStack) {
                guiItemStack.renderSlow();
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        GL11.glPopMatrix();

        for (GuiItemStackFast guiItemStack : renderItemsAll) {
            if (hovered != guiItemStack) {
                guiItemStack.renderOverlay(minecraft);
            }
        }

        RenderHelper.disableStandardItemLighting();

        return hovered;
    }
}
