package ruiseki.jfmuy.startup;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.util.Log;
import ruiseki.okcore.datastructure.NonNullList;

public class UniqueItemStackListBuilder {

    private final StackHelper stackHelper;
    private final NonNullList<ItemStack> ingredients = NonNullList.create();
    private final Set<String> ingredientUids = new HashSet<>();

    public UniqueItemStackListBuilder(StackHelper stackHelper) {
        this.stackHelper = stackHelper;
    }

    public void add(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        try {
            String uid = stackHelper.getUniqueIdentifierForStack(itemStack);
            if (!ingredientUids.contains(uid)) {
                ingredientUids.add(uid);
                ingredients.add(itemStack);
            }
        } catch (RuntimeException | LinkageError e) {
            Log.get()
                .error("Failed to get unique identifier for stack.", e);
        }
    }

    public NonNullList<ItemStack> build() {
        return ingredients;
    }
}
