package ruiseki.jfmuy.gui.ingredients;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

@SuppressWarnings("deprecation")
public class GuiItemStackFast {

    private final int xPosition;
    private final int yPosition;
    private final int width;
    private final int height;
    private final int padding;

    private ItemStack itemStack;

    public GuiItemStackFast(int xPosition, int yPosition, int padding) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.padding = padding;
        this.width = 16 + (2 * padding);
        this.height = 16 + (2 * padding);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void clear() {
        this.itemStack = null;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return (itemStack != null) && (mouseX >= xPosition)
            && (mouseY >= yPosition)
            && (mouseX < xPosition + width)
            && (mouseY < yPosition + height);
    }

    public void renderSlow() {
        if (itemStack == null || itemStack.getItem() == null) {
            return;
        }

        if (Config.isEditModeEnabled()) {
            renderEditMode();
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer font = getFontRenderer(minecraft, itemStack);
        RenderItem renderItem = RenderItem.getInstance();
        renderItem.renderItemAndEffectIntoGUI(
            minecraft.fontRenderer,
            minecraft.getTextureManager(),
            itemStack,
            xPosition + padding,
            yPosition + padding);
        renderItem.renderItemOverlayIntoGUI(
            font,
            minecraft.getTextureManager(),
            itemStack,
            xPosition + padding,
            yPosition + padding,
            null);
    }

    private void renderEditMode() {
        if (Config.isItemOnConfigBlacklist(itemStack, false)) {
            GuiScreen.drawRect(
                xPosition + padding,
                yPosition + padding,
                xPosition + 8 + padding,
                yPosition + 16 + padding,
                0xFFFFFF00);
        }
        if (Config.isItemOnConfigBlacklist(itemStack, true)) {
            GuiScreen.drawRect(
                xPosition + 8 + padding,
                yPosition + padding,
                xPosition + 16 + padding,
                yPosition + 16 + padding,
                0xFFFF0000);
        }
    }

    public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
        if (itemStack.getItem() == null) {
            return minecraft.fontRenderer;
        }
        FontRenderer fontRenderer = itemStack.getItem()
            .getFontRenderer(itemStack);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;
    }

    public void drawHovered(Minecraft minecraft, int mouseX, int mouseY) {
        try {
            Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);

            renderSlow();

            List<String> tooltip = getTooltip(minecraft, itemStack);
            FontRenderer fontRenderer = getFontRenderer(minecraft, itemStack);
            TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
        } catch (RuntimeException e) {
            Log.error("Exception when rendering tooltip on {}.", itemStack, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
        List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, itemStack.getRarity().rarityColor + list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + list.get(k));
            }
        }

        if (Config.isEditModeEnabled()) {
            list.add("");
            list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jfmuy.editMode.description"));
            if (Config.isItemOnConfigBlacklist(itemStack, false)) {
                String description = EnumChatFormatting.YELLOW
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.show");
                list.addAll(
                    minecraft.fontRenderer.listFormattedStringToWidth(description, Reference.MAX_TOOLTIP_WIDTH));
            } else {
                String description = EnumChatFormatting.YELLOW
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.hide");
                list.addAll(
                    minecraft.fontRenderer.listFormattedStringToWidth(description, Reference.MAX_TOOLTIP_WIDTH));
            }

            if (Config.isItemOnConfigBlacklist(itemStack, true)) {
                String description = EnumChatFormatting.RED
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.show.wild");
                list.addAll(
                    minecraft.fontRenderer.listFormattedStringToWidth(description, Reference.MAX_TOOLTIP_WIDTH));
            } else {
                String description = EnumChatFormatting.RED
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.hide.wild");
                list.addAll(
                    minecraft.fontRenderer.listFormattedStringToWidth(description, Reference.MAX_TOOLTIP_WIDTH));
            }
        }

        return list;
    }
}
