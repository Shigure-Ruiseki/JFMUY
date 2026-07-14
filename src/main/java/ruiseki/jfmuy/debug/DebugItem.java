package ruiseki.jfmuy.debug;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DebugItem extends Item {

    private static final RandomStringMaker RANDOM_STRING_MAKER = new RandomStringMaker(12);
    private static final int MAX_DAMAGE = 1000;

    public DebugItem(String name) {
        setUnlocalizedName(name);
        setCreativeTab(CreativeTabs.tabAllSearch);
        setMaxDamage(MAX_DAMAGE);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
        for (int i = 0; i < 10000; i++) {
            String name = RANDOM_STRING_MAKER.nextString();
            ItemStack itemStack = new ItemStack(itemIn);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("name", name);
            itemStack.setTagCompound(nbt);
            // noinspection unchecked
            subItems.add(itemStack);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return "no nbt";
        }
        return tagCompound.getString("name");
    }
}
