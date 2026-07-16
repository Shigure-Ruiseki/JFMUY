package ruiseki.jfmuy.plugins.vanilla.ingredients.item;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.color.ColorGetter;
import ruiseki.jfmuy.startup.StackHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.okcore.helper.Helpers;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {

    private final StackHelper stackHelper;

    public ItemStackHelper(StackHelper stackHelper) {
        this.stackHelper = stackHelper;
    }

    @Override
    public List<ItemStack> expandSubtypes(List<ItemStack> contained) {
        return stackHelper.getAllSubtypes(contained);
    }

    @Override
    public IFocus<?> translateFocus(IFocus<ItemStack> focus, IFocusFactory focusFactory) {
        ItemStack itemStack = focus.getValue();
        Item item = itemStack.getItem();
        // Special case for ItemBlocks containing fluid blocks.
        // Nothing crafts those, the player probably wants to look up fluids.
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).field_150939_a;
            Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
            if (fluid != null) {
                FluidStack fluidStack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
                return focusFactory.createFocus(focus.getMode(), fluidStack);
            }
        }
        return focus;
    }

    @Override
    @Nullable
    public ItemStack getMatch(Iterable<ItemStack> ingredients, ItemStack toMatch) {
        return stackHelper.containsStack(ingredients, toMatch);
    }

    @Override
    public String getDisplayName(ItemStack ingredient) {
        return ErrorUtil.checkNotNull(ingredient.getDisplayName(), "itemStack.getDisplayName()");
    }

    @Override
    public String getUniqueId(ItemStack ingredient) {
        ErrorUtil.checkNotEmpty(ingredient);
        return stackHelper.getUniqueIdentifierForStack(ingredient);
    }

    @Override
    public String getWildcardId(ItemStack ingredient) {
        ErrorUtil.checkNotEmpty(ingredient);
        return stackHelper.getUniqueIdentifierForStack(ingredient, StackHelper.UidMode.WILDCARD);
    }

    @Override
    public String getModId(ItemStack ingredient) {
        ErrorUtil.checkNotEmpty(ingredient);

        Item item = ingredient.getItem();
        ResourceLocation itemName = Helpers.getLocation(item);
        if (itemName == null) {
            String stackInfo = getErrorInfo(ingredient);
            throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
        }

        return itemName.getResourceDomain();
    }

    @Override
    public String getDisplayModId(ItemStack ingredient) {
        ErrorUtil.checkNotEmpty(ingredient);

        Item item = ingredient.getItem();
        ResourceLocation itemName = Helpers.getLocation(item);
        if (itemName == null) {
            String stackInfo = getErrorInfo(ingredient);
            throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
        }

        return itemName.getResourceDomain();
    }

    @Override
    public Iterable<Color> getColors(ItemStack ingredient) {
        return ColorGetter.getColors(ingredient, 2);
    }

    @Override
    public String getResourceId(ItemStack ingredient) {
        ErrorUtil.checkNotEmpty(ingredient);

        Item item = ingredient.getItem();
        ResourceLocation itemName = Helpers.getLocation(item);
        if (itemName == null) {
            String stackInfo = getErrorInfo(ingredient);
            throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
        }

        return itemName.getResourcePath();
    }

    @Override
    public int getOrdinal(ItemStack ingredient) {
        return ingredient.getItemDamage();
    }

    @Override
    public ItemStack getCheatItemStack(ItemStack ingredient) {
        return ingredient;
    }

    @Override
    public ItemStack copyIngredient(ItemStack ingredient) {
        return ingredient.copy();
    }

    @Override
    public boolean isValidIngredient(ItemStack ingredient) {
        return ingredient != null;
    }

    @Override
    public boolean isIngredientOnServer(ItemStack ingredient) {
        if (ingredient.getItem() == null) {
            return false;
        }
        Item item = ingredient.getItem();
        String registryName = GameData.getItemRegistry()
            .getNameForObject(item);

        return GameData.getItemRegistry()
            .containsKey(registryName);
    }

    @Override
    public Collection<String> getOreDictNames(ItemStack ingredient) {
        int[] oreIds = OreDictionary.getOreIDs(ingredient);
        Collection<String> names = new ArrayList<>(oreIds.length);
        for (int oreId : oreIds) {
            names.add(OreDictionary.getOreName(oreId));
        }
        return names;
    }

    @Override
    public Collection<String> getCreativeTabNames(ItemStack ingredient) {
        Collection<String> creativeTabsStrings = new ArrayList<>();
        Item item = ingredient.getItem();
        if (item == null) return creativeTabsStrings;
        for (CreativeTabs creativeTab : item.getCreativeTabs()) {
            if (creativeTab != null) {
                String creativeTabName = StatCollector.translateToLocal(creativeTab.getTranslatedTabLabel());
                creativeTabsStrings.add(creativeTabName);
            }
        }
        return creativeTabsStrings;
    }

    @Override
    public String getErrorInfo(ItemStack ingredient) {
        return ErrorUtil.getItemStackInfo(ingredient);
    }
}
