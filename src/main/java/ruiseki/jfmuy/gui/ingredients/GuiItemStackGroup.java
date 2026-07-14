package ruiseki.jfmuy.gui.ingredients;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.plugins.vanilla.ingredients.ItemStackRenderer;

public class GuiItemStackGroup extends GuiIngredientGroup<ItemStack> implements IGuiItemStackGroup {

    private static final int baseWidth = 16;
    private static final int baseHeight = 16;
    private static final ItemStackRenderer renderer = new ItemStackRenderer();

    public GuiItemStackGroup(IFocus<ItemStack> focus, int cycleOffset) {
        super(ItemStack.class, focus, cycleOffset);
    }

    public static int getWidth(int padding) {
        return baseWidth + (2 * padding);
    }

    public static int getHeight(int padding) {
        return baseHeight + (2 * padding);
    }

    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
        init(slotIndex, input, xPosition, yPosition, 1);
    }

    public void init(int index, boolean input, int xPosition, int yPosition, int padding) {
        init(index, input, renderer, xPosition, yPosition, getWidth(padding), getHeight(padding), padding, padding);
    }
}
