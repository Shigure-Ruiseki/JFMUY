package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.config.HoverChecker;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.BlankRecipeWrapper;
import ruiseki.jfmuy.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import ruiseki.jfmuy.util.Ingredients;
import ruiseki.jfmuy.util.Translator;

public abstract class AbstractShapelessRecipeWrapper extends BlankRecipeWrapper
    implements ICustomCraftingRecipeWrapper {

    private static final double shapelessIconScale = 0.5;
    private final IDrawable shapelessIcon;
    private final HoverChecker shapelessIconHoverChecker;

    public AbstractShapelessRecipeWrapper(IGuiHelper guiHelper) {
        ResourceLocation shapelessIconLocation = new ResourceLocation(
            Reference.MOD_ID,
            Reference.TEXTURE_GUI_PATH + "recipeBackground.png");
        shapelessIcon = guiHelper.createDrawable(shapelessIconLocation, 196, 0, 19, 15);

        int iconBottom = (int) (shapelessIcon.getHeight() * shapelessIconScale);
        int iconLeft = CraftingRecipeCategory.width - (int) (shapelessIcon.getWidth() * shapelessIconScale);
        int iconRight = iconLeft + (int) (shapelessIcon.getWidth() * shapelessIconScale);
        shapelessIconHoverChecker = new HoverChecker(0, iconBottom, iconLeft, iconRight, 0);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);

        if (hasMultipleIngredients()) {
            int shapelessIconX = recipeWidth - (int) (shapelessIcon.getWidth() * shapelessIconScale);

            GL11.glPushMatrix();
            GL11.glScaled(shapelessIconScale, shapelessIconScale, 1.0);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            shapelessIcon.draw(minecraft, (int) (shapelessIconX / shapelessIconScale), 0);
            GL11.glPopMatrix();
        }
    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (hasMultipleIngredients() && shapelessIconHoverChecker.checkHover(mouseX, mouseY)) {
            return Collections.singletonList(Translator.translateToLocal("jfmuy.tooltip.shapeless.recipe"));
        }

        return super.getTooltipStrings(mouseX, mouseY);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
        recipeLayout.getItemStacks()
            .set(ingredients);
    }

    private boolean hasMultipleIngredients() {
        Ingredients ingredients = new Ingredients();
        this.getIngredients(ingredients);

        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        return inputs != null && inputs.size() > 1;
    }
}
