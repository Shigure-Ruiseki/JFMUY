package ruiseki.jfmuy.plugins.vanilla;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import ruiseki.jfmuy.api.BlankModPlugin;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IItemRegistry;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.ISubtypeRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapedOreRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapedRecipesHandler;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapelessOreRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapelessRecipesHandler;
import ruiseki.jfmuy.plugins.vanilla.furnace.FuelRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.furnace.FuelRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.furnace.FurnaceFuelCategory;
import ruiseki.jfmuy.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import ruiseki.jfmuy.plugins.vanilla.furnace.SmeltingRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.furnace.SmeltingRecipeMaker;

@JFMUYPlugin
public class VanillaPlugin extends BlankModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IItemRegistry itemRegistry = registry.getItemRegistry();
        IJFMUYHelpers jfmuyHelpers = registry.getJFMUYHelpers();

        ISubtypeRegistry nbtRegistry = jfmuyHelpers.getSubtypeRegistry();

        nbtRegistry.useNbtForSubtypes(Items.spawn_egg, Items.enchanted_book, Items.potionitem);

        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();
        registry.addRecipeCategories(
            new CraftingRecipeCategory(guiHelper),
            new FurnaceFuelCategory(guiHelper),
            new FurnaceSmeltingCategory(guiHelper),
            new BrewingRecipeCategory(guiHelper));

        registry.addRecipeHandlers(
            new ShapedOreRecipeHandler(),
            new ShapedRecipesHandler(),
            new ShapelessOreRecipeHandler(guiHelper),
            new ShapelessRecipesHandler(guiHelper),
            new FuelRecipeHandler(),
            new SmeltingRecipeHandler(),
            new BrewingRecipeHandler());

        registry.addRecipeClickArea(GuiCrafting.class, 88, 32, 28, 23, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeClickArea(GuiBrewingStand.class, 97, 16, 14, 30, VanillaRecipeCategoryUid.BREWING);
        registry.addRecipeClickArea(
            GuiFurnace.class,
            78,
            32,
            28,
            23,
            VanillaRecipeCategoryUid.SMELTING,
            VanillaRecipeCategoryUid.FUEL);

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 3, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 4, 36);

        registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.crafting_table), VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeCategoryCraftingItem(
            new ItemStack(Blocks.furnace),
            VanillaRecipeCategoryUid.SMELTING,
            VanillaRecipeCategoryUid.FUEL);
        registry.addRecipeCategoryCraftingItem(new ItemStack(Items.brewing_stand), VanillaRecipeCategoryUid.BREWING);

        registry.addRecipes(
            CraftingManager.getInstance()
                .getRecipeList());
        registry.addRecipes(SmeltingRecipeMaker.getFurnaceRecipes(jfmuyHelpers));
        registry.addRecipes(FuelRecipeMaker.getFuelRecipes(itemRegistry, jfmuyHelpers));
        registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(itemRegistry));
    }
}
