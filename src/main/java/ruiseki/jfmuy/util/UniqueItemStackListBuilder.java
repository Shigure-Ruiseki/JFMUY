package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.jfmuy.Internal;

public class UniqueItemStackListBuilder {

    private final List<ItemStack> itemStacks = new ArrayList<>();
    private final Set<String> itemStackUids = new HashSet<>();

    public void add(@NotNull ItemStack itemStack) {
        String uid = Internal.getStackHelper()
            .getUniqueIdentifierForStack(itemStack, StackHelper.UidMode.NORMAL);
        if (!itemStackUids.contains(uid)) {
            itemStackUids.add(uid);
            itemStacks.add(itemStack);
        }
    }

    @NotNull
    public List<ItemStack> build() {
        return itemStacks;
    }
}
