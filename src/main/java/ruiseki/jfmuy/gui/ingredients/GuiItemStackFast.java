package ruiseki.jfmuy.gui.ingredients;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Joiner;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.util.Translator;

public class GuiItemStackFast {

    private static final int blacklistItemColor = Color.yellow.getRGB();
    private static final int blacklistWildColor = Color.red.getRGB();
    private static final int blacklistModColor = Color.blue.getRGB();

    private static final RenderItem itemRender = new RenderItem();

    @Nonnull
    private final Rectangle area;
    private final int padding;

    @Nullable
    private ItemStack itemStack;

    public GuiItemStackFast(int xPosition, int yPosition, int padding) {
        this.padding = padding;
        final int size = 16 + (2 * padding);
        this.area = new Rectangle(xPosition, yPosition, size, size);
    }

    @Nonnull
    public Rectangle getArea() {
        return area;
    }

    public void setItemStack(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void clear() {
        this.itemStack = null;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return (itemStack != null) && area.contains(mouseX, mouseY);
    }

    public void renderItemAndEffectIntoGUI() {
        renderSlow();
    }

    public void renderSlow() {
        if (itemStack == null) {
            return;
        }

        if (Config.isEditModeEnabled()) {
            renderEditMode();
        }

        Minecraft minecraft = Minecraft.getMinecraft();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        itemRender.renderItemAndEffectIntoGUI(
            minecraft.fontRenderer,
            minecraft.getTextureManager(),
            itemStack,
            area.x + padding,
            area.y + padding);

        GL11.glDisable(GL11.GL_BLEND);
    }

    public void renderOverlay(Minecraft minecraft) {
        if (itemStack == null) {
            return;
        }
        FontRenderer font = getFontRenderer(minecraft, itemStack);
        itemRender.renderItemOverlayIntoGUI(
            font,
            minecraft.getTextureManager(),
            itemStack,
            area.x + padding,
            area.y + padding,
            null);
    }

    private void renderEditMode() {
        if (itemStack == null) {
            return;
        }

        if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.ITEM)) {
            GuiScreen.drawRect(
                area.x + padding,
                area.y + padding,
                area.x + 8 + padding,
                area.y + 16 + padding,
                blacklistItemColor);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.WILDCARD)) {
            GuiScreen.drawRect(
                area.x + 8 + padding,
                area.y + padding,
                area.x + 16 + padding,
                area.y + 16 + padding,
                blacklistWildColor);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.MOD_ID)) {
            GuiScreen.drawRect(
                area.x + padding,
                area.y + 8 + padding,
                area.x + 16 + padding,
                area.y + 16 + padding,
                blacklistModColor);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    @Nonnull
    public static FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
        Item item = itemStack.getItem();
        FontRenderer fontRenderer = item.getFontRenderer(itemStack);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;
    }

    public void drawHovered(Minecraft minecraft) {
        if (itemStack == null) {
            return;
        }

        renderSlow();
        renderOverlay(minecraft);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        Gui.drawRect(area.x, area.y, area.x + area.width, area.y + area.height, 0x7FFFFFFF);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void drawTooltip(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
        if (itemStack == null) {
            return;
        }
        List<String> tooltip = getTooltip(minecraft, itemStack);
        FontRenderer fontRenderer = getFontRenderer(minecraft, itemStack);
        TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
        List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, itemStack.getRarity().rarityColor + list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + list.get(k));
            }
        }

        int maxWidth = Reference.MAX_TOOLTIP_WIDTH;
        for (String tooltipLine : list) {
            int width = minecraft.fontRenderer.getStringWidth(tooltipLine);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        if (Config.isColorSearchEnabled()) {
            Collection<String> colorNames = Internal.getColorNamer()
                .getColorNames(itemStack);
            if (!colorNames.isEmpty()) {
                String colorNamesString = Joiner.on(", ")
                    .join(colorNames);
                String colorNamesLocalizedString = EnumChatFormatting.GRAY
                    + Translator.translateToLocalFormatted("jfmuy.tooltip.item.colors", colorNamesString);
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(colorNamesLocalizedString, maxWidth));
            }
        }

        if (Config.isEditModeEnabled()) {
            list.add("");
            list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jfmuy.editMode.description"));
            if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.ITEM)) {
                String description = EnumChatFormatting.YELLOW
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.show");
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            } else {
                String description = EnumChatFormatting.YELLOW
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.hide");
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            }

            if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.WILDCARD)) {
                String description = EnumChatFormatting.RED
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.show.wild");
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            } else {
                String description = EnumChatFormatting.RED
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.hide.wild");
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            }

            if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.MOD_ID)) {
                String description = EnumChatFormatting.BLUE
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.show.mod.id");
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            } else {
                String description = EnumChatFormatting.BLUE
                    + Translator.translateToLocal("gui.jfmuy.editMode.description.hide.mod.id");
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            }
        }

        return list;
    }
}
