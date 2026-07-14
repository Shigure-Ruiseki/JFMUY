package ruiseki.jfmuy.gui.recipes;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.ingredients.ItemStackRenderer;

public class RecipeCategoryTab extends RecipeGuiTab {

    private final IRecipeGuiLogic logic;
    private final IRecipeCategory category;

    public RecipeCategoryTab(IRecipeGuiLogic logic, IRecipeCategory category, int x, int y) {
        super(x, y);
        this.logic = logic;
        this.category = category;
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        logic.setRecipeCategory(category);
        SoundHandler soundHandler = Minecraft.getMinecraft()
            .getSoundHandler();
        soundHandler.playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
        return true;
    }

    @Override
    public void draw(Minecraft minecraft, boolean selected, int mouseX, int mouseY) {
        super.draw(minecraft, selected, mouseX, mouseY);

        int iconX = x + 4;
        int iconY = y + 4;

        IDrawable icon = getCategoryIcon(category);
        if (icon != null) {
            iconX += (16 - icon.getWidth()) / 2;
            iconY += (16 - icon.getHeight()) / 2;
            icon.draw(minecraft, iconX, iconY);
        } else {
            List<ItemStack> craftingItems = logic.getRecipeCategoryCraftingItems(category);
            if (!craftingItems.isEmpty()) {
                ItemStackRenderer renderer = new ItemStackRenderer();
                ItemStack ingredient = craftingItems.getFirst();
                GL11.glEnable(GL11.GL_DEPTH_TEST);

                renderer.render(minecraft, iconX, iconY, ingredient);

                GL11.glEnable(GL11.GL_ALPHA_TEST);

                GL11.glDisable(GL11.GL_DEPTH_TEST);
            } else {
                String text = category.getTitle()
                    .substring(0, 2);
                FontRenderer fontRenderer = minecraft.fontRenderer;
                float textCenterX = x + (TAB_WIDTH / 2f);
                float textCenterY = y + (TAB_HEIGHT / 2f) - 3;
                int color = isMouseOver(mouseX, mouseY) ? 16777120 : 14737632;
                fontRenderer.drawStringWithShadow(
                    text,
                    (int) (textCenterX - fontRenderer.getStringWidth(text) / 2f),
                    (int) textCenterY,
                    color);
                GL11.glColor4f(1, 1, 1, 1);
            }
        }
    }

    @Nullable
    private static IDrawable getCategoryIcon(IRecipeCategory recipeCategory) {
        try {
            return recipeCategory.getIcon();
        } catch (AbstractMethodError ignored) { // old recipe categories do not implement this method
            return null;
        }
    }

    @Override
    public boolean isSelected(IRecipeCategory selectedCategory) {
        return category.getUid()
            .equals(selectedCategory.getUid());
    }

    @Nullable
    @Override
    public String getTooltip() {
        return category.getTitle();
    }
}
