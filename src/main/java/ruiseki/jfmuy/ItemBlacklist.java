package ruiseki.jfmuy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.util.Log;

public class ItemBlacklist implements IItemBlacklist {

    @NotNull
    private final Set<String> itemBlacklist = new HashSet<>();

    @Override
    public void addItemToBlacklist(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            Log.error("Null itemStack", new NullPointerException());
            return;
        }
        String uid = Internal.getStackHelper()
            .getUniqueIdentifierForStack(itemStack);
        itemBlacklist.add(uid);

        JFMUY.getProxy()
            .resetItemFilter();
    }

    @Override
    public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            Log.error("Null itemStack", new NullPointerException());
            return;
        }
        String uid = Internal.getStackHelper()
            .getUniqueIdentifierForStack(itemStack);
        itemBlacklist.remove(uid);

        JFMUY.getProxy()
            .resetItemFilter();
    }

    @Override
    public boolean isItemBlacklisted(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            Log.error("Null itemStack", new NullPointerException());
            return false;
        }
        List<String> uids = Internal.getStackHelper()
            .getUniqueIdentifiersWithWildcard(itemStack);
        for (String uid : uids) {
            if (itemBlacklist.contains(uid)) {
                return true;
            }
        }
        return Config.isItemOnConfigBlacklist(itemStack);
    }
}
