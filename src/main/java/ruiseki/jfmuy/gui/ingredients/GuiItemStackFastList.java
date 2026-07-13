package ruiseki.jfmuy.gui.ingredients;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;

import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.util.ItemStackElement;

public class GuiItemStackFastList {

    private final List<GuiItemStackFast> renderItemsAll = new ArrayList<>();

    public void clear() {

        renderItemsAll.clear();

    }

    public void add(GuiItemStackFast guiItemStack) {

        renderItemsAll.add(guiItemStack);

    }

    public void set(int i, List<ItemStackElement> itemList) {

        for (GuiItemStackFast guiItemStack : renderItemsAll) {

            if (i >= itemList.size()) {

                guiItemStack.clear();

            } else {

                guiItemStack.setItemStack(
                    itemList.get(i)
                        .getItemStack());

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

    public GuiItemStackFast render(@Nullable GuiItemStackFast hovered, @Nonnull Minecraft minecraft,
        boolean isMouseOver, int mouseX, int mouseY) {

        if (isMouseOver && hovered == null) {

            hovered = getHovered(mouseX, mouseY);

        }

        ItemStackRenderer.enableGuiItemRender();

        RenderItem renderItem = RenderItem.getInstance();

        renderItem.zLevel += 50.0F;

        for (GuiItemStackFast guiItemStack : renderItemsAll) {

            if (hovered != guiItemStack && guiItemStack.getItemStack() != null) {

                guiItemStack.renderSlow();

            }

        }

        renderItem.zLevel -= 50.0F;

        ItemStackRenderer.disableGuiItemRender();

        return hovered;

    }

}
