package ruiseki.jfmuy.plugins.vanilla;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.ISubtypeRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.ingredients.IIngredientBlacklist;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.brewing.PotionSubtypeInterpreter;
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
import ruiseki.jfmuy.plugins.vanilla.ingredients.FluidStackHelper;
import ruiseki.jfmuy.plugins.vanilla.ingredients.FluidStackListFactory;
import ruiseki.jfmuy.plugins.vanilla.ingredients.FluidStackRenderer;
import ruiseki.jfmuy.plugins.vanilla.ingredients.ItemStackHelper;
import ruiseki.jfmuy.plugins.vanilla.ingredients.ItemStackListFactory;
import ruiseki.jfmuy.plugins.vanilla.ingredients.ItemStackRenderer;
import ruiseki.jfmuy.transfer.PlayerRecipeTransferHandler;
import ruiseki.jfmuy.util.StackHelper;

@JFMUYPlugin
public class VanillaPlugin extends BlankModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.useNbtForSubtypes(Items.enchanted_book);

        subtypeRegistry.registerSubtypeInterpreter(Items.potionitem, PotionSubtypeInterpreter.INSTANCE);
        subtypeRegistry.registerSubtypeInterpreter(Items.spawn_egg, new ISubtypeRegistry.ISubtypeInterpreter() {

            @Nullable
            @Override
            public String getSubtypeInfo(ItemStack itemStack) {
                return String.valueOf(itemStack.getItemDamage());
            }
        });
    }

    @Override
    public void registerIngredients(IModIngredientRegistration ingredientRegistration) {
        StackHelper stackHelper = Internal.getStackHelper();
        ingredientRegistration.register(
            ItemStack.class,
            ItemStackListFactory.create(stackHelper),
            new ItemStackHelper(stackHelper),
            new ItemStackRenderer());
        ingredientRegistration.register(
            FluidStack.class,
            FluidStackListFactory.create(),
            new FluidStackHelper(),
            new FluidStackRenderer());
    }

    @Override
    public void register(IModRegistry registry) {
        IIngredientRegistry ingredientRegistry = registry.getIngredientRegistry();
        IJFMUYHelpers jfmuyHelpers = registry.getJFMUYHelpers();

        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();
        registry.addRecipeCategories(
            new CraftingRecipeCategory(guiHelper),
            new FurnaceFuelCategory(guiHelper),
            new FurnaceSmeltingCategory(guiHelper),
            new BrewingRecipeCategory(guiHelper));

        registry.addRecipeHandlers(
            new ShapedOreRecipeHandler(jfmuyHelpers),
            new ShapedRecipesHandler(),
            new ShapelessOreRecipeHandler(jfmuyHelpers),
            new ShapelessRecipesHandler(guiHelper),
            new FuelRecipeHandler(),
            new SmeltingRecipeHandler(),
            new BrewingRecipeHandler());

        // Đăng ký vùng Click xem công thức
        registry.addRecipeClickArea(GuiCrafting.class, 88, 32, 28, 23, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeClickArea(GuiInventory.class, 137, 29, 10, 13, VanillaRecipeCategoryUid.CRAFTING);
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

        // Khớp chỉ số slot trong Container chuẩn của 1.7.10
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        recipeTransferRegistry.addRecipeTransferHandler(
            new PlayerRecipeTransferHandler(jfmuyHelpers.recipeTransferHandlerHelper()),
            VanillaRecipeCategoryUid.CRAFTING);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 3, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 5, 36);

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
        registry.addRecipes(FuelRecipeMaker.getFuelRecipes(ingredientRegistry, jfmuyHelpers));
        registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(ingredientRegistry));

        IIngredientBlacklist ingredientBlacklist = registry.getJFMUYHelpers()
            .getIngredientBlacklist();
        ingredientBlacklist.addIngredientToBlacklist(new ItemStack(Items.skull, 1, 3));
    }
}
