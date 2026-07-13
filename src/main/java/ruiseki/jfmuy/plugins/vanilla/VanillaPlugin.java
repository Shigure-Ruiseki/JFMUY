package ruiseki.jfmuy.plugins.vanilla;

import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IItemRegistry;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeHandler;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeMaker;
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
import ruiseki.jfmuy.transfer.PlayerRecipeTransferHandler;

@JFMUYPlugin
public class VanillaPlugin implements IModPlugin {

    private IItemRegistry itemRegistry;
    private IJFMUYHelpers jfmuyHelpers;

    @Override
    public void onJFMUYHelpersAvailable(IJFMUYHelpers jfmuyHelpers) {
        this.jfmuyHelpers = jfmuyHelpers;
    }

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void register(IModRegistry registry) {
        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();
        registry.addRecipeCategories(
            new CraftingRecipeCategory(guiHelper),
            new FurnaceFuelCategory(guiHelper),
            new FurnaceSmeltingCategory(guiHelper),
            new BrewingRecipeCategory(guiHelper));

        registry.addRecipeHandlers(
            new ShapedOreRecipeHandler(),
            new ShapedRecipesHandler(),
            new ShapelessOreRecipeHandler(),
            new ShapelessRecipesHandler(),
            new FuelRecipeHandler(),
            new SmeltingRecipeHandler(),
            new BrewingRecipeHandler());

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(new PlayerRecipeTransferHandler(jfmuyHelpers.recipeTransferHandlerHelper()));
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 1, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 1, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 4, 36);

        registry.addRecipes(CraftingRecipeMaker.getCraftingRecipes());
        registry.addRecipes(SmeltingRecipeMaker.getFurnaceRecipes());
        registry.addRecipes(FuelRecipeMaker.getFuelRecipes(itemRegistry, guiHelper));
        registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(itemRegistry));
    }

    @Override
    public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {

    }
}
