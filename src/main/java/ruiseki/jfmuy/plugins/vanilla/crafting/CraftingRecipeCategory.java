package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.ICraftingGridHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.BlankRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.jfmuy.util.Translator;

public class CraftingRecipeCategory extends BlankRecipeCategory<IRecipeWrapper> {

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    public static final int width = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final String localizedName;
    private final ICraftingGridHelper craftingGridHelper;

    public CraftingRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
        background = guiHelper.createDrawable(location, 29, 16, width, height);
        localizedName = Translator.translateToLocal("gui.jfmuy.category.craftingTable");
        craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    public String getUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(craftOutputSlot, false, 94, 18);

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                int index = craftInputSlot1 + x + (y * 3);
                guiItemStacks.init(index, true, x * 18, y * 18);
            }
        }

        if (recipeWrapper instanceof ICustomCraftingRecipeWrapper customWrapper) {
            customWrapper.setRecipe(recipeLayout, ingredients);
            return;
        }

        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        List<ItemStack> outputs = ingredients.getOutputs(ItemStack.class);

        if (recipeWrapper instanceof IShapedCraftingRecipeWrapper wrapper) {
            craftingGridHelper.setInputStacks(guiItemStacks, inputs, wrapper.getWidth(), wrapper.getHeight());
        } else {
            craftingGridHelper.setInputStacks(guiItemStacks, inputs);
        }
        guiItemStacks.set(craftOutputSlot, outputs.getFirst());
    }
}
