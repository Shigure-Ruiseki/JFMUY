package ruiseki.jfmuy.gui.ingredients;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IGuiIngredient;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.util.CycleTimer;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {

    private static final String oreDictionaryIngredient = Translator.translateToLocal("jfmuy.tooltip.recipe.ore.dict");

    private final int slotIndex;
    private final boolean input;

    private final int xPosition;
    private final int yPosition;
    private final int width;
    private final int height;
    private final int xPadding;
    private final int yPadding;

    private final CycleTimer cycleTimer;
    private final List<T> displayIngredients = new ArrayList<T>(); // ingredients, taking focus into account
    private final List<T> allIngredients = new ArrayList<T>(); // all ingredients, ignoring focus
    private final IIngredientRenderer<T> ingredientRenderer;
    private final IIngredientHelper<T> ingredientHelper;
    @Nullable
    private ITooltipCallback<T> tooltipCallback;

    private boolean enabled;

    public GuiIngredient(int slotIndex, boolean input, IIngredientRenderer<T> ingredientRenderer,
        IIngredientHelper<T> ingredientHelper, int xPosition, int yPosition, int width, int height, int xPadding,
        int yPadding, int cycleOffset) {
        this.ingredientRenderer = ingredientRenderer;
        this.ingredientHelper = ingredientHelper;

        this.slotIndex = slotIndex;
        this.input = input;

        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.xPadding = xPadding;
        this.yPadding = yPadding;

        this.cycleTimer = new CycleTimer(cycleOffset);
    }

    public boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY) {
        return enabled && (mouseX >= xOffset + xPosition)
            && (mouseY >= yOffset + yPosition)
            && (mouseX < xOffset + xPosition + width)
            && (mouseY < yOffset + yPosition + height);
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

    public void set(T ingredient, IFocus<T> focus) {
        set(Collections.singletonList(ingredient), focus);
    }

    public void set(@Nullable List<T> ingredients, IFocus<T> focus) {
        this.displayIngredients.clear();
        this.allIngredients.clear();
        if (ingredients == null) {
            ingredients = Collections.emptyList();
        } else {
            ingredients = this.ingredientHelper.expandSubtypes(ingredients);
        }

        T match = getMatch(ingredients, focus);
        if (match != null) {
            this.displayIngredients.add(match);
        } else {
            this.displayIngredients.addAll(ingredients);
        }

        this.allIngredients.addAll(ingredients);
        enabled = !this.displayIngredients.isEmpty();
    }

    @Nullable
    private T getMatch(Collection<T> ingredients, IFocus<T> focus) {
        if ((isInput() && focus.getMode() == IFocus.Mode.INPUT)
            || (!isInput() && focus.getMode() == IFocus.Mode.OUTPUT)) {
            T focusValue = focus.getValue();
            if (focusValue != null) {
                return ingredientHelper.getMatch(ingredients, focusValue);
            }
        }
        return null;
    }

    public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
        this.tooltipCallback = tooltipCallback;
    }

    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        cycleTimer.onDraw();

        T value = getDisplayedIngredient();
        ingredientRenderer.render(minecraft, xOffset + xPosition + xPadding, yOffset + yPosition + yPadding, value);
    }

    public void drawHovered(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
        draw(minecraft, xOffset, yOffset);

        T value = getDisplayedIngredient();
        if (value != null) {
            drawTooltip(minecraft, xOffset, yOffset, mouseX, mouseY, value);
        }
    }

    @Override
    public void drawHighlight(Minecraft minecraft, Color color, int xOffset, int yOffset) {
        int x = xPosition + xOffset + xPadding;
        int y = yPosition + yOffset + yPadding;

        GL11.glDisable(GL11.GL_LIGHTING);

        drawRect(x, y, x + width - xPadding * 2, y + height - yPadding * 2, color.getRGB());

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawTooltip(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            RenderHelper.disableStandardItemLighting();
            drawRect(
                xOffset + xPosition + xPadding,
                yOffset + yPosition + yPadding,
                xOffset + xPosition + width - xPadding,
                yOffset + yPosition + height - yPadding,
                0x7FFFFFFF);

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);
            tooltip = Internal.getModIdUtil()
                .addModNameToIngredientTooltip(tooltip, value, ingredientHelper);

            if (tooltipCallback != null) {
                tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
            }

            FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
            if (value instanceof ItemStack) {
                // noinspection unchecked
                Collection<ItemStack> itemStacks = (Collection<ItemStack>) this.allIngredients;
                String oreDictEquivalent = Internal.getStackHelper()
                    .getOreDictEquivalent(itemStacks);
                if (oreDictEquivalent != null) {
                    final String acceptsAny = String.format(oreDictionaryIngredient, oreDictEquivalent);
                    tooltip.add(EnumChatFormatting.GRAY + acceptsAny);
                }
                TooltipRenderer.drawHoveringText(minecraft, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);
            } else {
                TooltipRenderer.drawHoveringText(minecraft, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } catch (RuntimeException e) {
            Log.error("Exception when rendering tooltip on {}.", value, e);
        }
    }

    @Override
    public boolean isInput() {
        return input;
    }
}
