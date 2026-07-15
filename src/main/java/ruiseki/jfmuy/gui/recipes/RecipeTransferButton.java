package ruiseki.jfmuy.gui.recipes;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.elements.GuiIconButtonSmall;
import ruiseki.jfmuy.transfer.RecipeTransferErrorInternal;
import ruiseki.jfmuy.transfer.RecipeTransferUtil;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class RecipeTransferButton extends GuiIconButtonSmall {

    private final RecipeLayout recipeLayout;
    @Nullable
    private IRecipeTransferError recipeTransferError;

    public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, IDrawable icon,
        RecipeLayout recipeLayout) {
        super(id, xPos, yPos, width, height, icon);
        this.recipeLayout = recipeLayout;
    }

    public void update(@Nullable Container container, EntityPlayer player) {
        if (container != null) {
            this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(container, recipeLayout, player);
        } else {
            this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
        }

        updateStateForTransferError(this, recipeTransferError);
    }

    public static void updateStateForTransferError(GuiButton button,
        @Nullable IRecipeTransferError recipeTransferError) {
        if (RecipeTransferUtil.allowsTransfer(recipeTransferError)) {
            button.enabled = true;
            button.visible = true;
        } else {
            button.enabled = false;
            IRecipeTransferError.Type type = recipeTransferError.getType();
            button.visible = (type == IRecipeTransferError.Type.USER_FACING);
        }
    }

    public void drawToolTip(Minecraft mc, int mouseX, int mouseY) {
        if (this.func_146115_a() && visible) {
            if (recipeTransferError == null) {
                String tooltipTransfer = Translator.translateToLocal("jei.tooltip.transfer");
                TooltipRenderer.drawHoveringText(mc, tooltipTransfer, mouseX, mouseY);
            } else {
                GlStateManager.pushMatrix();
                {
                    recipeTransferError
                        .showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
                }
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (this.visible && this.recipeTransferError != null
            && this.recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
            drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + this.height,
                this.recipeTransferError.getButtonHighlightColor());
        }
    }
}
