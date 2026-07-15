package ruiseki.jfmuy.plugins.vanilla.anvil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public final class AnvilRecipeMaker {

    private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.enchanted_book);

    private AnvilRecipeMaker() {}

    public static List<IRecipeWrapper> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory,
        IIngredientRegistry ingredientRegistry) {
        List<IRecipeWrapper> recipes = new ArrayList<>();
        Stopwatch sw = Stopwatch.createStarted();
        try {
            getRepairRecipes(recipes, vanillaRecipeFactory);
        } catch (RuntimeException e) {
            Log.get()
                .error("Failed to create repair recipes.", e);
        }
        sw.stop();
        Log.get()
            .debug("Registered vanilla repair recipes in {}", sw);
        sw.reset();
        sw.start();
        try {
            getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, ingredientRegistry);
        } catch (RuntimeException e) {
            Log.get()
                .error("Failed to create enchantment recipes.", e);
        }
        sw.stop();
        Log.get()
            .debug("Registered enchantment recipes in {}", sw);
        return recipes;
    }

    private static void getBookEnchantmentRecipes(List<IRecipeWrapper> recipes,
        IVanillaRecipeFactory vanillaRecipeFactory, IIngredientRegistry ingredientRegistry) {
        Collection<ItemStack> ingredients = ingredientRegistry.getAllIngredients(VanillaTypes.ITEM);

        List<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.enchantmentsList) {
            if (enchantment != null) {
                enchantments.add(enchantment);
            }
        }

        for (ItemStack ingredient : ingredients) {
            if (ingredient != null && ingredient.getItem() != null && ingredient.isItemEnchantable()) {
                for (Enchantment enchantment : enchantments) {
                    if (enchantment.canApply(ingredient)) {
                        try {
                            getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, enchantment, ingredient);
                        } catch (RuntimeException e) {
                            String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient);
                            Log.get()
                                .error(
                                    "Failed to register book enchantment recipes for ingredient: {}",
                                    ingredientInfo,
                                    e);
                        }
                    }
                }
            }
        }
    }

    private static void getBookEnchantmentRecipes(List<IRecipeWrapper> recipes,
        IVanillaRecipeFactory vanillaRecipeFactory, Enchantment enchantment, ItemStack ingredient) {
        Item item = ingredient.getItem();
        List<ItemStack> perLevelBooks = Lists.newArrayList();
        List<ItemStack> perLevelOutputs = Lists.newArrayList();
        for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
            Map<Integer, Integer> enchMap = Collections.singletonMap(enchantment.effectId, level);

            ItemStack bookEnchant = ENCHANTED_BOOK.copy();
            EnchantmentHelper.setEnchantments(enchMap, bookEnchant);

            if (item.isBookEnchantable(ingredient, bookEnchant)) {
                perLevelBooks.add(bookEnchant);

                ItemStack withEnchant = ingredient.copy();
                EnchantmentHelper.setEnchantments(enchMap, withEnchant);
                perLevelOutputs.add(withEnchant);
            }
        }
        if (!perLevelBooks.isEmpty() && !perLevelOutputs.isEmpty()) {
            IRecipeWrapper anvilRecipe = vanillaRecipeFactory
                .createAnvilRecipe(ingredient, perLevelBooks, perLevelOutputs);
            recipes.add(anvilRecipe);
        }
    }

    private static void getRepairRecipes(List<IRecipeWrapper> recipes, IVanillaRecipeFactory vanillaRecipeFactory) {
        Map<ItemStack, List<ItemStack>> items = Maps.newHashMap();

        // 1.7.10 Fix: Thay đổi tên hằng số vật phẩm viết thường (CamelCase) và loại bỏ Shield/Elytra
        ItemStack repairWood = new ItemStack(Blocks.planks, 1, OreDictionary.WILDCARD_VALUE);
        items.put(
            repairWood,
            Lists.newArrayList(
                new ItemStack(Items.wooden_sword),
                new ItemStack(Items.wooden_pickaxe),
                new ItemStack(Items.wooden_axe),
                new ItemStack(Items.wooden_shovel),
                new ItemStack(Items.wooden_hoe)));

        ItemStack repairStone = new ItemStack(Blocks.cobblestone);
        items.put(
            repairStone,
            Lists.newArrayList(
                new ItemStack(Items.stone_sword),
                new ItemStack(Items.stone_pickaxe),
                new ItemStack(Items.stone_axe),
                new ItemStack(Items.stone_shovel),
                new ItemStack(Items.stone_hoe)));

        ItemStack repairLeather = new ItemStack(Items.leather);
        items.put(
            repairLeather,
            Lists.newArrayList(
                new ItemStack(Items.leather_helmet),
                new ItemStack(Items.leather_chestplate),
                new ItemStack(Items.leather_leggings),
                new ItemStack(Items.leather_boots)));

        ItemStack repairIron = new ItemStack(Items.iron_ingot);
        items.put(
            repairIron,
            Lists.newArrayList(
                new ItemStack(Items.iron_sword),
                new ItemStack(Items.iron_pickaxe),
                new ItemStack(Items.iron_axe),
                new ItemStack(Items.iron_shovel),
                new ItemStack(Items.iron_hoe),
                new ItemStack(Items.iron_helmet),
                new ItemStack(Items.iron_chestplate),
                new ItemStack(Items.iron_leggings),
                new ItemStack(Items.iron_boots),
                new ItemStack(Items.chainmail_helmet),
                new ItemStack(Items.chainmail_chestplate),
                new ItemStack(Items.chainmail_leggings),
                new ItemStack(Items.chainmail_boots)));

        ItemStack repairGold = new ItemStack(Items.gold_ingot);
        items.put(
            repairGold,
            Lists.newArrayList(
                new ItemStack(Items.golden_sword),
                new ItemStack(Items.golden_pickaxe),
                new ItemStack(Items.golden_axe),
                new ItemStack(Items.golden_shovel),
                new ItemStack(Items.golden_hoe),
                new ItemStack(Items.golden_helmet),
                new ItemStack(Items.golden_chestplate),
                new ItemStack(Items.golden_leggings),
                new ItemStack(Items.golden_boots)));

        ItemStack repairDiamond = new ItemStack(Items.diamond);
        items.put(
            repairDiamond,
            Lists.newArrayList(
                new ItemStack(Items.diamond_sword),
                new ItemStack(Items.diamond_pickaxe),
                new ItemStack(Items.diamond_axe),
                new ItemStack(Items.diamond_shovel),
                new ItemStack(Items.diamond_hoe),
                new ItemStack(Items.diamond_helmet),
                new ItemStack(Items.diamond_chestplate),
                new ItemStack(Items.diamond_leggings),
                new ItemStack(Items.diamond_boots)));

        for (Map.Entry<ItemStack, List<ItemStack>> entry : items.entrySet()) {
            ItemStack repairMaterial = entry.getKey();
            for (ItemStack ingredient : entry.getValue()) {
                ItemStack damaged1 = ingredient.copy();
                damaged1.setItemDamage(damaged1.getMaxDamage());
                ItemStack damaged2 = ingredient.copy();
                damaged2.setItemDamage(damaged2.getMaxDamage() * 3 / 4);
                ItemStack damaged3 = ingredient.copy();
                damaged3.setItemDamage(damaged3.getMaxDamage() * 2 / 4);

                IRecipeWrapper repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
                    damaged1,
                    Collections.singletonList(repairMaterial),
                    Collections.singletonList(damaged2));
                IRecipeWrapper repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
                    damaged2,
                    Collections.singletonList(damaged2),
                    Collections.singletonList(damaged3));
                recipes.add(repairWithMaterial);
                recipes.add(repairWithSame);
            }
        }
    }

    public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return -1;
        }
        InventoryPlayer fakeInventory = new InventoryPlayer(player);
        try {
            ContainerRepair repair = new ContainerRepair(fakeInventory, player.worldObj, 0, 0, 0, player);
            repair.inventorySlots.get(0)
                .putStack(leftStack);
            repair.inventorySlots.get(1)
                .putStack(rightStack);
            return repair.maximumCost;
        } catch (RuntimeException e) {
            String left = ErrorUtil.getItemStackInfo(leftStack);
            String right = ErrorUtil.getItemStackInfo(rightStack);
            Log.get()
                .error("Could not get anvil level cost for: ({} and {}).", left, right, e);
            return -1;
        }
    }
}
