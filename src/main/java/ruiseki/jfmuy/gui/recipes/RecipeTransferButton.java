package ruiseki.jfmuy.gui.recipes;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import cpw.mods.fml.client.config.GuiButtonExt;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.transfer.RecipeTransferErrorInternal;
import ruiseki.jfmuy.transfer.RecipeTransferUtil;

public class RecipeTransferButton extends GuiButtonExt {

    private final RecipeLayout recipeLayout;
    @Nullable
    private IRecipeTransferError recipeTransferError;

    public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, String displayString,
        RecipeLayout recipeLayout) {
        super(id, xPos, yPos, width, height, displayString);
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

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (this.field_146123_n && visible) {
            if (recipeTransferError != null) {
                recipeTransferError
                    .showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
            } else {
                TooltipRenderer.drawHoveringText(mc, Reference.RECIPE_TRANSFER_TOOLTIP, mouseX, mouseY);
            }
        }
    }
}
