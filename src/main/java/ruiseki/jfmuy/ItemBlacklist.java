package ruiseki.jfmuy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IItemBlacklist;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.StackUtil;

public class ItemBlacklist implements IItemBlacklist {

    @Nonnull
    private final Set<String> itemBlacklist = new HashSet<>();

    @Override
    public void addItemToBlacklist(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            Log.error("Null itemStack", new NullPointerException());
            return;
        }
        String uid = StackUtil.getUniqueIdentifierForStack(itemStack);
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
        String uid = StackUtil.getUniqueIdentifierForStack(itemStack);
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
        List<String> uids = StackUtil.getUniqueIdentifiersWithWildcard(itemStack);
        for (String uid : uids) {
            if (itemBlacklist.contains(uid) || Config.getItemBlacklist()
                .contains(uid)) {
                return true;
            }
        }
        return false;
    }
}
