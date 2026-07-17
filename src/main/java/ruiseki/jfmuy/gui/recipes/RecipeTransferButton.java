package ruiseki.jfmuy.gui.recipes;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.elements.GuiIconButtonSmall;
import ruiseki.jfmuy.transfer.RecipeTransferErrorInternal;
import ruiseki.jfmuy.transfer.RecipeTransferUtil;
import ruiseki.jfmuy.util.Translator;

public class RecipeTransferButton extends GuiIconButtonSmall {

    private final RecipeLayout recipeLayout;
    @Nullable
    private IRecipeTransferError recipeTransferError;

    public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, IDrawable icon,
        RecipeLayout recipeLayout) {
        super(id, xPos, yPos, width, height, icon);
        this.recipeLayout = recipeLayout;
    }

    public void init(@Nullable Container container, EntityPlayer player) {
        if (container != null) {
            this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(container, recipeLayout, player);
        } else {
            this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
        }

        if (this.recipeTransferError == null) {
            this.enabled = true;
            this.visible = true;
        } else {
            this.enabled = false;
            IRecipeTransferError.Type type = this.recipeTransferError.getType();
            this.visible = (type == IRecipeTransferError.Type.USER_FACING);
        }
    }

    public void drawToolTip(Minecraft mc, int mouseX, int mouseY) {
        if (field_146123_n && visible) {
            if (recipeTransferError != null) {
                recipeTransferError
                    .showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
            } else {
                String tooltipTransfer = Translator.translateToLocal("jfmuy.tooltip.transfer");
                TooltipRenderer.drawHoveringText(mc, tooltipTransfer, mouseX, mouseY);
            }
        }
    }
}
