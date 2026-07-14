package ruiseki.jfmuy.gui.ingredients;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.api.gui.IGuiIngredient;
import ruiseki.jfmuy.api.gui.ITooltipCallback;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.util.CycleTimer;
import ruiseki.jfmuy.util.Log;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {

    private final int slotIndex;
    private final boolean input;

    private final int xPosition;
    private final int yPosition;
    private final int width;
    private final int height;
    private final int padding;

    @Nonnull
    private final CycleTimer cycleTimer;
    @Nonnull
    private final List<T> displayIngredients = new ArrayList<>(); // ingredients, taking focus into account
    @Nonnull
    private final List<T> allIngredients = new ArrayList<>(); // all ingredients, ignoring focus
    @Nonnull
    private final IIngredientRenderer<T> ingredientRenderer;
    @Nonnull
    private final IIngredientHelper<T> ingredientHelper;
    @Nullable
    private ITooltipCallback<T> tooltipCallback;

    private boolean enabled;

    public GuiIngredient(@Nonnull IIngredientRenderer<T> ingredientRenderer,
        @Nonnull IIngredientHelper<T> ingredientHelper, int slotIndex, boolean input, int xPosition, int yPosition,
        int width, int height, int padding, int itemCycleOffset) {
        this.ingredientRenderer = ingredientRenderer;
        this.ingredientHelper = ingredientHelper;

        this.slotIndex = slotIndex;
        this.input = input;

        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.padding = padding;

        this.cycleTimer = new CycleTimer(itemCycleOffset);
    }

    @Override
    public void clear() {
        enabled = false;
        displayIngredients.clear();
    }

    @Override
    public boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY) {
        return enabled && (mouseX >= xOffset + xPosition)
            && (mouseY >= yOffset + yPosition)
            && (mouseX < xOffset + xPosition + width)
            && (mouseY < yOffset + yPosition + height);
    }

    @Nullable
    public T getIngredient() {
        return cycleTimer.getCycledItem(displayIngredients);
    }

    @Override
    public Focus getFocus() {
        T ingredient = getIngredient();
        if (ingredient == null) {
            return null;
        }
        return ingredientHelper.createFocus(ingredient);
    }

    @Nonnull
    @Override
    public List<T> getAllIngredients() {
        return allIngredients;
    }

    @Override
    public void set(@Nonnull T ingredient, @Nonnull Focus focus) {
        set(Collections.singleton(ingredient), focus);
    }

    @Override
    public void set(@Nonnull Collection<T> ingredients, @Nonnull Focus focus) {
        this.displayIngredients.clear();
        this.allIngredients.clear();
        ingredients = ingredientHelper.expandSubtypes(ingredients);
        T match = null;
        if ((isInput() && focus.getMode() == Focus.Mode.INPUT)
            || (!isInput() && focus.getMode() == Focus.Mode.OUTPUT)) {
            match = ingredientHelper.getMatch(ingredients, focus);
        }
        if (match != null) {
            this.displayIngredients.add(match);
        } else {
            this.displayIngredients.addAll(ingredients);
        }
        this.ingredientRenderer.setIngredients(ingredients);
        this.allIngredients.addAll(ingredients);
        enabled = !this.displayIngredients.isEmpty();
    }

    public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
        this.tooltipCallback = tooltipCallback;
    }

    @Override
    public void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset) {
        cycleTimer.onDraw();

        T value = getIngredient();
        ingredientRenderer.draw(minecraft, xOffset + xPosition + padding, yOffset + yPosition + padding, value);
    }

    @Override
    public void drawHovered(@Nonnull Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
        T value = getIngredient();
        if (value == null) {
            return;
        }
        draw(minecraft, xOffset, yOffset);
        drawTooltip(minecraft, xOffset, yOffset, mouseX, mouseY, value);
    }

    @Override
    public void drawHighlight(@Nonnull Minecraft minecraft, Color color, int xOffset, int yOffset) {
        int x = xPosition + xOffset + padding;
        int y = yPosition + yOffset + padding;

        GL11.glDisable(GL11.GL_LIGHTING);

        drawRect(x, y, x + width - padding * 2, y + height - padding * 2, color.getRGB());

        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawTooltip(@Nonnull Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY,
        @Nonnull T value) {
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            RenderHelper.disableStandardItemLighting();

            drawRect(
                xOffset + xPosition + padding,
                yOffset + yPosition + padding,
                xOffset + xPosition + width - padding,
                yOffset + yPosition + height - padding,
                0x7FFFFFFF);

            // FIX 1.7.10: Thay thế GlStateManager.color(...)
            GL11.glColor4f(1f, 1f, 1f, 1f);

            List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);

            if (tooltipCallback != null) {
                tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
            }

            FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
            TooltipRenderer.drawHoveringText(minecraft, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);

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
