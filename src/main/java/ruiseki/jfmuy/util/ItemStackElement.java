package ruiseki.jfmuy.util;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.config.Config;

/**
 * For getting properties of ItemStacks efficiently
 */
public class ItemStackElement {

    @Nonnull
    private final ItemStack itemStack;
    @Nonnull
    private final String searchString;
    @Nonnull
    private final String modName;

    @Nullable
    public static ItemStackElement create(@Nonnull ItemStack itemStack) {
        try {
            return new ItemStackElement(itemStack);
        } catch (RuntimeException e) {
            Log.warning("Found broken itemStack.", e);
            return null;
        }
    }

    private ItemStackElement(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;

        GameRegistry.UniqueIdentifier uniqueIdentifier = GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
        if (uniqueIdentifier == null) {
            throw new NullPointerException(
                "No name for item in item registry: " + itemStack.getItem()
                    .getClass());
        }
        String modId = uniqueIdentifier.modId.toLowerCase(Locale.ENGLISH);
        String modName = Internal.getItemRegistry()
            .getModNameForItem(itemStack.getItem())
            .toLowerCase(Locale.ENGLISH);

        String displayName = itemStack.getDisplayName();
        if (displayName == null) {
            throw new NullPointerException(
                "No display name for item. " + uniqueIdentifier.modId
                    + ":"
                    + uniqueIdentifier.name
                    + " "
                    + itemStack.getItem()
                        .getClass());
        }

        String searchString = displayName.toLowerCase();

        this.modName = modId + ' ' + modName;

        if (Config.isAtPrefixRequiredForModName()) {
            this.searchString = searchString;
        } else {
            this.searchString = searchString + ' ' + this.modName;
        }
    }

    @Nonnull
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nonnull
    public String getSearchString() {
        return searchString;
    }

    @Nonnull
    public String getModName() {
        return modName;
    }
}
