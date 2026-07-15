package ruiseki.jfmuy.render;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.base.Joiner;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.color.ColorNamer;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.startup.ForgeModIdHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.helper.GuiHelpers;

public class IngredientRenderer<T> {

    private static final int BLACKLIST_COLOR = Color.red.getRGB();
    private static final Rectangle DEFAULT_AREA = new Rectangle(0, 0, 16, 16);

    protected final IIngredientListElement<T> element;
    protected Rectangle area = DEFAULT_AREA;
    protected int padding;

    public IngredientRenderer(IIngredientListElement<T> element) {
        this.element = element;
    }

    public void setArea(Rectangle area) {
        this.area = area;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public IIngredientListElement<T> getElement() {
        return element;
    }

    public Rectangle getArea() {
        return area;
    }

    public void renderSlow() {
        if (Config.isEditModeEnabled()) {
            renderEditMode(element, area, padding);
        }

        try {
            IIngredientRenderer<T> ingredientRenderer = element.getIngredientRenderer();
            T ingredient = element.getIngredient();
            ingredientRenderer.render(Minecraft.getMinecraft(), area.x + padding, area.y + padding, ingredient);
        } catch (RuntimeException | LinkageError e) {
            throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
        }
    }

    /**
     * Matches the highlight code in {@link net.minecraft.client.gui.inventory.GuiContainer#drawScreen(int, int, float)}
     */
    public void drawHighlight() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        GuiHelpers
            .drawGradientRect(0, area.x, area.y, area.x + area.width, area.y + area.height, 0x80FFFFFF, 0x80FFFFFF);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
    }

    public void drawTooltip(Minecraft minecraft, int mouseX, int mouseY) {
        T ingredient = element.getIngredient();
        IIngredientRenderer<T> ingredientRenderer = element.getIngredientRenderer();
        List<String> tooltip = getTooltip(minecraft, element);
        FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);

        IIngredientHelper<T> ingredientHelper = element.getIngredientHelper();
        ItemStack itemStack = ingredientHelper.getCheatItemStack(ingredient);
        TooltipRenderer.drawHoveringText(itemStack, minecraft, tooltip, mouseX, mouseY, fontRenderer);
    }

    protected static <V> void renderEditMode(IIngredientListElement<V> element, Rectangle area, int padding) {
        V ingredient = element.getIngredient();
        IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();

        if (Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
            GuiScreen.drawRect(
                area.x + padding,
                area.y + padding,
                area.x + 16 + padding,
                area.y + 16 + padding,
                BLACKLIST_COLOR);
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
    }

    private static <V> List<String> getTooltip(Minecraft minecraft, IIngredientListElement<V> element) {
        List<String> tooltip = getIngredientTooltipSafe(minecraft, element);
        V ingredient = element.getIngredient();
        IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
        tooltip = ForgeModIdHelper.getInstance()
            .addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);

        int maxWidth = Reference.MAX_TOOLTIP_WIDTH;
        for (String tooltipLine : tooltip) {
            int width = minecraft.fontRenderer.getStringWidth(tooltipLine);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        if (Config.getColorSearchMode() != Config.SearchMode.DISABLED) {
            addColorSearchInfoToTooltip(minecraft, element, tooltip, maxWidth);
        }

        if (Config.isEditModeEnabled()) {
            addEditModeInfoToTooltip(minecraft, tooltip, maxWidth);
        }

        return tooltip;
    }

    private static <V> List<String> getIngredientTooltipSafe(Minecraft minecraft, IIngredientListElement<V> element) {
        IIngredientRenderer<V> ingredientRenderer = element.getIngredientRenderer();
        V ingredient = element.getIngredient();
        try {
            boolean tooltipFlag = minecraft.gameSettings.advancedItemTooltips;
            return ingredientRenderer.getTooltip(minecraft, ingredient, tooltipFlag);
        } catch (RuntimeException | LinkageError e) {
            Log.get()
                .error("Tooltip crashed.", e);
        }

        List<String> tooltip = new ArrayList<>();
        tooltip.add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
        return tooltip;
    }

    private static <V> void addColorSearchInfoToTooltip(Minecraft minecraft, IIngredientListElement<V> element,
        List<String> tooltip, int maxWidth) {
        ColorNamer colorNamer = Internal.getColorNamer();

        V ingredient = element.getIngredient();
        IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
        Iterable<Color> colors = ingredientHelper.getColors(ingredient);
        Collection<String> colorNames = colorNamer.getColorNames(colors, false);
        if (!colorNames.isEmpty()) {
            String colorNamesString = Joiner.on(", ")
                .join(colorNames);
            String colorNamesLocalizedString = EnumChatFormatting.GRAY
                + Translator.translateToLocalFormatted("jfmuy.tooltip.item.colors", colorNamesString);
            tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(colorNamesLocalizedString, maxWidth));
        }
    }

    private static void addEditModeInfoToTooltip(Minecraft minecraft, List<String> tooltip, int maxWidth) {
        tooltip.add("");
        tooltip.add(EnumChatFormatting.DARK_GREEN + Translator.translateToLocal("gui.jfmuy.editMode.description"));

        String hideMessage = EnumChatFormatting.GRAY
            + Translator.translateToLocal("gui.jfmuy.editMode.description.hide")
                .replace("%CTRL", "key.jfmuy.ctrl");
        tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(hideMessage, maxWidth));

        String hideWildMessage = EnumChatFormatting.GRAY
            + Translator.translateToLocal("gui.jfmuy.editMode.description.hide.wild")
                .replace("%CTRL", "key.jfmuy.ctrl");
        tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(hideWildMessage, maxWidth));
    }

}
