package ruiseki.okbase.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;

import ruiseki.okbase.OKBase;
import ruiseki.okcore.item.IItem;

public enum ModItems {

    // spotless: off

    ,;

    // spotless: on

    public static final ModItems[] VALUES = values();

    public static void preInit() {
        for (ModItems item : VALUES) {
            if (item.item == null) {
                continue;
            }
            try {
                item.item.init();
                OKBase.okLog(Level.INFO, "Successfully initialized " + item.name());
            } catch (Exception e) {
                OKBase.okLog(Level.ERROR, "Failed to initialize item: +" + item.name());
            }
        }
    }

    private final IItem item;

    ModItems(IItem block) {
        this.item = block;
    }

    public Item getItem() {
        return item.getItem();
    }

    public ItemStack newItemStack() {
        return newItemStack(1);
    }

    public ItemStack newItemStack(int count) {
        return newItemStack(count, 0);
    }

    public ItemStack newItemStack(int count, int meta) {
        return item != null ? new ItemStack(this.getItem(), count, meta) : null;
    }
}
