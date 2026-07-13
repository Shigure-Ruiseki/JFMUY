package ruiseki.jfmuy.gui;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiFluidStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.GuiFluidStackGroup;
import ruiseki.jfmuy.gui.ingredients.GuiIngredient;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackGroup;
import ruiseki.jfmuy.gui.ingredients.ItemStackRenderer;

public class RecipeLayout implements IRecipeLayout {

    private static final int RECIPE_BUTTON_SIZE = 12;
    public static final int recipeTransferButtonIndex = 100;

    @Nonnull
    private final IRecipeCategory recipeCategory;
    @Nonnull
    private final GuiItemStackGroup guiItemStackGroup;
    @Nonnull
    private final GuiFluidStackGroup guiFluidStackGroup;
    @Nonnull
    private final RecipeTransferButton recipeTransferButton;
    @Nonnull
    private final IRecipeWrapper recipeWrapper;

    private final int posX;
    private final int posY;

    public RecipeLayout(int index, int posX, int posY, @Nonnull IRecipeCategory recipeCategory,
        @Nonnull IRecipeWrapper recipeWrapper, @Nonnull Focus focus) {
        this.recipeCategory = recipeCategory;
        this.guiItemStackGroup = new GuiItemStackGroup();
        this.guiFluidStackGroup = new GuiFluidStackGroup();
        int width = recipeCategory.getBackground()
            .getWidth();
        int height = recipeCategory.getBackground()
            .getHeight();
        this.recipeTransferButton = new RecipeTransferButton(
            recipeTransferButtonIndex + index,
            posX + width + 2,
            posY + height - RECIPE_BUTTON_SIZE,
            RECIPE_BUTTON_SIZE,
            RECIPE_BUTTON_SIZE,
            "+");
        this.posX = posX;
        this.posY = posY;

        this.recipeWrapper = recipeWrapper;
        this.guiItemStackGroup.setFocus(focus);
        this.guiFluidStackGroup.setFocus(focus);
        this.recipeCategory.setRecipe(this, recipeWrapper);
    }

    public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
        GL11.glPushMatrix();
        GL11.glTranslatef(posX, posY, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);

        IDrawable background = recipeCategory.getBackground();
        background.draw(minecraft);
        recipeCategory.drawExtras(minecraft);

        if (Config.isRecipeAnimationsEnabled()) {
            recipeCategory.drawAnimations(minecraft);
            recipeWrapper.drawAnimations(minecraft, background.getWidth(), background.getHeight());
        }

        GL11.glTranslatef(-posX, -posY, 0.0F);
        if (recipeTransferButton.visible) {
            recipeTransferButton.drawButton(minecraft, mouseX, mouseY);
        }
        GL11.glTranslatef(posX, posY, 0.0F);

        recipeWrapper.drawInfo(minecraft, background.getWidth(), background.getHeight());

        final int recipeMouseX = mouseX - posX;
        final int recipeMouseY = mouseY - posY;

        ItemStackRenderer.enableGuiItemRender();
        GuiIngredient hoveredItemStack = guiItemStackGroup.draw(minecraft, recipeMouseX, recipeMouseY);
        ItemStackRenderer.disableGuiItemRender();
        GuiIngredient hoveredFluidStack = guiFluidStackGroup.draw(minecraft, recipeMouseX, recipeMouseY);

        if (hoveredItemStack != null) {
            ItemStackRenderer.enableGuiItemRender();
            hoveredItemStack.drawHovered(minecraft, recipeMouseX, recipeMouseY);
            ItemStackRenderer.disableGuiItemRender();
        } else if (hoveredFluidStack != null) {
            hoveredFluidStack.drawHovered(minecraft, recipeMouseX, recipeMouseY);
        } else if (recipeMouseX >= 0 && recipeMouseX < background.getWidth()
            && recipeMouseY >= 0
            && recipeMouseY < background.getHeight()) {
                List<String> tooltipStrings = null;
                try {
                    tooltipStrings = recipeWrapper.getTooltipStrings(recipeMouseX, recipeMouseY);
                } catch (AbstractMethodError ignored) {
                    // older wrappers don't have this method
                }
                if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
                    TooltipRenderer.drawHoveringText(minecraft, tooltipStrings, recipeMouseX, recipeMouseY);
                }
            }

        GL11.glPopMatrix();
    }

    public Focus getFocusUnderMouse(int mouseX, int mouseY) {
        Focus focus = guiItemStackGroup.getFocusUnderMouse(mouseX - posX, mouseY - posY);
        if (focus == null) {
            focus = guiFluidStackGroup.getFocusUnderMouse(mouseX - posX, mouseY - posY);
        }
        return focus;
    }

    @Override
    @Nonnull
    public GuiItemStackGroup getItemStacks() {
        return guiItemStackGroup;
    }

    @Override
    @Nonnull
    public IGuiFluidStackGroup getFluidStacks() {
        return guiFluidStackGroup;
    }

    @Override
    public void setRecipeTransferButton(int posX, int posY) {
        recipeTransferButton.xPosition = posX + this.posX;
        recipeTransferButton.yPosition = posY + this.posY;
    }

    @Nonnull
    public RecipeTransferButton getRecipeTransferButton() {
        return recipeTransferButton;
    }

    @Nonnull
    public IRecipeWrapper getRecipeWrapper() {
        return recipeWrapper;
    }

    @Nonnull
    public IRecipeCategory getRecipeCategory() {
        return recipeCategory;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}
