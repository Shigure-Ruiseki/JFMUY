package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.ICraftingGridHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IGuiItemStackGroup;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.wrapper.ICraftingRecipeWrapper;
import ruiseki.jfmuy.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.jfmuy.startup.ForgeModIdHelper;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.helper.Helpers;

public class CraftingRecipeCategory implements IRecipeCategory<IRecipeWrapper> {

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    public static final int width = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    private final ICraftingGridHelper craftingGridHelper;

    public CraftingRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = Reference.RECIPE_GUI_VANILLA;
        background = guiHelper.createDrawable(location, 0, 60, width, height);
        icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.crafting_table));
        localizedName = Translator.translateToLocal("gui.jei.category.craftingTable");
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
    public String getModName() {
        return Reference.MINECRAFT_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
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

        if (recipeWrapper instanceof ICustomCraftingRecipeWrapper) {
            ICustomCraftingRecipeWrapper customWrapper = (ICustomCraftingRecipeWrapper) recipeWrapper;
            customWrapper.setRecipe(recipeLayout, ingredients);
            return;
        }

        List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

        if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
            IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper) recipeWrapper;
            craftingGridHelper.setInputs(guiItemStacks, inputs, wrapper.getWidth(), wrapper.getHeight());
        } else {
            craftingGridHelper.setInputs(guiItemStacks, inputs);
            recipeLayout.setShapeless();
        }
        guiItemStacks.set(craftOutputSlot, outputs.get(0));

        if (recipeWrapper instanceof ICraftingRecipeWrapper) {
            ICraftingRecipeWrapper craftingRecipeWrapper = (ICraftingRecipeWrapper) recipeWrapper;
            ResourceLocation registryName = craftingRecipeWrapper.getRegistryName();
            if (registryName != null) {
                guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
                    if (slotIndex == craftOutputSlot) {
                        String recipeModId = registryName.getResourceDomain();

                        boolean modIdDifferent = false;
                        ResourceLocation itemRegistryName = Helpers.getLocation(ingredient.getItem());
                        if (itemRegistryName != null) {
                            String itemModId = itemRegistryName.getResourceDomain();
                            modIdDifferent = !recipeModId.equals(itemModId);
                        }

                        if (modIdDifferent) {
                            String modName = ForgeModIdHelper.getInstance()
                                .getFormattedModNameForModId(recipeModId);
                            if (modName != null) {
                                tooltip.add(
                                    EnumChatFormatting.GRAY
                                        + Translator.translateToLocalFormatted("jei.tooltip.recipe.by", modName));
                            }
                        }

                        boolean showAdvanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips
                            || GuiScreen.isShiftKeyDown();
                        if (showAdvanced) {
                            tooltip.add(
                                EnumChatFormatting.DARK_GRAY + Translator
                                    .translateToLocalFormatted("jei.tooltip.recipe.id", registryName.toString()));
                        }
                    }
                });
            }
        }
    }

}
