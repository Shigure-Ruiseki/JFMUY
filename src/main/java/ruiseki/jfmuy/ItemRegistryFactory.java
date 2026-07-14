package ruiseki.jfmuy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.ModList;
import ruiseki.jfmuy.util.StackHelper;

public class ItemRegistryFactory {

    @NotNull
    private final Set<String> itemNameSet = new HashSet<>();
    @NotNull
    private final List<ItemStack> itemList = new ArrayList<>();
    @NotNull
    private final List<ItemStack> fuels = new ArrayList<>();
    @NotNull
    private final List<ItemStack> potionIngredients = new ArrayList<>();

    @NotNull
    private final Set<String> itemWildcardNameSet = new HashSet<>();
    /** The order that items were added, using wildcard. Used to keep similar items together. */
    @NotNull
    private final List<String> itemAddedOrder = new ArrayList<>();

    public ItemRegistry createItemRegistry() {
        final ModList modList = new ModList();

        for (CreativeTabs creativeTab : CreativeTabs.creativeTabArray) {
            List<ItemStack> creativeTabItemStacks = new ArrayList<>();
            try {
                creativeTab.displayAllReleventItems(creativeTabItemStacks);
            } catch (RuntimeException | LinkageError e) {
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
                    addItemStack(itemStack);
                }
            }
        }

        for (Object block : GameData.getBlockRegistry()) {
            addBlockAndSubBlocks((Block) block);
        }
        for (Object item : GameData.getItemRegistry()) {
            addItemAndSubItems((Item) item);
        }

        final StackHelper stackHelper = Internal.getStackHelper();

        Collections.sort(itemList, new Comparator<ItemStack>() {

            @Override
            public int compare(ItemStack stack1, ItemStack stack2) {
                final String stack1ModName = modList.getModNameForItem(stack1.getItem());
                final String stack2ModName = modList.getModNameForItem(stack2.getItem());

                if (stack1ModName.equals(stack2ModName)) {
                    final String itemUid1 = stackHelper
                        .getUniqueIdentifierForStack(stack1, StackHelper.UidMode.WILDCARD);
                    final String itemUid2 = stackHelper
                        .getUniqueIdentifierForStack(stack2, StackHelper.UidMode.WILDCARD);
                    final int itemOrderIndex1 = itemAddedOrder.indexOf(itemUid1);
                    final int itemOrderIndex2 = itemAddedOrder.indexOf(itemUid2);
                    return Integer.compare(itemOrderIndex1, itemOrderIndex2);
                } else if (stack1ModName.equals(Reference.MINECRAFT_MOD_NAME)) {
                    return -1;
                } else if (stack2ModName.equals(Reference.MINECRAFT_MOD_NAME)) {
                    return 1;
                } else {
                    return stack1ModName.compareTo(stack2ModName);
                }
            }
        });

        ImmutableListMultimap.Builder<String, ItemStack> itemsByModIdBuilder = ImmutableListMultimap.builder();
        for (ItemStack itemStack : itemList) {
            Item item = itemStack.getItem();
            if (item != null) {
                String modId = stackHelper.getModId(itemStack)
                    .toLowerCase(Locale.ENGLISH);
                itemsByModIdBuilder.put(modId, itemStack);
            }
        }

        return new ItemRegistry(
            ImmutableList.copyOf(itemList),
            itemsByModIdBuilder.build(),
            ImmutableList.copyOf(potionIngredients),
            ImmutableList.copyOf(fuels),
            modList);
    }

    private void addItemAndSubItems(@Nullable Item item) {
        if (item == null) {
            return;
        }

        List<ItemStack> items = Internal.getStackHelper()
            .getSubtypes(item, 1);
        for (ItemStack stack : items) {
            if (stack != null) {
                addItemStack(stack);
            }
        }
    }

    private void addBlockAndSubBlocks(@Nullable Block block) {
        if (block == null) {
            return;
        }

        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return;
        }

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            List<ItemStack> subBlocks = new ArrayList<>();
            block.getSubBlocks(item, itemTab, subBlocks);
            for (ItemStack subBlock : subBlocks) {
                if (subBlock == null) {
                    Log.error("Found null subBlock of {}", block);
                } else if (subBlock.getItem() == null) {
                    Log.error("Found subBlock of {} with null item", block);
                } else {
                    addItemStack(subBlock);
                }
            }
        }
    }

    private void addItemStack(@NotNull ItemStack stack) {
        StackHelper stackHelper = Internal.getStackHelper();
        try {
            final String itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);

            if (itemNameSet.contains(itemKey)) {
                return;
            }
            itemNameSet.add(itemKey);
            itemList.add(stack);

            final String itemWildcardKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.WILDCARD);
            if (!itemWildcardNameSet.contains(itemWildcardKey)) {
                itemWildcardNameSet.add(itemWildcardKey);
                itemAddedOrder.add(itemWildcardKey);
            }

            if (TileEntityFurnace.isItemFuel(stack)) {
                fuels.add(stack);
            }

            if (stack.getItem()
                .isPotionIngredient(stack)) {
                potionIngredients.add(stack);
            }
        } catch (RuntimeException e) {
            String stackInfo = ErrorUtil.getItemStackInfo(stack);
            Log.error("Couldn't create unique name for itemStack {}", stackInfo, e);
        }
    }
}
