package ruiseki.jfmuy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ISubtypeRegistry;
import ruiseki.jfmuy.util.Log;

public class SubtypeRegistry implements ISubtypeRegistry {

    private final Map<Item, ISubtypeInterpreter> interpreters = new HashMap<Item, ISubtypeInterpreter>();

    @Override
    public void useNbtForSubtypes(Item... items) {
        for (Item item : items) {
            registerSubtypeInterpreter(item, AllNbt.INSTANCE);
        }
    }

    @Override
    public void registerSubtypeInterpreter(@Nullable Item item, @Nullable ISubtypeInterpreter interpreter) {
        if (item == null) {
            Log.error("Null item", new NullPointerException());
            return;
        }
        if (interpreter == null) {
            Log.error("Null interpreter", new NullPointerException());
            return;
        }

        if (interpreters.containsKey(item)) {
            Log.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
            return;
        }

        interpreters.put(item, interpreter);
    }

    @Nullable
    @Override
    public String getSubtypeInfo(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            Log.error("Null itemStack", new NullPointerException());
            return null;
        }

        Item item = itemStack.getItem();
        if (item == null) {
            Log.error("Null item", new NullPointerException());
            return null;
        }

        ISubtypeInterpreter nbtInterpreter = interpreters.get(item);
        if (nbtInterpreter == null) {
            return null;
        }

        return nbtInterpreter.getSubtypeInfo(itemStack);
    }

    private static class AllNbt implements ISubtypeInterpreter {

        public static final AllNbt INSTANCE = new AllNbt();

        private AllNbt() {}

        @Nullable
        @Override
        public String getSubtypeInfo(ItemStack itemStack) {
            NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
            if (nbtTagCompound == null || nbtTagCompound.hasNoTags()) {
                return null;
            }
            return nbtTagCompound.toString();
        }
    }
}
