package ruiseki.jfmuy.gui;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiFluidStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.gui.ingredients.GuiFluidStackGroup;
import ruiseki.jfmuy.gui.ingredients.GuiIngredient;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackGroup;

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

    public <T extends IRecipeWrapper> RecipeLayout(int index, int posX, int posY,
        @Nonnull IRecipeCategory<T> recipeCategory, @Nonnull T recipeWrapper, @Nonnull Focus focus) {
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
        recipeCategory.setRecipe(this, recipeWrapper);
    }

    public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
        IDrawable background = recipeCategory.getBackground();

        // FIX 1.7.10: Sử dụng GL11 thay thế hoàn toàn cho GlStateManager
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glPushMatrix();
        GL11.glTranslatef(posX, posY, 0.0F);
        {
            background.draw(minecraft);
            recipeCategory.drawExtras(minecraft);
            recipeCategory.drawAnimations(minecraft);
            recipeWrapper.drawAnimations(minecraft, background.getWidth(), background.getHeight());
        }
        GL11.glPopMatrix();

        recipeTransferButton.drawButton(minecraft, mouseX, mouseY);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);

        final int recipeMouseX = mouseX - posX;
        final int recipeMouseY = mouseY - posY;

        GL11.glPushMatrix();
        GL11.glTranslatef(posX, posY, 0.0F);
        {
            recipeWrapper
                .drawInfo(minecraft, background.getWidth(), background.getHeight(), recipeMouseX, recipeMouseY);
        }
        GL11.glPopMatrix();

        RenderHelper.enableGUIStandardItemLighting();
        GuiIngredient hoveredItemStack = guiItemStackGroup.draw(minecraft, posX, posY, mouseX, mouseY);
        RenderHelper.disableStandardItemLighting();
        GuiIngredient hoveredFluidStack = guiFluidStackGroup.draw(minecraft, posX, posY, mouseX, mouseY);

        if (hoveredItemStack != null) {
            RenderHelper.enableGUIStandardItemLighting();
            hoveredItemStack.drawHovered(minecraft, posX, posY, recipeMouseX, recipeMouseY);
            RenderHelper.disableStandardItemLighting();
        } else if (hoveredFluidStack != null) {
            hoveredFluidStack.drawHovered(minecraft, posX, posY, recipeMouseX, recipeMouseY);
        } else if (isMouseOver(mouseX, mouseY)) {
            List<String> tooltipStrings = recipeWrapper.getTooltipStrings(recipeMouseX, recipeMouseY);
            if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
                TooltipRenderer.drawHoveringText(minecraft, tooltipStrings, mouseX, mouseY);
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        final int recipeMouseX = mouseX - posX;
        final int recipeMouseY = mouseY - posY;
        final IDrawable background = recipeCategory.getBackground();
        return recipeMouseX >= 0 && recipeMouseX < background.getWidth()
            && recipeMouseY >= 0
            && recipeMouseY < background.getHeight();
    }

    public Focus getFocusUnderMouse(int mouseX, int mouseY) {
        Focus focus = guiItemStackGroup.getFocusUnderMouse(posX, posY, mouseX, mouseY);
        if (focus == null) {
            focus = guiFluidStackGroup.getFocusUnderMouse(posX, posY, mouseX, mouseY);
        }
        return focus;
    }

    public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return recipeWrapper.handleClick(minecraft, mouseX - posX, mouseY - posY, mouseButton);
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
