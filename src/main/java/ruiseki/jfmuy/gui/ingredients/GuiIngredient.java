package ruiseki.jfmuy.gui.ingredients;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiIngredient;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.startup.ForgeModIdHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.helper.ItemHelpers;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {

    private static final String oreDictionaryIngredient = Translator.translateToLocal("jfmuy.tooltip.recipe.ore.dict");

    private final int slotIndex;
    private final boolean input;

    private final Rectangle rect;
    private final int xPadding;
    private final int yPadding;

    private final CycleTimer cycleTimer;
    private final List<T> displayIngredients = new ArrayList<>(); // ingredients, taking focus into account
    private final List<T> allIngredients = new ArrayList<>(); // all ingredients, ignoring focus
    private final IIngredientRenderer<T> ingredientRenderer;
    private final IIngredientHelper<T> ingredientHelper;
    @Nullable
    private ITooltipCallback<T> tooltipCallback;
    @Nullable
    private IDrawable background;
    @Nullable
    private IngredientListPreview ingredientPreview;
    private boolean ingredientPreviewInvalidated = true;

    private boolean enabled;

    public GuiIngredient(int slotIndex, boolean input, IIngredientRenderer<T> ingredientRenderer,
        IIngredientHelper<T> ingredientHelper, Rectangle rect, int xPadding, int yPadding, int cycleOffset) {
        this.ingredientRenderer = ingredientRenderer;
        this.ingredientHelper = ingredientHelper;

        this.slotIndex = slotIndex;
        this.input = input;

        this.rect = rect;
        this.xPadding = xPadding;
        this.yPadding = yPadding;

        this.cycleTimer = new CycleTimer(cycleOffset);
    }

    public Rectangle getRect() {
        return rect;
    }

    public boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY) {
        return enabled && (mouseX >= xOffset + rect.x)
            && (mouseY >= yOffset + rect.y)
            && (mouseX < xOffset + rect.x + rect.width)
            && (mouseY < yOffset + rect.y + rect.height);
    }

    @Nullable
    @Override
    public T getDisplayedIngredient() {
        return cycleTimer.getCycledItem(displayIngredients);
    }

    @Override
    public List<T> getAllIngredients() {
        return allIngredients;
    }

    public void set(@Nullable List<T> ingredients, @Nullable IFocus<T> focus) {
        this.displayIngredients.clear();
        this.allIngredients.clear();
        List<T> displayIngredients;
        if (ingredients == null) {
            displayIngredients = Collections.emptyList();
        } else {
            displayIngredients = this.ingredientHelper.expandSubtypes(ingredients);
        }

        T match = getMatch(displayIngredients, focus);
        if (match != null) {
            this.displayIngredients.add(match);
        } else {
            displayIngredients = filterOutHidden(displayIngredients);
            this.displayIngredients.addAll(displayIngredients);
        }

        if (ingredients != null) {
            this.allIngredients.addAll(ingredients);
        }
        enabled = !this.displayIngredients.isEmpty();
        // The preview is built lazily on first hover: only one slot is ever hovered at a time, so
        // wrapping every ingredient of every slot up front would be wasted work.
        this.ingredientPreview = null;
        this.ingredientPreviewInvalidated = true;
    }

    /**
     * Every ingredient this slot accepts, as a tooltip grid, or null when there is nothing to show.
     * Built on first request and cached until the slot is set again.
     */
    @Nullable
    public IngredientListPreview getIngredientPreview() {
        if (ingredientPreviewInvalidated) {
            ingredientPreviewInvalidated = false;
            // A focused slot collapses displayIngredients down to the single match, which leaves
            // fewer than two entries and so yields no preview.
            ingredientPreview = Config.isRecipeIngredientPreviewEnabled()
                ? IngredientListPreview
                    .create(displayIngredients, ingredientHelper, ingredientRenderer, ForgeModIdHelper.getInstance())
                : null;
        }
        return ingredientPreview;
    }

    private List<T> filterOutHidden(List<T> ingredients) {
        if (ingredients.isEmpty()) {
            return ingredients;
        }
        IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IngredientFilter ingredientFilter = Internal.getIngredientFilter();
        List<T> visible = new ArrayList<>();
        for (T ingredient : ingredients) {
            if (ingredient == null || ingredientRegistry.isIngredientVisible(ingredient, ingredientFilter)) {
                visible.add(ingredient);
            }
            if (visible.size() > 100) {
                return visible;
            }
        }
        if (visible.size() > 0) {
            return visible;
        }
        return ingredients;
    }

    public void setBackground(IDrawable background) {
        this.background = background;
    }

    @Nullable
    private T getMatch(Collection<T> ingredients, @Nullable IFocus<T> focus) {
        if (focus != null && isMode(focus.getMode())) {
            T focusValue = focus.getValue();
            return ingredientHelper.getMatch(ingredients, focusValue);
        }
        return null;
    }

    public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
        this.tooltipCallback = tooltipCallback;
    }

    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        cycleTimer.onDraw();

        if (background != null) {
            background.draw(minecraft, xOffset + rect.x, yOffset + rect.y);
        }

        T value = getDisplayedIngredient();
        try {
            ingredientRenderer.render(minecraft, xOffset + rect.x + xPadding, yOffset + rect.y + yPadding, value);
        } catch (RuntimeException | LinkageError e) {
            if (value != null) {
                throw ErrorUtil.createRenderIngredientException(e, value);
            }
            throw e;
        }
    }

    @Override
    public void drawHighlight(Minecraft minecraft, Color color, int xOffset, int yOffset) {
        int x = rect.x + xOffset + xPadding;
        int y = rect.y + yOffset + yPadding;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        drawRect(x, y, x + rect.width - xPadding * 2, y + rect.height - yPadding * 2, color.getRGB());
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public void drawOverlays(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
        T value = getDisplayedIngredient();
        if (value != null) {
            drawTooltip(minecraft, xOffset, yOffset, mouseX, mouseY, value);
        }
    }

    private void drawTooltip(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
        try {
            GlStateManager.disableDepth();

            RenderHelper.disableStandardItemLighting();
            drawRect(
                xOffset + rect.x + xPadding,
                yOffset + rect.y + yPadding,
                xOffset + rect.x + rect.width - xPadding,
                yOffset + rect.y + rect.height - yPadding,
                0x7FFFFFFF);
            GlStateManager.color(1f, 1f, 1f, 1f);

            boolean tooltipFlag = minecraft.gameSettings.advancedItemTooltips;
            List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value, tooltipFlag);
            tooltip = ForgeModIdHelper.getInstance()
                .addModNameToIngredientTooltip(tooltip, value, ingredientHelper);

            if (tooltipCallback != null) {
                tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
            }

            int activeIndex = 0;
            if (!this.displayIngredients.isEmpty()) {
                activeIndex = this.displayIngredients.indexOf(value);
                if (activeIndex == -1) {
                    activeIndex = 0;
                }
            }

            FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
            ItemStack tooltipStack = ItemHelpers.EMPTY;
            if (value instanceof ItemStack) {
                tooltipStack = (ItemStack) value;
                // noinspection unchecked
                Collection<ItemStack> itemStacks = (Collection<ItemStack>) this.allIngredients;
                String oreDictEquivalent = Internal.getStackHelper()
                    .getOreDictEquivalent(itemStacks);
                if (oreDictEquivalent != null) {
                    final String acceptsAny = String.format(oreDictionaryIngredient, oreDictEquivalent);
                    tooltip.add(EnumChatFormatting.GRAY + acceptsAny);
                }
            }

            int tooltipX = xOffset + mouseX;
            int tooltipY = yOffset + mouseY;
            IngredientListPreview preview = getIngredientPreview();
            if (preview != null && !GuiScreen.isShiftKeyDown()) {
                // Shift pins the tooltip, so the hint only makes sense while it is not held.
                tooltip.add(Translator.translateToLocal("jfmuy.tooltip.recipe.ingredient_pin"));
            }
            if (preview == null) {
                if (value instanceof ItemStack) {
                    TooltipRenderer
                        .drawHoveringText(tooltipStack, minecraft, tooltip, tooltipX, tooltipY, fontRenderer);
                } else {
                    TooltipRenderer.drawHoveringText(minecraft, tooltip, tooltipX, tooltipY, fontRenderer);
                }
            } else {
                Rectangle bound = TooltipRenderer.drawHoveringTextAndItems(
                    tooltipStack,
                    minecraft,
                    tooltip,
                    Collections.singletonList(preview.getRenderer()),
                    tooltipX,
                    tooltipY,
                    -1,
                    fontRenderer,
                    IngredientListPreview.GRID_WIDTH);
                // Kept so a pinned tooltip knows which part of the screen it is covering.
                preview.setTooltipBounds(bound);
            }

            GlStateManager.enableDepth();
        } catch (RuntimeException e) {
            Log.get()
                .error("Exception when rendering tooltip on {}.", value, e);
        }
    }

    @Override
    public boolean isInput() {
        return input;
    }

    public boolean isMode(IFocus.Mode mode) {
        return (input && mode == IFocus.Mode.INPUT) || (!input && mode == IFocus.Mode.OUTPUT);
    }
}
