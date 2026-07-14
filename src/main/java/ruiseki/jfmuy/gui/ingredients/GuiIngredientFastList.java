package ruiseki.jfmuy.gui.ingredients;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.input.ClickedIngredient;

public class GuiIngredientFastList {

    private final List<GuiIngredientFast> renderAll = new ArrayList<>();

    private final List<GuiIngredientFast> renderItems = new ArrayList<GuiIngredientFast>();
    private final List<GuiIngredientFast> renderOther = new ArrayList<GuiIngredientFast>();

    private static final RenderItem itemRender = new RenderItem();

    private final IIngredientRegistry ingredientRegistry;

    public GuiIngredientFastList(IIngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
    }

    public void clear() {
        renderAll.clear();
        renderItems.clear();
        renderOther.clear();
    }

    public int size() {
        return renderAll.size();
    }

    public void add(GuiIngredientFast guiItemStack) {
        renderAll.add(guiItemStack);
    }

    public List<GuiIngredientFast> getAllGuiIngredients() {
        return renderAll;
    }

    public void set(int i, List<IIngredientListElement> itemList) {
        renderItems.clear();
        renderOther.clear();

        for (GuiIngredientFast guiItemStack : renderAll) {
            if (i >= itemList.size()) {
                guiItemStack.clear();
            } else {
                Object ingredient = itemList.get(i)
                    .getIngredient();
                set(guiItemStack, ingredient);
            }
            i++;
        }
    }

    private <V> void set(GuiIngredientFast guiItemStack, V ingredient) {
        guiItemStack.setIngredient(ingredient);

        if (ingredient instanceof ItemStack) {
            // FIX 1.7.10: Loại bỏ hoàn toàn IBakedModel checker, đưa trực tiếp vào renderItems
            renderItems.add(guiItemStack);
        } else {
            renderOther.add(guiItemStack);
        }
    }

    @Nullable
    public ClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        GuiIngredientFast hovered = getHovered(mouseX, mouseY);
        if (hovered != null) {
            Object ingredient = hovered.getIngredient();
            if (ingredient != null) {
                return new ClickedIngredient<Object>(ingredient);
            }
        }
        return null;
    }

    @Nullable
    private GuiIngredientFast getHovered(int mouseX, int mouseY) {
        for (GuiIngredientFast guiItemStack : renderAll) {
            if (guiItemStack.isMouseOver(mouseX, mouseY)) {
                return guiItemStack;
            }
        }
        return null;
    }

    /**
     * Renders all ItemStacks and returns hovered gui item stack for later render pass
     */
    @Nullable
    public GuiIngredientFast render(Minecraft minecraft, boolean isMouseOver, int mouseX, int mouseY) {
        GuiIngredientFast hovered = null;
        if (isMouseOver) {
            hovered = getHovered(mouseX, mouseY);
        }

        RenderHelper.enableGUIStandardItemLighting();

        TextureManager textureManager = minecraft.getTextureManager();
        itemRender.zLevel += 50.0F;

        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        // TODO: add blur mipmap
        // textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glEnable(GL11.GL_LIGHTING);
        for (GuiIngredientFast guiItemStack : renderItems) {
            if (hovered != guiItemStack) {
                guiItemStack.renderItemAndEffectIntoGUI();
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_LIGHTING);

        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        // textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

        itemRender.zLevel -= 50.0F;

        for (GuiIngredientFast guiItemStack : renderItems) {
            if (hovered != guiItemStack) {
                guiItemStack.renderOverlay(minecraft);
            }
        }

        for (GuiIngredientFast guiItemStack : renderOther) {
            if (hovered != guiItemStack) {
                guiItemStack.renderSlow();
            }
        }

        RenderHelper.disableStandardItemLighting();

        return hovered;
    }
}
