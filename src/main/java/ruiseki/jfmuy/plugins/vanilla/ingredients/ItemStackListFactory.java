package ruiseki.jfmuy.plugins.vanilla.ingredients;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.StackHelper;

public class ItemStackListFactory {

    private ItemStackListFactory() {

    }

    public static List<ItemStack> create(StackHelper stackHelper) {
        final List<ItemStack> itemList = new ArrayList<ItemStack>();
        final Set<String> itemNameSet = new HashSet<>();

        for (CreativeTabs creativeTab : CreativeTabs.creativeTabArray) {
            List<ItemStack> creativeTabItemStacks = new ArrayList<ItemStack>();
            try {
                creativeTab.displayAllReleventItems(creativeTabItemStacks);
            } catch (RuntimeException e) {
                Log.error(
                    "Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}",
                    creativeTab,
                    e);
            } catch (LinkageError e) {
                Log.error(
                    "Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}",
                    creativeTab,
                    e);
            }
            for (ItemStack itemStack : creativeTabItemStacks) {
                if (itemStack == null) {
                    Log.error("Found a null itemStack in creative tab: {}", creativeTab);
                } else if (itemStack.getItem() == null) {
                    Log.error("Found a null item in an itemStack from creative tab: {}", creativeTab);
                } else {
                    addItemStack(stackHelper, itemStack, itemList, itemNameSet);
                }
            }
        }

        FMLControlledNamespacedRegistry<Block> blockRegistry = GameData.getBlockRegistry();
        for (Block block : blockRegistry.typeSafeIterable()) {
            if (block != null) {
                addBlockAndSubBlocks(stackHelper, block, itemList, itemNameSet);
            }
        }

        FMLControlledNamespacedRegistry<Item> itemRegistry = GameData.getItemRegistry();
        for (Item item : itemRegistry.typeSafeIterable()) {
            if (item != null) {
                addItemAndSubItems(stackHelper, item, itemList, itemNameSet);
            }
        }

        return itemList;
    }

    private static void addItemAndSubItems(StackHelper stackHelper, @Nullable Item item, List<ItemStack> itemList,
        Set<String> itemNameSet) {
        if (item == null) {
            return;
        }

        List<ItemStack> items = stackHelper.getSubtypes(item, 1);
        for (ItemStack stack : items) {
            if (stack != null) {
                addItemStack(stackHelper, stack, itemList, itemNameSet);
            }
        }
    }

    private static void addBlockAndSubBlocks(StackHelper stackHelper, @Nullable Block block, List<ItemStack> itemList,
        Set<String> itemNameSet) {
        if (block == null) {
            return;
        }

        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return;
        }

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            List<ItemStack> subBlocks = new ArrayList<>();
            try {
                block.getSubBlocks(item, itemTab, subBlocks);
            } catch (RuntimeException e) {
                String itemStackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
                Log.error("Failed to getSubBlocks {}", itemStackInfo, e);
            } catch (LinkageError e) {
                String itemStackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
                Log.error("Failed to getSubBlocks {}", itemStackInfo, e);
            }

            for (ItemStack subBlock : subBlocks) {
                if (subBlock == null) {
                    Log.error("Found null subBlock of {}", block);
                } else if (subBlock.getItem() == null) {
                    Log.error("Found subBlock of {} with null item", block);
                } else {
                    addItemStack(stackHelper, subBlock, itemList, itemNameSet);
                }
            }
        }
    }

    private static void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList,
        Set<String> itemNameSet) {
        String itemKey = null;

        try {
            itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
        } catch (RuntimeException e) {
            String stackInfo = ErrorUtil.getItemStackInfo(stack);
            Log.error("Couldn't get unique name for itemStack {}", stackInfo, e);
        } catch (LinkageError e) {
            String stackInfo = ErrorUtil.getItemStackInfo(stack);
            Log.error("Couldn't get unique name for itemStack {}", stackInfo, e);
        }

        if (itemKey != null) {
            if (itemNameSet.contains(itemKey)) {
                return;
            }
            itemNameSet.add(itemKey);
            itemList.add(stack);
        }
    }
}
