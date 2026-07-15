package ruiseki.jfmuy.plugins.vanilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.ISubtypeRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.ingredients.IIngredientBlacklist;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeCategoryRegistration;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.plugins.vanilla.anvil.AnvilRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.anvil.AnvilRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.brewing.BrewingRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.brewing.PotionSubtypeInterpreter;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeCategory;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeChecker;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapedOreRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapedRecipesWrapper;
import ruiseki.jfmuy.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.furnace.FuelRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.furnace.FurnaceFuelCategory;
import ruiseki.jfmuy.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import ruiseki.jfmuy.plugins.vanilla.furnace.SmeltingRecipeMaker;
import ruiseki.jfmuy.plugins.vanilla.ingredients.fluid.FluidStackHelper;
import ruiseki.jfmuy.plugins.vanilla.ingredients.fluid.FluidStackListFactory;
import ruiseki.jfmuy.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import ruiseki.jfmuy.plugins.vanilla.ingredients.item.ItemStackHelper;
import ruiseki.jfmuy.plugins.vanilla.ingredients.item.ItemStackListFactory;
import ruiseki.jfmuy.plugins.vanilla.ingredients.item.ItemStackRenderer;
import ruiseki.jfmuy.runtime.JFMUYHelpers;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.transfer.PlayerRecipeTransferHandler;

@JFMUYPlugin
public class VanillaPlugin implements IModPlugin {

    @Nullable
    private ISubtypeRegistry subtypeRegistry;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        this.subtypeRegistry = subtypeRegistry;

        subtypeRegistry.registerSubtypeInterpreter(Items.potionitem, PotionSubtypeInterpreter.INSTANCE);

        subtypeRegistry
            .registerSubtypeInterpreter(Items.spawn_egg, itemStack -> String.valueOf(itemStack.getItemDamage()));

        subtypeRegistry.registerSubtypeInterpreter(Items.enchanted_book, itemStack -> {
            List<String> enchantmentNames = new ArrayList<>();
            if (itemStack.stackTagCompound != null && itemStack.stackTagCompound.hasKey("StoredEnchantments", 9)) {
                NBTTagList enchantments = itemStack.stackTagCompound.getTagList("StoredEnchantments", 10);
                for (int i = 0; i < enchantments.tagCount(); ++i) {
                    NBTTagCompound nbttagcompound = enchantments.getCompoundTagAt(i);
                    int id = nbttagcompound.getShort("id");
                    int lvl = nbttagcompound.getShort("lvl");
                    Enchantment enchantment = Enchantment.enchantmentsList[id];
                    if (enchantment != null) {
                        enchantmentNames.add(enchantment.getName() + ".lvl" + lvl);
                    }
                }
            }
            Collections.sort(enchantmentNames);
            return enchantmentNames.toString();
        });
    }

    @Override
    public void registerIngredients(IModIngredientRegistration ingredientRegistration) {
        Preconditions.checkState(this.subtypeRegistry != null);
        StackHelper stackHelper = Internal.getStackHelper();
        ItemStackListFactory itemStackListFactory = new ItemStackListFactory(this.subtypeRegistry);

        List<ItemStack> itemStacks = itemStackListFactory.create(stackHelper);
        ItemStackHelper itemStackHelper = new ItemStackHelper(stackHelper);
        ItemStackRenderer itemStackRenderer = new ItemStackRenderer();
        ingredientRegistration.register(VanillaTypes.ITEM, itemStacks, itemStackHelper, itemStackRenderer);

        List<FluidStack> fluidStacks = FluidStackListFactory.create();
        FluidStackHelper fluidStackHelper = new FluidStackHelper();
        FluidStackRenderer fluidStackRenderer = new FluidStackRenderer();
        ingredientRegistration.register(VanillaTypes.FLUID, fluidStacks, fluidStackHelper, fluidStackRenderer);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        JFMUYHelpers jeiHelpers = Internal.getHelpers();
        GuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registry.addRecipeCategories(
            new CraftingRecipeCategory(guiHelper),
            new FurnaceFuelCategory(guiHelper),
            new FurnaceSmeltingCategory(guiHelper),
            new BrewingRecipeCategory(guiHelper),
            new AnvilRecipeCategory(guiHelper));
    }

    @Override
    public void register(IModRegistry registry) {
        IIngredientRegistry ingredientRegistry = registry.getIngredientRegistry();
        IJFMUYHelpers jeiHelpers = registry.getJFMUYHelpers();
        IVanillaRecipeFactory vanillaRecipeFactory = jeiHelpers.getVanillaRecipeFactory();

        Pair<List<IRecipe>, Set<Class<? extends IRecipe>>> result = CraftingRecipeChecker.getValidRecipes(jeiHelpers);
        List<IRecipe> validRecipes = result.getLeft();

        registry.addRecipes(validRecipes, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(SmeltingRecipeMaker.getFurnaceRecipes(jeiHelpers), VanillaRecipeCategoryUid.SMELTING);
        registry
            .addRecipes(FuelRecipeMaker.getFuelRecipes(ingredientRegistry, jeiHelpers), VanillaRecipeCategoryUid.FUEL);
        registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes(ingredientRegistry), VanillaRecipeCategoryUid.BREWING);
        registry.addRecipes(
            AnvilRecipeMaker.getAnvilRecipes(vanillaRecipeFactory, ingredientRegistry),
            VanillaRecipeCategoryUid.ANVIL);

        registry.handleRecipes(
            ShapedOreRecipe.class,
            recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe),
            VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(
            ShapedRecipes.class,
            recipe -> new ShapedRecipesWrapper(jeiHelpers, recipe),
            VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(
            ShapelessOreRecipe.class,
            recipe -> new ShapelessRecipeWrapper<>(jeiHelpers, recipe),
            VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(
            ShapelessRecipes.class,
            recipe -> new ShapelessRecipeWrapper<>(jeiHelpers, recipe),
            VanillaRecipeCategoryUid.CRAFTING);

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
        registry.addRecipeClickArea(GuiRepair.class, 102, 48, 22, 15, VanillaRecipeCategoryUid.ANVIL);

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        recipeTransferRegistry.addRecipeTransferHandler(
            new PlayerRecipeTransferHandler(jeiHelpers.recipeTransferHandlerHelper()),
            VanillaRecipeCategoryUid.CRAFTING);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 3, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 3, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 5, 36);
        recipeTransferRegistry
            .addRecipeTransferHandler(ContainerRepair.class, VanillaRecipeCategoryUid.ANVIL, 0, 2, 3, 36);

        registry.addRecipeCatalyst(new ItemStack(Blocks.crafting_table), VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeCatalyst(
            new ItemStack(Blocks.furnace),
            VanillaRecipeCategoryUid.SMELTING,
            VanillaRecipeCategoryUid.FUEL);
        registry.addRecipeCatalyst(new ItemStack(Items.brewing_stand), VanillaRecipeCategoryUid.BREWING);
        registry.addRecipeCatalyst(new ItemStack(Blocks.anvil), VanillaRecipeCategoryUid.ANVIL);

        IIngredientBlacklist ingredientBlacklist = registry.getJFMUYHelpers()
            .getIngredientBlacklist();
        ingredientBlacklist.addIngredientToBlacklist(new ItemStack(Items.skull, 1, 3));
        ingredientBlacklist
            .addIngredientToBlacklist(new ItemStack(Items.enchanted_book, 1, OreDictionary.WILDCARD_VALUE));

        registry.addAdvancedGuiHandlers(new InventoryEffectRendererGuiHandler());
    }
}
