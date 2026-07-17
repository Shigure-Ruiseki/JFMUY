package ruiseki.jfmuy.plugins.vanilla.anvil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IVanillaRecipeFactory;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public final class AnvilRecipeMaker {

    private AnvilRecipeMaker() {}

    public static List<IRecipeWrapper> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory,
        IIngredientRegistry ingredientRegistry) {
        Stopwatch sw = Stopwatch.createStarted();
        return Stream.concat(
            wrapStream("vanilla repair", () -> getRepairRecipes(vanillaRecipeFactory), sw),
            wrapStream("enchantment", () -> getBookEnchantmentRecipes(vanillaRecipeFactory, ingredientRegistry), sw))
            .collect(Collectors.toList());
    }

    private static <T> Stream<T> wrapStream(String name, Supplier<Stream<T>> supplier, Stopwatch stopwatch) {
        stopwatch.reset();
        stopwatch.start();
        try {
            List<T> data = supplier.get()
                .collect(Collectors.toList());
            Log.get()
                .debug("Registered {} recipes in {}", name, stopwatch);
            return data.stream();
        } catch (RuntimeException e) {
            Log.get()
                .error("Failed to create {} recipes.", name, e);
            return Stream.empty();
        } finally {
            stopwatch.stop();
        }
    }

    /* Enchantment recipes */

    private static final class EnchantmentData {

        private final Enchantment enchantment;
        private final List<ItemStack> enchantedBooks;

        EnchantmentData(Enchantment enchantment) {
            this.enchantment = enchantment;
            this.enchantedBooks = enumerateBooks(enchantment);
        }

        public List<ItemStack> getEnchantedBooks(ItemStack ingredient) {
            Item item = ingredient.getItem();
            List<ItemStack> list = enchantedBooks.stream()
                .filter(enchantedBook -> item.isBookEnchantable(ingredient, enchantedBook))
                .collect(Collectors.toList());
            return list.size() == enchantedBooks.size() ? enchantedBooks : ImmutableList.copyOf(list);
        }

        private boolean canEnchant(ItemStack ingredient) {
            try {
                return enchantment.canApply(ingredient);
            } catch (RuntimeException e) {
                String stackInfo = ErrorUtil.getItemStackInfo(ingredient);
                Log.get()
                    .error("Failed to check if ingredient can be enchanted: {}", stackInfo, e);
                return false;
            }
        }

        private static List<ItemStack> enumerateBooks(Enchantment enchantment) {
            return IntStream.rangeClosed(enchantment.getMinLevel(), enchantment.getMaxLevel())
                .mapToObj(level -> {
                    ItemStack book = new ItemStack(Items.enchanted_book);
                    Items.enchanted_book
                        .addEnchantment(book, new net.minecraft.enchantment.EnchantmentData(enchantment, level));
                    return book;
                })
                .collect(Collectors.toList());
        }
    }

    private static Stream<IRecipeWrapper> getBookEnchantmentRecipes(IVanillaRecipeFactory vanillaRecipeFactory,
        IIngredientRegistry ingredientRegistry) {
        List<EnchantmentData> enchantmentDatas = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.enchantmentsList) {
            if (enchantment != null) {
                enchantmentDatas.add(new EnchantmentData(enchantment));
            }
        }

        Collection<ItemStack> ingredients = ingredientRegistry.getAllIngredients(VanillaTypes.ITEM);
        return ingredients.stream()
            .filter(ItemStack::isItemEnchantable)
            .flatMap(ingredient -> getBookEnchantmentRecipes(vanillaRecipeFactory, enchantmentDatas, ingredient));
    }

    private static Stream<IRecipeWrapper> getBookEnchantmentRecipes(IVanillaRecipeFactory vanillaRecipeFactory,
        List<EnchantmentData> enchantmentDatas, ItemStack ingredient) {
        List<ItemStack> ingredientSingletonList = ImmutableList.of(ingredient);
        return enchantmentDatas.stream()
            .filter(data -> data.canEnchant(ingredient))
            .map(data -> data.getEnchantedBooks(ingredient))
            .filter(enchantedBooks -> !enchantedBooks.isEmpty())
            .map(enchantedBooks -> {
                List<ItemStack> outputs = getEnchantedIngredients(ingredient, enchantedBooks);
                return vanillaRecipeFactory.createAnvilRecipe(ingredientSingletonList, enchantedBooks, outputs);
            });
    }

    private static List<ItemStack> getEnchantedIngredients(ItemStack ingredient, List<ItemStack> enchantedBooks) {
        return Lists.transform(enchantedBooks, enchantedBook -> getEnchantedIngredient(ingredient, enchantedBook));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ItemStack getEnchantedIngredient(ItemStack ingredient, ItemStack enchantedBook) {
        ItemStack enchantedIngredient = ingredient.copy();
        Map enchantments = EnchantmentHelper.getEnchantments(enchantedBook);
        EnchantmentHelper.setEnchantments(enchantments, enchantedIngredient);
        return enchantedIngredient;
    }

    /* Repair recipes */

    private static class RepairData {

        private final ItemStack repairIngredient;
        private final List<ItemStack> repairables;

        public RepairData(ItemStack repairIngredient, ItemStack... repairables) {
            this.repairIngredient = repairIngredient;
            this.repairables = Collections.unmodifiableList(Arrays.asList(repairables));
        }

        public ItemStack getRepairStack() {
            return repairIngredient;
        }

        public List<ItemStack> getRepairables() {
            return repairables;
        }
    }

    private static Stream<RepairData> getRepairData() {
        return Stream.of(
            new RepairData(
                Item.ToolMaterial.WOOD.getRepairItemStack(),
                new ItemStack(Items.wooden_sword),
                new ItemStack(Items.wooden_pickaxe),
                new ItemStack(Items.wooden_axe),
                new ItemStack(Items.wooden_shovel),
                new ItemStack(Items.wooden_hoe)),
            new RepairData(
                Item.ToolMaterial.STONE.getRepairItemStack(),
                new ItemStack(Items.stone_sword),
                new ItemStack(Items.stone_pickaxe),
                new ItemStack(Items.stone_axe),
                new ItemStack(Items.stone_shovel),
                new ItemStack(Items.stone_hoe)),
            new RepairData(
                new ItemStack(ItemArmor.ArmorMaterial.CLOTH.func_151685_b()),
                new ItemStack(Items.leather_helmet),
                new ItemStack(Items.leather_chestplate),
                new ItemStack(Items.leather_leggings),
                new ItemStack(Items.leather_boots)),
            new RepairData(
                Item.ToolMaterial.IRON.getRepairItemStack(),
                new ItemStack(Items.iron_sword),
                new ItemStack(Items.iron_pickaxe),
                new ItemStack(Items.iron_axe),
                new ItemStack(Items.iron_shovel),
                new ItemStack(Items.iron_hoe)),
            new RepairData(
                new ItemStack(ItemArmor.ArmorMaterial.IRON.func_151685_b()),
                new ItemStack(Items.iron_helmet),
                new ItemStack(Items.iron_chestplate),
                new ItemStack(Items.iron_leggings),
                new ItemStack(Items.iron_boots)),
            new RepairData(
                new ItemStack(ItemArmor.ArmorMaterial.CHAIN.func_151685_b()),
                new ItemStack(Items.chainmail_helmet),
                new ItemStack(Items.chainmail_chestplate),
                new ItemStack(Items.chainmail_leggings),
                new ItemStack(Items.chainmail_boots)),
            new RepairData(
                Item.ToolMaterial.GOLD.getRepairItemStack(),
                new ItemStack(Items.golden_sword),
                new ItemStack(Items.golden_pickaxe),
                new ItemStack(Items.golden_axe),
                new ItemStack(Items.golden_shovel),
                new ItemStack(Items.golden_hoe)),
            new RepairData(
                new ItemStack(ItemArmor.ArmorMaterial.GOLD.func_151685_b()),
                new ItemStack(Items.golden_helmet),
                new ItemStack(Items.golden_chestplate),
                new ItemStack(Items.golden_leggings),
                new ItemStack(Items.golden_boots)),
            new RepairData(
                Item.ToolMaterial.EMERALD.getRepairItemStack(),
                new ItemStack(Items.diamond_sword),
                new ItemStack(Items.diamond_pickaxe),
                new ItemStack(Items.diamond_axe),
                new ItemStack(Items.diamond_shovel),
                new ItemStack(Items.diamond_hoe)),
            new RepairData(
                new ItemStack(ItemArmor.ArmorMaterial.DIAMOND.func_151685_b()),
                new ItemStack(Items.diamond_helmet),
                new ItemStack(Items.diamond_chestplate),
                new ItemStack(Items.diamond_leggings),
                new ItemStack(Items.diamond_boots)));
    }

    private static Stream<IRecipeWrapper> getRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
        return getRepairData().flatMap(repairData -> getRepairRecipes(repairData, vanillaRecipeFactory));
    }

    private static Stream<IRecipeWrapper> getRepairRecipes(RepairData repairData,
        IVanillaRecipeFactory vanillaRecipeFactory) {
        List<ItemStack> repairStackInput = ImmutableList.of(repairData.getRepairStack());
        List<ItemStack> repairables = repairData.getRepairables();
        Stream.Builder<IRecipeWrapper> recipes = Stream.builder();

        for (ItemStack repairable : repairables) {
            ItemStack damagedThreeQuarters = repairable.copy();
            damagedThreeQuarters.setItemDamage(damagedThreeQuarters.getMaxDamage() * 3 / 4);
            ItemStack damagedHalf = repairable.copy();
            damagedHalf.setItemDamage(damagedHalf.getMaxDamage() / 2);

            List<ItemStack> damagedThreeQuartersSingletonList = ImmutableList.of(damagedThreeQuarters);
            IRecipeWrapper repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
                damagedThreeQuartersSingletonList,
                damagedThreeQuartersSingletonList,
                ImmutableList.of(damagedHalf));
            recipes.add(repairWithSame);

            ItemStack damagedFully = repairable.copy();
            damagedFully.setItemDamage(damagedFully.getMaxDamage());
            IRecipeWrapper repairWithMaterial = vanillaRecipeFactory
                .createAnvilRecipe(ImmutableList.of(damagedFully), repairStackInput, damagedThreeQuartersSingletonList);
            recipes.add(repairWithMaterial);
        }

        return recipes.build();
    }

    public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return -1;
        }
        InventoryPlayer fakeInventory = new InventoryPlayer(player);
        try {
            ContainerRepair repair = new ContainerRepair(fakeInventory, player.worldObj, 0, 0, 0, player);
            repair.getSlot(0)
                .putStack(leftStack);
            repair.getSlot(1)
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
